package com.ecode.mapper;

import com.ecode.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     */
    void insert(Orders orders);
}
