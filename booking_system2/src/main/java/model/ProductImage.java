package model;

public class ProductImage {
    private int id;
    private int productId;
    private String imagePath;
    private int sortOrder;

    public ProductImage() {}

    public ProductImage(int id, int productId, String imagePath, int sortOrder) {
        this.id = id;
        this.productId = productId;
        this.imagePath = imagePath;
        this.sortOrder = sortOrder;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}