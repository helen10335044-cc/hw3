package controller;

import exception.ServiceException;
import service.CustomerService;
import service.EmployeeService;
import vo.LoginUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginUI extends JFrame {
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JComboBox<String> cbType = new JComboBox<>(new String[]{"會員(Customer)", "員工(Employee)"});
    private final CustomerService customerService = new CustomerService();
    private final EmployeeService employeeService = new EmployeeService();

    public LoginUI() {
        setTitle("Booking System2 - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        form.add(new JLabel("登入身份"));
        form.add(cbType);
        form.add(new JLabel("帳號"));
        form.add(txtUsername);
        form.add(new JLabel("密碼"));
        form.add(txtPassword);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogin = new JButton("登入");
        JButton btnRegister = new JButton("會員註冊");
        btns.add(btnRegister);
        btns.add(btnLogin);

        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        btnLogin.addActionListener(this::onLogin);
        btnRegister.addActionListener(e -> new RegisterUI(this).setVisible(true));
    }

    private void onLogin(ActionEvent e) {
        try {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            if (username.trim().isEmpty() || password.trim().isEmpty()) throw new ServiceException("請輸入帳號與密碼");

            LoginUser user;
            if (cbType.getSelectedIndex() == 0) {
                user = customerService.login(username, password);
                JOptionPane.showMessageDialog(this, "登入成功(會員): " + user.getUsername());
                new ShopUI(user).setVisible(true);
            } else {
                user = employeeService.login(username, password);
                if (!user.isAdmin()) throw new ServiceException("此帳號沒有管理員權限");
                JOptionPane.showMessageDialog(this, "登入成功(管理員): " + user.getUsername());
                new AdminUI(user).setVisible(true);
            }
            dispose();
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "登入失敗", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.toString(), "系統錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}
