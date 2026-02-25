package dao.order_item.impl;

import dao.order_item.OrderItemDao;
import exception.DaoException;
import model.OrderItem;
import util.Tool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDaoImpl implements OrderItemDao {
    private final Tool conn = new Tool();

    @Override
    public int create(OrderItem item) {
        String sql = "INSERT INTO order_items(order_id, product_id, quantity, unit_price_per_night)\n" +
                "                VALUES(?,?,?,?)";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPricePerNight());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new DaoException("Create order item failed", e);
        }
    }

    @Override
    public List<OrderItem> listByOrderId(int orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id=? ORDER BY id";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("List order items failed", e);
        }
    }

    @Override
    public boolean deleteByOrderId(int orderId) {
        String sql = "DELETE FROM order_items WHERE order_id=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new DaoException("Delete order items failed", e);
        }
    }

    private OrderItem map(ResultSet rs) throws SQLException {
        OrderItem i = new OrderItem();
        i.setId(rs.getInt("id"));
        i.setOrderId(rs.getInt("order_id"));
        i.setProductId(rs.getInt("product_id"));
        i.setQuantity(rs.getInt("quantity"));
        i.setUnitPricePerNight(rs.getBigDecimal("unit_price_per_night"));
        return i;
    }
}
