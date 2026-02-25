package service;

import dao.product_image.ProductImageDao;
import dao.product_image.impl.ProductImageDaoImpl;

import java.util.List;

public class ProductImageService {
    private final ProductImageDao dao = new ProductImageDaoImpl();

    public List<String> listPaths(int productId) {
        return dao.listImagePathsByProductId(productId);
    }
}
