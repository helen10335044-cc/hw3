package service;

import dao.order_item.OrderItemDao;
import dao.order_item.impl.OrderItemDaoImpl;
import model.OrderItem;

import java.util.List;

public class OrderItemService {
    private final OrderItemDao dao = new OrderItemDaoImpl();

    public List<OrderItem> listByOrderId(int orderId) {
        return dao.listByOrderId(orderId);
    }
}
