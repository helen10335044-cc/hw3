package dao.employee;

import model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeDao {
    int create(Employee e);
    boolean update(Employee e);
    boolean delete(int id);
    Optional<Employee> findById(int id);
    Optional<Employee> findByUsername(String username);
    List<Employee> listAll();
}
