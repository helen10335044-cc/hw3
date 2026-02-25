package controller;

import exception.ServiceException;
import service.CustomerService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RegisterUI extends JDialog {
    private final JTextField txtName = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtAddress = new JTextField();
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();

    private final CustomerService customerService = new CustomerService();

    public RegisterUI(JFrame owner) {
        super(owner, "會員註冊", true);
        setSize(420, 320);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        form.add(new JLabel("姓名"));
        form.add(txtName);
        form.add(new JLabel("電話"));
        form.add(txtPhone);
        form.add(new JLabel("地址"));
        form.add(txtAddress);
        form.add(new JLabel("帳號"));
        form.add(txtUsername);
        form.add(new JLabel("密碼"));
        form.add(txtPassword);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("註冊");
        JButton btnCancel = new JButton("取消");
        btns.add(btnCancel);
        btns.add(btnOk);

        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnOk.addActionListener(this::onRegister);
    }

    private void onRegister(ActionEvent e) {
        try {
            int id = customerService.register(
                    txtName.getText().trim(),
                    txtPhone.getText().trim(),
                    txtAddress.getText().trim(),
                    txtUsername.getText().trim(),
                    new String(txtPassword.getPassword())
            );
            JOptionPane.showMessageDialog(this, "註冊成功，會員ID=" + id);
            dispose();
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "註冊失敗", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.toString(), "系統錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }
}
