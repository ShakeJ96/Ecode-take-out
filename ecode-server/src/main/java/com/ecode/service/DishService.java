package com.ecode.service;

import com.ecode.dto.DishDTO;
import com.ecode.entity.Dish;

public interface DishService {
    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);
}
