package service;

import dao.customer.CustomerDao;
import dao.customer.impl.CustomerDaoImpl;
import exception.ServiceException;
import model.Customer;
import vo.LoginUser;

import java.util.Optional;
import java.util.UUID;

public class CustomerService {
    private final CustomerDao customerDao = new CustomerDaoImpl();

    public LoginUser login(String username, String password) {
        Optional<Customer> oc = customerDao.findByUsername(username);
        if (!oc.isPresent()) throw new ServiceException("帳號不存在");
        Customer c = oc.get();
        if (!c.getPassword().equals(password)) throw new ServiceException("密碼錯誤");
        return new LoginUser(LoginUser.Type.CUSTOMER, c.getId(), c.getUsername(), "CUSTOMER");
    }

    public int register(String name, String phone, String address, String username, String password) {
        if (customerDao.findByUsername(username).isPresent()) throw new ServiceException("此帳號已存在");
        Customer c = new Customer();
        c.setCustomerNo("C-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        c.setCustomerName(name);
        c.setPhone(phone);
        c.setAddress(address);
        c.setUsername(username);
        c.setPassword(password);
        return customerDao.create(c);
    }
}
