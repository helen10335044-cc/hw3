package service;

import exception.ServiceException;
import model.Product;
import util.Tool;
import vo.Page;
import model.Order;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class OrderService {
    private final Tool conn = new Tool();

    public static class CartLine {
        public final int productId;
        public final int qty;
        public CartLine(int productId, int qty) { this.productId = productId; this.qty = qty; }
    }

    public String placeOrder(int customerId, List<CartLine> cart, LocalDate checkIn, LocalDate checkOut) {
        if (cart == null || cart.isEmpty()) throw new ServiceException("請至少選擇一個房型");
        if (checkIn == null || checkOut == null) throw new ServiceException("請選擇入住/退房日期");
        if (!checkIn.isBefore(checkOut)) throw new ServiceException("退房日期必須晚於入住日期");
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) throw new ServiceException("入住天數不正確");

        String orderNo = "O-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        try (Connection c = conn.getDb()) {
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            try {
                Map<Integer, Product> productMap = new HashMap<>();

                for (CartLine line : cart) {
                    Product p = lockProductRow(c, line.productId);
                    productMap.put(line.productId, p);

                    if (existsCustomerProductOverlapTx(c, customerId, line.productId, checkIn, checkOut)) {
                        throw new ServiceException("你已經在此日期區間內訂過：" + p.getProductName());
                    }

                    int bookedQty = bookedQtyOverlapTx(c, line.productId, checkIn, checkOut);
                    int available = p.getStock() - bookedQty;
                    if (line.qty > available) {
                        throw new ServiceException("房型【" + p.getProductName() + "】剩餘 " + Math.max(0, available) + " 間，無法下單 " + line.qty + " 間");
                    }
                    if (line.qty <= 0 || line.qty > p.getStock()) {
                        throw new ServiceException("房型【" + p.getProductName() + "】數量不正確");
                    }
                }

                BigDecimal total = BigDecimal.ZERO;
                for (CartLine line : cart) {
                    Product p = productMap.get(line.productId);
                    total = total.add(p.getPricePerNight().multiply(BigDecimal.valueOf(nights)).multiply(BigDecimal.valueOf(line.qty)));
                }

                int orderId = insertOrderTx(c, orderNo, customerId, checkIn, checkOut, total);

                for (CartLine line : cart) {
                    Product p = productMap.get(line.productId);
                    insertOrderItemTx(c, orderId, line.productId, line.qty, p.getPricePerNight());
                }

                c.commit();
                return orderNo;
            } catch (Exception e) {
                c.rollback();
                if (e instanceof ServiceException) throw (ServiceException) e;
                throw new ServiceException("下單失敗: " + e.getMessage(), e);
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("DB 錯誤: " + e.getMessage(), e);
        }
    }

    public boolean deleteOrder(int orderId) {
        String sql1 = "DELETE FROM order_items WHERE order_id=?";
        String sql2 = "DELETE FROM orders WHERE id=?";
        try (Connection c = conn.getDb()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps1 = c.prepareStatement(sql1);
                 PreparedStatement ps2 = c.prepareStatement(sql2)) {
                ps1.setInt(1, orderId);
                ps1.executeUpdate();
                ps2.setInt(1, orderId);
                int n = ps2.executeUpdate();
                c.commit();
                return n == 1;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("刪除訂單失敗: " + e.getMessage(), e);
        }
    }

    public Page<Order> listOrders(Integer customerId, String status, LocalDate from, LocalDate to, int page, int pageSize) {
        int offset = (page - 1) * pageSize;

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (customerId != null) { where.append(" AND customer_id=? "); params.add(customerId); }
        if (status != null && !status.trim().isEmpty()) { where.append(" AND status=? "); params.add(status.trim()); }
        if (from != null) { where.append(" AND check_in_date >= ? "); params.add(Date.valueOf(from)); }
        if (to != null) { where.append(" AND check_out_date <= ? "); params.add(Date.valueOf(to)); }

        String countSql = "SELECT COUNT(*) FROM orders" + where;
        String listSql = "SELECT * FROM orders" + where + " ORDER BY created_at DESC LIMIT ? OFFSET ?";

        try (Connection c = conn.getDb()) {
            long total;
            try (PreparedStatement ps = c.prepareStatement(countSql)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) { rs.next(); total = rs.getLong(1); }
            }

            List<Order> items = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(listSql)) {
                List<Object> p2 = new ArrayList<>(params);
                p2.add(pageSize);
                p2.add(offset);
                bind(ps, p2);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) items.add(mapOrder(rs));
                }
            }

            return new Page<>(items, page, pageSize, total);
        } catch (SQLException e) {
            throw new ServiceException("查詢訂單失敗: " + e.getMessage(), e);
        }
    }

    // ---------- TX helpers ----------
    private Product lockProductRow(Connection c, int productId) throws SQLException {
        String sql = "SELECT * FROM product WHERE id=? FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new ServiceException("商品不存在: " + productId);
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setProductCode(rs.getString("product_code"));
                p.setProductName(rs.getString("product_name"));
                p.setPricePerNight(rs.getBigDecimal("price_per_night"));
                p.setStock(rs.getInt("stock"));
                p.setImagePath(rs.getString("image_path"));
                p.setDescription(rs.getString("description"));
                return p;
            }
        }
    }

    private int bookedQtyOverlapTx(Connection c, int productId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        String sql = "SELECT COALESCE(SUM(oi.quantity),0) AS booked_qty\n" +
                "                FROM orders o\n" +
                "                JOIN order_items oi ON oi.order_id=o.id\n" +
                "                WHERE o.status='PAID'\n" +
                "                  AND oi.product_id=?\n" +
                "                  AND NOT (o.check_out_date <= ? OR o.check_in_date >= ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setDate(2, Date.valueOf(checkIn));
            ps.setDate(3, Date.valueOf(checkOut));
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt("booked_qty"); }
        }
    }

    private boolean existsCustomerProductOverlapTx(Connection c, int customerId, int productId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        String sql = "SELECT 1\n" +
                "                FROM orders o\n" +
                "                JOIN order_items oi ON oi.order_id=o.id\n" +
                "                WHERE o.status='PAID'\n" +
                "                  AND o.customer_id=?\n" +
                "                  AND oi.product_id=?\n" +
                "                  AND NOT (o.check_out_date <= ? OR o.check_in_date >= ?)\n" +
                "                LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            ps.setDate(3, Date.valueOf(checkIn));
            ps.setDate(4, Date.valueOf(checkOut));
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private int insertOrderTx(Connection c, String orderNo, int customerId, LocalDate checkIn, LocalDate checkOut, BigDecimal total) throws SQLException {
        String sql = "INSERT INTO orders(order_no, customer_id, check_in_date, check_out_date, total_amount, status, created_at)\n" +
                "                VALUES(?,?,?,?,?,'PAID',?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, orderNo);
            ps.setInt(2, customerId);
            ps.setDate(3, Date.valueOf(checkIn));
            ps.setDate(4, Date.valueOf(checkOut));
            ps.setBigDecimal(5, total);
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
        }
    }

    private void insertOrderItemTx(Connection c, int orderId, int productId, int qty, BigDecimal unitPrice) throws SQLException {
        String sql = "INSERT INTO order_items(order_id, product_id, quantity, unit_price_per_night)\n" +
                "                VALUES(?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, productId);
            ps.setInt(3, qty);
            ps.setBigDecimal(4, unitPrice);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setOrderNo(rs.getString("order_no"));
        o.setCustomerId(rs.getInt("customer_id"));
        o.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        o.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        o.setCreatedAt(ts == null ? LocalDateTime.now() : ts.toLocalDateTime());
        return o;
    }
}
