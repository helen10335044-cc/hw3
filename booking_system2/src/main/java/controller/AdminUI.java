package controller;

import model.Order;
import model.OrderItem;
import model.Product;
import service.OrderItemService;
import service.OrderService;
import service.ProductService;
import util.ReportUtil;
import vo.LoginUser;
import vo.Page;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import exception.ServiceException;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AdminUI extends JFrame {
    private final LoginUser user;

    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final OrderItemService orderItemService = new OrderItemService();

    // product tab
    private final JTextField pKeyword = new JTextField();
    private final JButton pSearch = new JButton("查詢");
    private final JButton pPrev = new JButton("上一頁");
    private final JButton pNext = new JButton("下一頁");
    private final JLabel pLblPage = new JLabel("Page 1");
    private int pPage = 1;
    private final int pPageSize = 8;

    private final DefaultTableModel pModel = new DefaultTableModel(
            new Object[]{"ID", "代號", "名稱", "每晚價格", "庫存(房數)", "圖片路徑", "描述"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable pTable = new JTable(pModel);

    // order tab
    private final JTextField oCustomerId = new JTextField();
    private final JComboBox<String> oStatus = new JComboBox<>(new String[]{"", "PAID", "CANCELLED"});
    private final JTextField oFrom = new JTextField(); // yyyy-mm-dd
    private final JTextField oTo = new JTextField();   // yyyy-mm-dd
    private final JButton oSearch = new JButton("查詢");
    private final JButton oPrev = new JButton("上一頁");
    private final JButton oNext = new JButton("下一頁");
    private final JButton oDelete = new JButton("刪除訂單");
    private final JButton oDetail = new JButton("明細");
    private final JLabel oLblPage = new JLabel("Page 1");
    private int oPage = 1;
    private final int oPageSize = 10;

    private final DefaultTableModel oModel = new DefaultTableModel(
            new Object[]{"訂單ID", "訂單編號", "會員ID", "入住", "退房", "總金額", "狀態", "建立時間"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable oTable = new JTable(oModel);

    public AdminUI(LoginUser user) {
        this.user = user;
        setTitle("後台管理 - " + user.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 640);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("產品管理", buildProductPanel());
        tabs.addTab("訂單管理", buildOrderPanel());
        tabs.addTab("報表", buildReportPanel());
        add(tabs);

        refreshProducts();
        refreshOrders();
    }

    private JPanel buildProductPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout(8,8));
        top.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        top.add(new JLabel("關鍵字"), BorderLayout.WEST);
        top.add(pKeyword, BorderLayout.CENTER);
        top.add(pSearch, BorderLayout.EAST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("新增");
        JButton btnEdit = new JButton("修改");
        JButton btnDel = new JButton("刪除");
        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDel);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER));
        nav.add(pPrev);
        nav.add(pLblPage);
        nav.add(pNext);

        JPanel north = new JPanel(new BorderLayout());
        north.add(top, BorderLayout.NORTH);
        north.add(actions, BorderLayout.CENTER);

        panel.add(north, BorderLayout.NORTH);
        panel.add(new JScrollPane(pTable), BorderLayout.CENTER);
        panel.add(nav, BorderLayout.SOUTH);

        pSearch.addActionListener(e -> { pPage = 1; refreshProducts(); });
        pPrev.addActionListener(e -> { if (pPage > 1) { pPage--; refreshProducts(); } });
        pNext.addActionListener(e -> { pPage++; refreshProducts(); });

        btnAdd.addActionListener(e -> editProduct(null));
        btnEdit.addActionListener(e -> {
            int row = pTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "請先選擇一筆"); return; }
            int id = (Integer) pModel.getValueAt(row, 0);
            Product p = productService.findById(id).orElse(null);
            editProduct(p);
        });
        btnDel.addActionListener(e -> {
            int row = pTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "請先選擇一筆"); return; }
            int id = (Integer) pModel.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "確定刪除?") == JOptionPane.OK_OPTION) {
                productService.delete(id);
                refreshProducts();
            }
        });

        return panel;
    }

    private JPanel buildOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(2, 1));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("會員ID"));
        oCustomerId.setColumns(6);
        filters.add(oCustomerId);
        filters.add(new JLabel("狀態"));
        filters.add(oStatus);
        filters.add(new JLabel("入住>= (yyyy-mm-dd)"));
        oFrom.setColumns(10);
        filters.add(oFrom);
        filters.add(new JLabel("退房<= (yyyy-mm-dd)"));
        oTo.setColumns(10);
        filters.add(oTo);
        filters.add(oSearch);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(oDetail);
        actions.add(oDelete);

        top.add(filters);
        top.add(actions);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER));
        nav.add(oPrev);
        nav.add(oLblPage);
        nav.add(oNext);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(oTable), BorderLayout.CENTER);
        panel.add(nav, BorderLayout.SOUTH);

        oSearch.addActionListener(e -> { oPage = 1; refreshOrders(); });
        oPrev.addActionListener(e -> { if (oPage > 1) { oPage--; refreshOrders(); } });
        oNext.addActionListener(e -> { oPage++; refreshOrders(); });

        oDelete.addActionListener(e -> onDeleteOrder());
        oDetail.addActionListener(e -> onOrderDetail());

        return panel;
    }

    private JPanel buildReportPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnProductPdf = new JButton("列印產品報表(PDF)");
        JButton btnOrderPdf = new JButton("列印訂單報表(PDF)");
        JButton btnLogout = new JButton("登出");
        panel.add(btnProductPdf);
        panel.add(btnOrderPdf);
        panel.add(btnLogout);

        btnProductPdf.addActionListener(e -> {
            try {
                String out = ReportUtil.exportProductReportToPdf();
                JOptionPane.showMessageDialog(this, "已輸出: " + out);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.toString(), "報表失敗", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnOrderPdf.addActionListener(e -> {
            try {
                String out = ReportUtil.exportOrderReportToPdf();
                JOptionPane.showMessageDialog(this, "已輸出: " + out);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.toString(), "報表失敗", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnLogout.addActionListener(e -> { new LoginUI().setVisible(true); dispose(); });

        return panel;
    }

    private void refreshProducts() {
        Page<Product> p = productService.listPaged(pKeyword.getText(), pPage, pPageSize);
        pModel.setRowCount(0);
        for (Product it : p.getItems()) {
            pModel.addRow(new Object[]{
                    it.getId(), it.getProductCode(), it.getProductName(), it.getPricePerNight(),
                    it.getStock(), it.getImagePath(), it.getDescription()
            });
        }
        long totalPages = Math.max(1, p.getTotalPages());
        if (pPage > totalPages) { pPage = (int) totalPages; refreshProducts(); return; }
        pLblPage.setText("Page " + pPage + " / " + totalPages);
        pNext.setEnabled(pPage < totalPages);
        pPrev.setEnabled(pPage > 1);
    }

    private void refreshOrders() {
        Integer cid = null;
        if (!oCustomerId.getText().trim().isEmpty()) {
            try { cid = Integer.parseInt(oCustomerId.getText().trim()); } catch (NumberFormatException ignore) {}
        }
        String status = (String) oStatus.getSelectedItem();
        LocalDate from = parseDate(oFrom.getText().trim());
        LocalDate to = parseDate(oTo.getText().trim());

        Page<Order> p = orderService.listOrders(cid, status, from, to, oPage, oPageSize);
        oModel.setRowCount(0);
        for (Order it : p.getItems()) {
            oModel.addRow(new Object[]{
                    it.getId(), it.getOrderNo(), it.getCustomerId(),
                    it.getCheckInDate(), it.getCheckOutDate(),
                    it.getTotalAmount(), it.getStatus(), it.getCreatedAt()
            });
        }
        long totalPages = Math.max(1, p.getTotalPages());
        if (oPage > totalPages) { oPage = (int) totalPages; refreshOrders(); return; }
        oLblPage.setText("Page " + oPage + " / " + totalPages);
        oNext.setEnabled(oPage < totalPages);
        oPrev.setEnabled(oPage > 1);
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    private void editProduct(Product p) {
        boolean isNew = (p == null);
        JTextField fCode = new JTextField(isNew ? "" : p.getProductCode());
        JTextField fName = new JTextField(isNew ? "" : p.getProductName());
        JTextField fPrice = new JTextField(isNew ? "3000" : p.getPricePerNight().toPlainString());
        JTextField fStock = new JTextField(isNew ? "2" : String.valueOf(p.getStock()));
        JTextField fImg = new JTextField(isNew ? "/images/sample.jpg" : p.getImagePath());
        JTextField fDesc = new JTextField(isNew ? "" : (p.getDescription() == null ? "" : p.getDescription()));

        JPanel panel = new JPanel(new GridLayout(0,2,8,8));
        panel.add(new JLabel("代號(英文)")); panel.add(fCode);
        panel.add(new JLabel("名稱")); panel.add(fName);
        panel.add(new JLabel("每晚價格")); panel.add(fPrice);
        panel.add(new JLabel("庫存(房數)")); panel.add(fStock);
        panel.add(new JLabel("圖片路徑(classpath)")); panel.add(fImg);
        panel.add(new JLabel("描述")); panel.add(fDesc);

        int ok = JOptionPane.showConfirmDialog(this, panel, isNew ? "新增產品" : "修改產品", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            Product x = isNew ? new Product() : p;
            x.setProductCode(fCode.getText().trim());
            x.setProductName(fName.getText().trim());
            x.setPricePerNight(new BigDecimal(fPrice.getText().trim()));
            x.setStock(Integer.parseInt(fStock.getText().trim()));
            x.setImagePath(fImg.getText().trim());
            x.setDescription(fDesc.getText().trim());

            if (x.getProductCode().trim().isEmpty() || x.getProductName().trim().isEmpty()) throw new ServiceException("代號/名稱必填");
            if (x.getStock() <= 0) throw new ServiceException("庫存必須大於 0");

            if (isNew) productService.create(x);
            else productService.update(x);

            refreshProducts();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "儲存失敗", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteOrder() {
        int row = oTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "請先選擇一筆訂單"); return; }
        int id = (Integer) oModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "確定刪除此訂單?") == JOptionPane.OK_OPTION) {
            orderService.deleteOrder(id);
            refreshOrders();
        }
    }

    private void onOrderDetail() {
        int row = oTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "請先選擇一筆訂單"); return; }
        int orderId = (Integer) oModel.getValueAt(row, 0);

        java.util.List<OrderItem> items = orderItemService.listByOrderId(orderId);
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
