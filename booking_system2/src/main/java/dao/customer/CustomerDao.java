package dao.customer;

import model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerDao {
    int create(Customer c);
    boolean update(Customer c);
    boolean delete(int id);
    Optional<Customer> findById(int id);
    Optional<Customer> findByUsername(String username);
    List<Customer> listAll();
}
