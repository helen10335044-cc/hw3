package dao.order;

import model.Order;
import vo.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderDao {
    int create(Order o);
    boolean delete(int id);
    Optional<Order> findById(int id);
    Optional<Order> findByOrderNo(String orderNo);
    List<Order> listByCustomer(int customerId);

    Page<Order> listPaged(Integer customerId, String status, LocalDate from, LocalDate to, int page, int pageSize);

    boolean existsCustomerProductOverlap(int customerId, int productId, LocalDate checkIn, LocalDate checkOut);
}
