package com.ecode.service.impl;

import com.ecode.constant.MessageConstant;
import com.ecode.constant.PasswordConstant;
import com.ecode.constant.StatusConstant;
import com.ecode.context.BaseContext;
import com.ecode.dto.EmployeeDTO;
import com.ecode.dto.EmployeeLoginDTO;
import com.ecode.dto.EmployeePageQueryDTO;
import com.ecode.entity.Employee;
import com.ecode.exception.AccountLockedException;
import com.ecode.exception.AccountNotFoundException;
import com.ecode.exception.PasswordErrorException;
import com.ecode.mapper.EmployeeMapper;
import com.ecode.result.PageResult;
import com.ecode.service.EmployeeService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jdk.jpackage.internal.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import sun.security.util.Password;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //对前端加入的明文进行md5加密，这里是将数据库中的对应的密码值改为了密文保存，暂且不做细究~
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工的实现类
     * @param employeeDTO
     */

    @Override
    public void save(EmployeeDTO employeeDTO) {
        System.out.println("当前线程的id："+Thread.currentThread().getId());


        Employee employee = new Employee();

        //使用对象的属性拷贝
        BeanUtils.copyProperties(employeeDTO,employee);

        //设置帐号的状态，默认是正常状态
        //1表示正常；0表示锁定
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码，默认的密码是123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置当前记录的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前的记录创建人id和修改人id
        //后期需要改为当前登录的用户id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }


    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //select * from employee limit 0,10
        //pageHelper插件
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        Page<Employee> page=employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total,records);
    }

    /**
     * 启用禁用员工帐号实现类
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // update employee set status =? where id = ?

       /* Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);*/

        Employee employee=Employee.builder()
                        .status(status)
                                .id(id)
                                        .build();


        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee=employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //对象的属性拷贝
        BeanUtils.copyProperties(employeeDTO,employee);
        //更新系统的修改时间
        employee.setUpdateTime(LocalDateTime.now());
        //修改人的id
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }


}
