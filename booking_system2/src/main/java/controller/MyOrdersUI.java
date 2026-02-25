package controller;

import model.Order;
import model.OrderItem;
import model.Product;
import service.OrderItemService;
import service.OrderService;
import service.ProductService;
import vo.LoginUser;
import vo.Page;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MyOrdersUI extends JFrame {
    private final LoginUser user;
    private final OrderService orderService = new OrderService();
    private final OrderItemService itemService = new OrderItemService();
    private final ProductService productService = new ProductService();

    private int page = 1;
    private final int pageSize = 8;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"訂單ID", "訂單編號", "入住", "退房", "總金額", "狀態", "建立時間"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JLabel lblPage = new JLabel("Page 1");
    private final JButton btnPrev = new JButton("上一頁");
    private final JButton btnNext = new JButton("下一頁");
    private final JButton btnDetail = new JButton("明細");

    public MyOrdersUI(LoginUser user) {
        this.user = user;
        setTitle("我的訂單 - " + user.getUsername());
        setSize(880, 420);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(btnPrev);
        bottom.add(lblPage);
        bottom.add(btnNext);
        bottom.add(btnDetail);
        add(bottom, BorderLayout.SOUTH);

        btnPrev.addActionListener(e -> { if (page > 1) { page--; refresh(); } });
        btnNext.addActionListener(e -> { page++; refresh(); });
        btnDetail.addActionListener(e -> showDetail());

        refresh();
    }

    private void refresh() {
        Page<Order> p = orderService.listOrders(user.getId(), null, null, null, page, pageSize);
        model.setRowCount(0);
        for (Order o : p.getItems()) {
            model.addRow(new Object[]{o.getId(), o.getOrderNo(), o.getCheckInDate(), o.getCheckOutDate(), o.getTotalAmount(), o.getStatus(), o.getCreatedAt()});
        }
        long totalPages = Math.max(1, p.getTotalPages());
        if (page > totalPages) { page = (int) totalPages; refresh(); return; }
        lblPage.setText("Page " + page + " / " + totalPages);
        btnNext.setEnabled(page < totalPages);
        btnPrev.setEnabled(page > 1);
    }

    private void showDetail() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "請先選擇訂單"); return; }
        int orderId = (Integer) model.getValueAt(row, 0);

        java.util.List<OrderItem> items = itemService.listByOrderId(orderId);
        StringBuilder sb = new StringBuilder();
        sb.append("訂單ID: ").append(orderId).append("\n\n");
        for (OrderItem it : items) {
            Product p = productService.findById(it.getProductId()).orElse(null);
            sb.append("- ").append(p == null ? ("product#" + it.getProductId()) : p.getProductName())
              .append(" x ").append(it.getQuantity())
              .append(" (每晚 ").append(it.getUnitPricePerNight()).append(")\n");
        }
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "訂單明細", JOptionPane.INFORMATION_MESSAGE);
    }
}
