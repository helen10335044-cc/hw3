package dao.employee.impl;

import dao.employee.EmployeeDao;
import exception.DaoException;
import model.Employee;
import util.Tool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDaoImpl implements EmployeeDao {
    private final Tool conn = new Tool();

    @Override
    public int create(Employee emp) {
        String sql = "INSERT INTO employee(employee_no, employee_name, phone, address, username, password, role)\n" +
                "                VALUES(?,?,?,?,?,?,?)";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, emp.getEmployeeNo());
            ps.setString(2, emp.getEmployeeName());
            ps.setString(3, emp.getPhone());
            ps.setString(4, emp.getAddress());
            ps.setString(5, emp.getUsername());
            ps.setString(6, emp.getPassword());
            ps.setString(7, emp.getRole());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new DaoException("Create employee failed", e);
        }
    }

    @Override
    public boolean update(Employee emp) {
        String sql = "UPDATE employee\n" +
                "                SET employee_no=?, employee_name=?, phone=?, address=?, username=?, password=?, role=?\n" +
                "                WHERE id=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, emp.getEmployeeNo());
            ps.setString(2, emp.getEmployeeName());
            ps.setString(3, emp.getPhone());
            ps.setString(4, emp.getAddress());
            ps.setString(5, emp.getUsername());
            ps.setString(6, emp.getPassword());
            ps.setString(7, emp.getRole());
            ps.setInt(8, emp.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DaoException("Update employee failed", e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM employee WHERE id=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DaoException("Delete employee failed", e);
        }
    }

    @Override
    public Optional<Employee> findById(int id) {
        String sql = "SELECT * FROM employee WHERE id=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Find employee by id failed", e);
        }
    }

    @Override
    public Optional<Employee> findByUsername(String username) {
        String sql = "SELECT * FROM employee WHERE username=?";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Find employee by username failed", e);
        }
    }

    @Override
    public List<Employee> listAll() {
        String sql = "SELECT * FROM employee ORDER BY id";
        try (Connection c = conn.getDb();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Employee> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new DaoException("List employees failed", e);
        }
    }

    private Employee map(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("id"));
        e.setEmployeeNo(rs.getString("employee_no"));
        e.setEmployeeName(rs.getString("employee_name"));
        e.setPhone(rs.getString("phone"));
        e.setAddress(rs.getString("address"));
        e.setUsername(rs.getString("username"));
        e.setPassword(rs.getString("password"));
        e.setRole(rs.getString("role"));
        return e;
    }
}
