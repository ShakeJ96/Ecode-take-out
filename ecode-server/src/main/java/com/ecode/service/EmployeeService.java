package com.ecode.service;

import com.ecode.dto.EmployeeDTO;
import com.ecode.dto.EmployeeLoginDTO;
import com.ecode.dto.EmployeePageQueryDTO;
import com.ecode.dto.PasswordEditDTO;
import com.ecode.entity.Employee;
import com.ecode.result.PageResult;

import javax.servlet.http.HttpServletRequest;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工的实现类
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);


    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    void update(EmployeeDTO employeeDTO);

    /**
     * 修改密码
     * @param passwordEditDTO
     */
    void editPassword(PasswordEditDTO passwordEditDTO);
}
