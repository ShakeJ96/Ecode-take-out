package com.ecode.service;

import com.ecode.dto.EmployeeLoginDTO;
import com.ecode.entity.Employee;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

}
