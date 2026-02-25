package service;

import dao.employee.EmployeeDao;
import dao.employee.impl.EmployeeDaoImpl;
import exception.ServiceException;
import model.Employee;
import vo.LoginUser;

import java.util.Optional;

public class EmployeeService {
    private final EmployeeDao employeeDao = new EmployeeDaoImpl();

    public LoginUser login(String username, String password) {
        Optional<Employee> oe = employeeDao.findByUsername(username);
        if (!oe.isPresent()) throw new ServiceException("帳號不存在");
        Employee e = oe.get();
        if (!e.getPassword().equals(password)) throw new ServiceException("密碼錯誤");
        return new LoginUser(LoginUser.Type.EMPLOYEE, e.getId(), e.getUsername(), e.getRole());
    }
}
