package dao.product;

import model.Product;
import vo.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductDao {
    int create(Product p);
    boolean update(Product p);
    boolean delete(int id);
    Optional<Product> findById(int id);
    Optional<Product> findByCode(String code);

    List<Product> listAll();
    Page<Product> listPaged(String keyword, int page, int pageSize);

    int getAvailableStock(int productId, LocalDate checkIn, LocalDate checkOut);
    int getBookedQtyOverlap(int productId, LocalDate checkIn, LocalDate checkOut);
}
