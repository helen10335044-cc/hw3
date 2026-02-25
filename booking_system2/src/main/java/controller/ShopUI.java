package controller;

import com.toedter.calendar.JDateChooser;
import exception.ServiceException;
import model.Product;
import service.OrderService;
import service.ProductImageService;
import service.ProductService;
import vo.LoginUser;
import vo.Page;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class ShopUI extends JFrame {
    private final LoginUser user;
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final ProductImageService productImageService = new ProductImageService();

    private final JTextField txtKeyword = new JTextField();
    private final JButton btnSearch = new JButton("查詢");
    private final JButton btnPrev = new JButton("上一頁");
    private final JButton btnNext = new JButton("下一頁");
    private final JLabel lblPage = new JLabel("Page 1");

    private final JDateChooser dcCheckIn = new JDateChooser();
    private final JDateChooser dcCheckOut = new JDateChooser();

    private final JPanel gridPanel = new JPanel();
    private final JScrollPane scrollPane = new JScrollPane(gridPanel);

    private final Map<Integer, CartItem> cart = new LinkedHashMap<>();

    private int page = 1;
    private final int pageSize = 9;

    public ShopUI(LoginUser user) {
        this.user = user;

        setTitle("前台訂房（卡片+購物車+多圖輪播）- " + user.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        initDefaultDates();
        bindActions();
        bindDateChangeRefresh();

        refresh();
    }

    private JPanel buildTopPanel() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(10, 10, 6, 10));

        JPanel searchRow = new JPanel(new BorderLayout(8, 8));
        searchRow.add(new JLabel("關鍵字(房型代號/名稱)"), BorderLayout.WEST);
        searchRow.add(txtKeyword, BorderLayout.CENTER);
        searchRow.add(btnSearch, BorderLayout.EAST);

        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        dateRow.add(new JLabel("入住"));
        dateRow.add(dcCheckIn);
        dateRow.add(new JLabel("退房"));
        dateRow.add(dcCheckOut);

        JButton btnCart = new JButton("購物車 / 結帳");
        JButton btnMyOrders = new JButton("我的訂單");
        JButton btnLogout = new JButton("登出");

        dateRow.add(btnCart);
        dateRow.add(btnMyOrders);
        dateRow.add(btnLogout);

        top.putClientProperty("btnCart", btnCart);
        top.putClientProperty("btnMyOrders", btnMyOrders);
        top.putClientProperty("btnLogout", btnLogout);

        top.add(searchRow, BorderLayout.NORTH);
        top.add(dateRow, BorderLayout.SOUTH);
        return top;
    }

    private JComponent buildCenterPanel() {
        gridPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        gridPanel.setLayout(new GridLayout(0, 3, 12, 12));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bottom.add(btnPrev);
        bottom.add(lblPage);
        bottom.add(btnNext);
        return bottom;
    }

    private void initDefaultDates() {
        dcCheckIn.setDate(new java.util.Date());
        dcCheckOut.setDate(new java.util.Date(System.currentTimeMillis() + 86400000L));
    }

    private void bindActions() {
        btnSearch.addActionListener(e -> { page = 1; refresh(); });
        btnPrev.addActionListener(e -> { if (page > 1) { page--; refresh(); } });
        btnNext.addActionListener(e -> { page++; refresh(); });

        JPanel top = (JPanel) getContentPane().getComponent(0);
        JButton btnCart = (JButton) top.getClientProperty("btnCart");
        JButton btnMyOrders = (JButton) top.getClientProperty("btnMyOrders");
        JButton btnLogout = (JButton) top.getClientProperty("btnLogout");

        btnCart.addActionListener(e -> openCartDialog());
        btnMyOrders.addActionListener(e -> new MyOrdersUI(user).setVisible(true));
        btnLogout.addActionListener(e -> { new LoginUI().setVisible(true); dispose(); });
    }

    private void bindDateChangeRefresh() {
        if (dcCheckIn.getDateEditor() != null) {
            dcCheckIn.getDateEditor().addPropertyChangeListener("date", evt -> refreshAvailabilityOnly());
        }
        if (dcCheckOut.getDateEditor() != null) {
            dcCheckOut.getDateEditor().addPropertyChangeListener("date", evt -> refreshAvailabilityOnly());
        }
    }

    private void refresh() {
        Page<Product> p = productService.listPaged(txtKeyword.getText(), page, pageSize);
        gridPanel.removeAll();

        for (Product it : p.getItems()) {
            gridPanel.add(new ProductCard(it));
        }

        long totalPages = Math.max(1, p.getTotalPages());
        if (page > totalPages) {
            page = (int) totalPages;
            refresh();
            return;
        }

        lblPage.setText("Page " + page + " / " + totalPages + " (Total " + p.getTotal() + ")");
        btnNext.setEnabled(page < totalPages);
        btnPrev.setEnabled(page > 1);

        gridPanel.revalidate();
        gridPanel.repaint();

        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }

    private void refreshAvailabilityOnly() {
        Component[] comps = gridPanel.getComponents();
        for (Component c : comps) {
            if (c instanceof ProductCard) {
                ((ProductCard) c).updateAvailability();
            }
        }
    }

    private LocalDate getDate(JDateChooser dc) {
        java.util.Date d = dc.getDate();
        if (d == null) return null;
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // ---------------- Cart ----------------
    private static class CartItem {
        final Product product;
        int qty;

        CartItem(Product product, int qty) {
            this.product = product;
            this.qty = qty;
        }
    }

    private void addToCart(Product p, int qty) {
        if (qty <= 0) return;
        CartItem existing = cart.get(p.getId());
        int newQty = (existing == null) ? qty : existing.qty + qty;
        if (newQty > 2) newQty = 2;
        cart.put(p.getId(), new CartItem(p, newQty));
    }

    private void removeFromCart(int productId) { cart.remove(productId); }
    private void clearCart() { cart.clear(); }

    private void openCartDialog() {
        LocalDate in = getDate(dcCheckIn);
        LocalDate out = getDate(dcCheckOut);

        if (in == null || out == null || !in.isBefore(out)) {
            JOptionPane.showMessageDialog(this, "請先選擇正確的入住/退房日期（退房必須晚於入住）", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(this, "購物車 / 結帳", true);
        dlg.setSize(720, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> list = new JList<>(listModel);
        list.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JLabel lblTotal = new JLabel("總計：NT$ 0");
        lblTotal.setBorder(new EmptyBorder(8, 10, 8, 10));

        JButton btnRemove = new JButton("移除選取");
        JButton btnCheckout = new JButton("結帳(下單)");
        JButton btnClose = new JButton("關閉");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actions.add(btnRemove);
        actions.add(btnCheckout);
        actions.add(btnClose);

        dlg.add(new JScrollPane(list), BorderLayout.CENTER);
        dlg.add(lblTotal, BorderLayout.NORTH);
        dlg.add(actions, BorderLayout.SOUTH);

        Runnable refreshCartView = () -> {
            listModel.clear();
            BigDecimal total = BigDecimal.ZERO;

            for (CartItem it : cart.values()) {
                int available = productService.getAvailableStock(it.product.getId(), in, out);
                String warn = (it.qty > available) ? "  [超過剩餘!]" : "";
                listModel.addElement(String.format("%-22s  x %d  每晚:%s  剩餘:%d%s",
                        it.product.getProductName(), it.qty, fmtMoney(it.product.getPricePerNight()), available, warn));

                long nights = java.time.temporal.ChronoUnit.DAYS.between(in, out);
                BigDecimal line = it.product.getPricePerNight()
                        .multiply(BigDecimal.valueOf(it.qty))
                        .multiply(BigDecimal.valueOf(nights));
                total = total.add(line);
            }

            lblTotal.setText("總計：NT$ " + fmtMoney(total));
        };

        refreshCartView.run();

        btnRemove.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx < 0) return;

            Integer removeId = null;
            int i = 0;
            for (Integer pid : cart.keySet()) {
                if (i == idx) { removeId = pid; break; }
                i++;
            }
            if (removeId != null) {
                removeFromCart(removeId);
                refreshCartView.run();
                refreshAvailabilityOnly();
            }
        });

        btnCheckout.addActionListener(e -> {
            try {
                if (cart.isEmpty()) throw new ServiceException("購物車是空的");

                List<OrderService.CartLine> lines = new ArrayList<>();
                for (CartItem it : cart.values()) {
                    int available = productService.getAvailableStock(it.product.getId(), in, out);
                    if (available <= 0) throw new ServiceException("房型【" + it.product.getProductName() + "】在此日期區間已無剩餘");
                    if (it.qty > available) throw new ServiceException("房型【" + it.product.getProductName() + "】剩餘 " + available + " 間，購物車數量過多");
                    lines.add(new OrderService.CartLine(it.product.getId(), it.qty));
                }

                String orderNo = orderService.placeOrder(user.getId(), lines, in, out);
                JOptionPane.showMessageDialog(dlg, "下單成功，訂單編號: " + orderNo);
                clearCart();
                dlg.dispose();
                refreshAvailabilityOnly();

            } catch (ServiceException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "結帳失敗", JOptionPane.ERROR_MESSAGE);
                refreshCartView.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.toString(), "系統錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnClose.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }

    // ---------------- Product Card ----------------
    private class ProductCard extends JPanel {
        private final Product p;

        private final JLabel lblAvail = new JLabel("剩餘：-");
        private final JSpinner spQty;
        private final JButton btnAdd = new JButton("加入購物車");
        private final JButton btnPreview = new JButton("多圖預覽");

        ProductCard(Product p) {
            this.p = p;

            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220)),
                    new EmptyBorder(10, 10, 10, 10)
            ));
            setBackground(Color.WHITE);

            JLabel img = new JLabel();
            img.setHorizontalAlignment(SwingConstants.CENTER);
            img.setIcon(loadIcon(p.getImagePath(), 320, 200)); // 封面圖
            img.setPreferredSize(new Dimension(320, 200));

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            JLabel name = new JLabel(p.getProductName());
            name.setFont(name.getFont().deriveFont(Font.BOLD, 16f));

            JLabel code = new JLabel(p.getProductCode());
            code.setForeground(new Color(90, 90, 90));

            JLabel price = new JLabel("每晚：NT$ " + fmtMoney(p.getPricePerNight()));
            price.setFont(price.getFont().deriveFont(Font.PLAIN, 14f));

            lblAvail.setForeground(new Color(40, 110, 40));

            JLabel stockTotal = new JLabel("總房數： " + p.getStock());
            stockTotal.setForeground(new Color(90, 90, 90));

            JLabel desc = new JLabel(p.getDescription() == null ? "" : p.getDescription());
            desc.setForeground(new Color(120, 120, 120));

            info.add(name);
            info.add(Box.createVerticalStrut(4));
            info.add(code);
            info.add(Box.createVerticalStrut(6));
            info.add(price);
            info.add(stockTotal);
            info.add(lblAvail);
            info.add(Box.createVerticalStrut(6));
            info.add(desc);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            bottom.setOpaque(false);

            bottom.add(new JLabel("數量"));
            spQty = new JSpinner(new SpinnerNumberModel(1, 1, 2, 1));
            ((JSpinner.DefaultEditor) spQty.getEditor()).getTextField().setColumns(2);
            bottom.add(spQty);

            bottom.add(btnAdd);
            bottom.add(btnPreview);

            btnPreview.addActionListener(e -> openCarouselPreview(p));
            btnAdd.addActionListener(e -> onAddToCart());

            add(img, BorderLayout.NORTH);
            add(info, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);

            updateAvailability();
        }

        void updateAvailability() {
            LocalDate in = getDate(dcCheckIn);
            LocalDate out = getDate(dcCheckOut);

            if (in == null || out == null || !in.isBefore(out)) {
                lblAvail.setText("剩餘：-（請選日期）");
                btnAdd.setEnabled(false);
                spQty.setEnabled(false);
                return;
            }

            int available;
            try {
                available = productService.getAvailableStock(p.getId(), in, out);
            } catch (Exception ex) {
                available = 0;
            }

            lblAvail.setText("此區間剩餘： " + available + " 間");

            int maxCanBuy = Math.min(2, Math.max(0, available));
            if (maxCanBuy <= 0) {
                btnAdd.setEnabled(false);
                spQty.setEnabled(false);
                spQty.setValue(1);
            } else {
                btnAdd.setEnabled(true);
                spQty.setEnabled(true);

                int current = (Integer) spQty.getValue();
                SpinnerNumberModel m = (SpinnerNumberModel) spQty.getModel();
                m.setMinimum(1);
                m.setMaximum(maxCanBuy);
                if (current > maxCanBuy) spQty.setValue(maxCanBuy);
                if (current < 1) spQty.setValue(1);
            }
        }

        private void onAddToCart() {
            LocalDate in = getDate(dcCheckIn);
            LocalDate out = getDate(dcCheckOut);
            if (in == null || out == null || !in.isBefore(out)) {
                JOptionPane.showMessageDialog(ShopUI.this, "請先選擇正確日期（退房必須晚於入住）", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int available = productService.getAvailableStock(p.getId(), in, out);
            if (available <= 0) {
                JOptionPane.showMessageDialog(ShopUI.this, "此日期區間已無剩餘房間", "無庫存", JOptionPane.WARNING_MESSAGE);
                updateAvailability();
                return;
            }

            int qty = (Integer) spQty.getValue();
            if (qty > available) {
                JOptionPane.showMessageDialog(ShopUI.this, "剩餘 " + available + " 間，數量過多", "提示", JOptionPane.WARNING_MESSAGE);
                updateAvailability();
                return;
            }

            addToCart(p, qty);
            JOptionPane.showMessageDialog(ShopUI.this, "已加入購物車：" + p.getProductName() + " x " + qty);
            updateAvailability();
        }
    }

    // ---------------- Carousel Preview ----------------
    private void openCarouselPreview(Product p) {
        // 先抓 DB product_images；沒有就 fallback product.image_path
        List<String> paths;
        try {
            paths = productImageService.listPaths(p.getId());
        } catch (Exception ex) {
            paths = new ArrayList<>();
        }

        if (paths == null) paths = new ArrayList<>();
        if (paths.isEmpty() && p.getImagePath() != null && !p.getImagePath().trim().isEmpty()) {
            paths.add(p.getImagePath());
        }
        if (paths.isEmpty()) {
            JOptionPane.showMessageDialog(this, "找不到圖片，請確認 src/main/resources/images 與 DB image_path", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new CarouselDialog(this, p.getProductName(), paths).setVisible(true);
    }

    private class CarouselDialog extends JDialog {
        private final List<String> paths;
        private int idx = 0;

        private final JLabel lblImage = new JLabel();
        private final JLabel lblIndex = new JLabel("1/1", SwingConstants.CENTER);

        private final JButton btnPrev = new JButton("◀ 上一張");
        private final JButton btnNext = new JButton("下一張 ▶");
        private final JToggleButton tglAuto = new JToggleButton("自動播放");
        private final javax.swing.Timer timer;

        CarouselDialog(JFrame owner, String title, List<String> paths) {
            super(owner, title, true);
            this.paths = paths;

            setSize(980, 640);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(10, 10));

            lblImage.setHorizontalAlignment(SwingConstants.CENTER);
            lblImage.setBorder(new EmptyBorder(10, 10, 10, 10));
            add(new JScrollPane(lblImage), BorderLayout.CENTER);

            JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
            controls.add(btnPrev);
            controls.add(lblIndex);
            controls.add(btnNext);
            controls.add(tglAuto);

            JButton btnClose = new JButton("關閉");
            controls.add(btnClose);

            add(controls, BorderLayout.SOUTH);

            btnPrev.addActionListener(e -> showAt(idx - 1));
            btnNext.addActionListener(e -> showAt(idx + 1));
            btnClose.addActionListener(e -> dispose());

            // 3 秒換一張
            timer = new javax.swing.Timer(3000, e -> showAt(idx + 1));
            tglAuto.addActionListener(e -> {
                if (tglAuto.isSelected()) timer.start();
                else timer.stop();
            });

            // 鍵盤左右鍵切換
            getRootPane().registerKeyboardAction(e -> showAt(idx - 1),
                    KeyStroke.getKeyStroke("LEFT"), JComponent.WHEN_IN_FOCUSED_WINDOW);
            getRootPane().registerKeyboardAction(e -> showAt(idx + 1),
                    KeyStroke.getKeyStroke("RIGHT"), JComponent.WHEN_IN_FOCUSED_WINDOW);
            getRootPane().registerKeyboardAction(e -> dispose(),
                    KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

            // 初始顯示
            showAt(0);
        }

        private void showAt(int newIndex) {
            if (paths.isEmpty()) return;
            if (newIndex < 0) newIndex = paths.size() - 1;
            if (newIndex >= paths.size()) newIndex = 0;
            idx = newIndex;

            String path = paths.get(idx);
            ImageIcon icon = loadIcon(path, 900, 560);
            if (icon == null) {
                lblImage.setIcon(null);
                lblImage.setText("圖片找不到: " + path);
            } else {
                lblImage.setText("");
                lblImage.setIcon(icon);
            }
            lblIndex.setText((idx + 1) + " / " + paths.size());
        }

        @Override
        public void dispose() {
            if (timer != null) timer.stop();
            super.dispose();
        }
    }

    // ---------------- Utils ----------------
    private ImageIcon loadIcon(String path, int w, int h) {
        if (path == null || path.trim().isEmpty()) return null;
        java.net.URL url = getClass().getResource(path.startsWith("/") ? path : ("/" + path));
        if (url == null) return null;

        ImageIcon icon = new ImageIcon(url);
        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private String fmtMoney(BigDecimal v) {
        if (v == null) return "0";
        return v.stripTrailingZeros().toPlainString();
    }
}