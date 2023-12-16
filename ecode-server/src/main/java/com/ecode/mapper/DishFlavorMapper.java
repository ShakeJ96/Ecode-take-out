package com.ecode.mapper;

import com.ecode.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味数据
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品的id删除对应的风味数据
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id=#{dishId}")
    void deleteByDishId(Long dishId);

    @Select("select * from dish_flavor where dish_id=#{dishid}")
    List<DishFlavor> getByDishId(Long dishId);

    /**
     * 根据菜品id集合删除关联的口味数据
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);
}
