package dao.order.impl;

import dao.order.OrderDao;
import exception.DaoException;
import model.Order;
import util.Tool;
import vo.Page;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDaoImpl implements OrderDao {
    private final Tool conn = new Tool();

    @Override
    public int create(Order o) {
        String sql = "INSERT INTO orders(order_no, customer_id, check_in_date, check_out_date, total_amount, status, created_at)\n" +
                "                VALUES(?,?,?,?,?,?,?)";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, o.getOrderNo());
            ps.setInt(2, o.getCustomerId());
            ps.setDate(3, Date.valueOf(o.getCheckInDate()));
            ps.setDate(4, Date.valueOf(o.getCheckOutDate()));
            ps.setBigDecimal(5, o.getTotalAmount());
            ps.setString(6, o.getStatus());
            ps.setTimestamp(7, Timestamp.valueOf(o.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new DaoException("Create order failed", e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql1 = "DELETE FROM order_items WHERE order_id=?";
        String sql2 = "DELETE FROM orders WHERE id=?";
        try (Connection c = conn.getDb()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps1 = c.prepareStatement(sql1);
                 PreparedStatement ps2 = c.prepareStatement(sql2)) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
                ps2.setInt(1, id);
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
            throw new DaoException("Delete order failed", e);
        }
    }

    @Override
    public Optional<Order> findById(int id) {
        String sql = "SELECT * FROM orders WHERE id=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Find order by id failed", e);
        }
    }

    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        String sql = "SELECT * FROM orders WHERE order_no=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, orderNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Find order by order_no failed", e);
        }
    }

    @Override
    public List<Order> listByCustomer(int customerId) {
        String sql = "SELECT * FROM orders WHERE customer_id=? ORDER BY created_at DESC";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("List orders by customer failed", e);
        }
    }

    @Override
    public Page<Order> listPaged(Integer customerId, String status, LocalDate from, LocalDate to, int page, int pageSize) {
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
                    while (rs.next()) items.add(map(rs));
                }
            }

            return new Page<>(items, page, pageSize, total);
        } catch (SQLException e) {
            throw new DaoException("List orders paged failed", e);
        }
    }

    @Override
    public boolean existsCustomerProductOverlap(int customerId, int productId, LocalDate checkIn, LocalDate checkOut) {
        String sql = "SELECT 1\n" +
                "                FROM orders o\n" +
                "                JOIN order_items oi ON oi.order_id=o.id\n" +
                "                WHERE o.status='PAID'\n" +
                "                  AND o.customer_id=?\n" +
                "                  AND oi.product_id=?\n" +
                "                  AND NOT (o.check_out_date <= ? OR o.check_in_date >= ?)\n" +
                "                LIMIT 1";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            ps.setDate(3, Date.valueOf(checkIn));
            ps.setDate(4, Date.valueOf(checkOut));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("Check duplicate booking failed", e);
        }
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
    }

    private Order map(ResultSet rs) throws SQLException {
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
