package model;

import java.math.BigDecimal;

public class Product {
    private Integer id;
    private String productCode;   // English code
    private String productName;   // display name
    private BigDecimal pricePerNight;
    private int stock;            // total rooms (e.g., 2)
    private String imagePath;     // classpath resource like "/images/xxx.jpg"
    private String description;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
