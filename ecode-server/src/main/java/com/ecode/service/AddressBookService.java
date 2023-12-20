package com.ecode.service;

import com.ecode.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    /**
     * 查询当前登录用户的所有地址信息
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 新增地址
     * @param addressBook
     */
    void save(AddressBook addressBook);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    AddressBook getById(Long id);

    /**
     * 根据id修改地址
     * @param addressBook
     * @return
     */
    void update(AddressBook addressBook);

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    void setDefault(AddressBook addressBook);

    /**
     * 根据id删除地址
     *
     * @param id
     * @return
     */
    void deleteById(Long id);
}
