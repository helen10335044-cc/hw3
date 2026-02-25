package dao.product.impl;

import dao.product.ProductDao;
import exception.DaoException;
import model.Product;
import util.Tool;
import vo.Page;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDaoImpl implements ProductDao {

    private final Tool conn = new Tool();

    @Override
    public int create(Product p) {
        String sql = "INSERT INTO product(product_code, product_name, price_per_night, stock, image_path, description)\n" +
                "                VALUES(?,?,?,?,?,?)";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getProductCode());
            ps.setString(2, p.getProductName());
            ps.setBigDecimal(3, p.getPricePerNight());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getImagePath());
            ps.setString(6, p.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new DaoException("Create product failed", e);
        }
    }

    @Override
    public boolean update(Product p) {
        String sql = "UPDATE product\n" +
                "                SET product_code=?, product_name=?, price_per_night=?, stock=?, image_path=?, description=?\n" +
                "                WHERE id=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getProductCode());
            ps.setString(2, p.getProductName());
            ps.setBigDecimal(3, p.getPricePerNight());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getImagePath());
            ps.setString(6, p.getDescription());
            ps.setInt(7, p.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DaoException("Update product failed", e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM product WHERE id=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DaoException("Delete product failed", e);
        }
    }

    @Override
    public Optional<Product> findById(int id) {
        String sql = "SELECT * FROM product WHERE id=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Find product by id failed", e);
        }
    }

    @Override
    public Optional<Product> findByCode(String code) {
        String sql = "SELECT * FROM product WHERE product_code=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Find product by code failed", e);
        }
    }

    @Override
    public List<Product> listAll() {
        String sql = "SELECT * FROM product ORDER BY id";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Product> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new DaoException("List products failed", e);
        }
    }

    @Override
    public Page<Product> listPaged(String keyword, int page, int pageSize) {
        String kw = (keyword == null) ? "" : keyword.trim();
        int offset = (page - 1) * pageSize;

        String where = " WHERE product_code LIKE ? OR product_name LIKE ? ";
        String countSql = "SELECT COUNT(*) FROM product" + where;
        String listSql = "SELECT * FROM product" + where + " ORDER BY id LIMIT ? OFFSET ?";

        try (Connection c = conn.getDb()) {
            long total;
            try (PreparedStatement ps = c.prepareStatement(countSql)) {
                ps.setString(1, "%" + kw + "%");
                ps.setString(2, "%" + kw + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    total = rs.getLong(1);
                }
            }

            List<Product> items = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(listSql)) {
                ps.setString(1, "%" + kw + "%");
                ps.setString(2, "%" + kw + "%");
                ps.setInt(3, pageSize);
                ps.setInt(4, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) items.add(map(rs));
                }
            }

            return new Page<>(items, page, pageSize, total);
        } catch (SQLException e) {
            throw new DaoException("List products paged failed", e);
        }
    }

    @Override
    public int getAvailableStock(int productId, LocalDate checkIn, LocalDate checkOut) {
        Optional<Product> p = findById(productId);
        if (!p.isPresent()) return 0;
        int booked = getBookedQtyOverlap(productId, checkIn, checkOut);
        return Math.max(0, p.get().getStock() - booked);
    }

    @Override
    public int getBookedQtyOverlap(int productId, LocalDate checkIn, LocalDate checkOut) {
        String sql = "SELECT COALESCE(SUM(oi.quantity),0) AS booked_qty\n" +
                "                FROM orders o\n" +
                "                JOIN order_items oi ON oi.order_id = o.id\n" +
                "                WHERE o.status='PAID'\n" +
                "                  AND oi.product_id=?\n" +
                "                  AND NOT (o.check_out_date <= ? OR o.check_in_date >= ?)";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setDate(2, Date.valueOf(checkIn));
            ps.setDate(3, Date.valueOf(checkOut));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("booked_qty");
            }
        } catch (SQLException e) {
            throw new DaoException("Get booked qty overlap failed", e);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
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
