package service;

import dao.product.ProductDao;
import dao.product.impl.ProductDaoImpl;
import model.Product;
import vo.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ProductService {
    private final ProductDao dao = new ProductDaoImpl();

    public int create(Product p) { return dao.create(p); }
    public boolean update(Product p) { return dao.update(p); }
    public boolean delete(int id) { return dao.delete(id); }
    public Optional<Product> findById(int id) { return dao.findById(id); }
    public List<Product> listAll() { return dao.listAll(); }
    public Page<Product> listPaged(String keyword, int page, int pageSize) { return dao.listPaged(keyword, page, pageSize); }
    public int getAvailableStock(int productId, LocalDate checkIn, LocalDate checkOut) { return dao.getAvailableStock(productId, checkIn, checkOut); }
}
