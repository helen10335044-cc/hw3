package dao.product_image;

import java.util.List;

public interface ProductImageDao {
    List<String> listImagePathsByProductId(int productId);
}