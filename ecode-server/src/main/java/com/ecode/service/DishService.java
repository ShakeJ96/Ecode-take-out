package com.ecode.service;

import com.ecode.dto.DishDTO;
import com.ecode.dto.DishPageQueryDTO;
import com.ecode.entity.Dish;
import com.ecode.result.PageResult;
import com.ecode.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品批量删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     *根据id查询菜品和口味数据
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 添加修改菜品的功能
     * @param dishDTO
     * @return
     */
    void updateWithFlavor(DishDTO dishDTO);
}