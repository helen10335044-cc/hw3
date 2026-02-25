package model;

import java.math.BigDecimal;

public class OrderItem {
    private Integer id;
    private Integer orderId;
    private Integer productId;
    private int quantity; // rooms count
    private BigDecimal unitPricePerNight;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPricePerNight() { return unitPricePerNight; }
    public void setUnitPricePerNight(BigDecimal unitPricePerNight) { this.unitPricePerNight = unitPricePerNight; }
}
