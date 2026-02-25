package dao.order_item;

import model.OrderItem;

import java.util.List;

public interface OrderItemDao {
    int create(OrderItem item);
    List<OrderItem> listByOrderId(int orderId);
    boolean deleteByOrderId(int orderId);
}
