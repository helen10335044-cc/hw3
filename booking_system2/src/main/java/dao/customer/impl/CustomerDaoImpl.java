package dao.customer.impl;

import dao.customer.CustomerDao;
import exception.DaoException;
import model.Customer;
import util.Tool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDaoImpl implements CustomerDao {
    private final Tool conn = new Tool();

    @Override
    public int create(Customer cst) {
        String sql = "INSERT INTO customer(customer_no, customer_name, phone, address, username, password)\n" +
                "                VALUES(?,?,?,?,?,?)";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cst.getCustomerNo());
            ps.setString(2, cst.getCustomerName());
            ps.setString(3, cst.getPhone());
            ps.setString(4, cst.getAddress());
            ps.setString(5, cst.getUsername());
            ps.setString(6, cst.getPassword());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new DaoException("Create customer failed", e);
        }
    }

    @Override
    public boolean update(Customer cst) {
        String sql = "UPDATE customer\n" +
                "                SET customer_no=?, customer_name=?, phone=?, address=?, username=?, password=?\n" +
                "                WHERE id=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cst.getCustomerNo());
            ps.setString(2, cst.getCustomerName());
            ps.setString(3, cst.getPhone());
            ps.setString(4, cst.getAddress());
            ps.setString(5, cst.getUsername());
            ps.setString(6, cst.getPassword());
            ps.setInt(7, cst.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DaoException("Update customer failed", e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM customer WHERE id=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DaoException("Delete customer failed", e);
        }
    }

    @Override
    public Optional<Customer> findById(int id) {
        String sql = "SELECT * FROM customer WHERE id=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Find customer by id failed", e);
        }
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        String sql = "SELECT * FROM customer WHERE username=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Find customer by username failed", e);
        }
    }

    @Override
    public List<Customer> listAll() {
        String sql = "SELECT * FROM customer ORDER BY id";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Customer> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new DaoException("List customers failed", e);
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setCustomerNo(rs.getString("customer_no"));
        c.setCustomerName(rs.getString("customer_name"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setUsername(rs.getString("username"));
        c.setPassword(rs.getString("password"));
        return c;
    }
}
