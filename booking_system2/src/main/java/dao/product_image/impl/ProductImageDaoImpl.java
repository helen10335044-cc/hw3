package dao.product_image.impl;

import dao.product_image.ProductImageDao;
import util.Tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductImageDaoImpl implements ProductImageDao {

    @Override
    public List<String> listImagePathsByProductId(int productId) {
        String sql = "SELECT image_path FROM product_images WHERE product_id=? ORDER BY sort_order ASC, id ASC";
        List<String> list = new ArrayList<>();

        try (Connection conn = new Tool().getDb();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("image_path"));
                }
            }
            return list;

        } catch (Exception e) {
            throw new RuntimeException("List product images failed: " + e.getMessage(), e);
        }
    }
}
