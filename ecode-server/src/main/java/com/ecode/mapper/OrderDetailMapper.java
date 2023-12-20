package com.ecode.mapper;

import com.ecode.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单明细数据
     * @param orderddddd
     */
    void insertBatch(List<OrderDetail> orderddddd);
}
