package com.ecode.service.impl;

import com.ecode.entity.Orders;
import com.ecode.mapper.OrderMapper;
import com.ecode.mapper.UserMapper;
import com.ecode.service.ReportService;
import com.ecode.vo.OrderReportVO;
import com.ecode.vo.TurnoverReportVO;
import com.ecode.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据时间区间统计营业额
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)){
            begin = begin.plusDays(1);//日期计算，获得指定日期后1天的日期
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("status", Orders.COMPLETED);
            map.put("begin",beginTime);
            map.put("end", endTime);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        //数据封装
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }


    /**
     * 用户数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUsersData(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);

        while(!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        //新增的用户数量
        List<Integer> newUserList=new ArrayList<>();
        //总用户数量
        List<Integer> totalUserList=new ArrayList<>();

        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 新增用户数量
            // select count(id) from user where create_time>? and create_time <?
            Integer newUser = getUserCount(beginTime, endTime);
            //总用户数量 select count(id) from user where  create_time < ?
            Integer totalUser=getUserCount(null,endTime);

            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }


    /**
     * 根据时间区间统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);

        while(!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        //每天订单总数集合
        List<Integer> orderCountList = new ArrayList<>();

        //每天的有效订单数的集合
        List<Integer> validOrderCountList=new ArrayList<>();

        for(LocalDate date:dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // 查询每天的总订单数
            // select count(id) from orders where order_time >? and order_time<?
            Integer orderCount = getOrderCount(beginTime, endTime,Orders.COMPLETED);

            //查询每天的有效订单数
            // select count(id) from orders where order_time>? and order_time<? and status=?
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
            }

            //特定时间区间中的总订单数和总有效订单数
            Integer totalOrderCount=0;
            for(Integer t:orderCountList){
                totalOrderCount+=t;
            }
//            Integer totalOrderCount=orderCountList.stream().reduce(Integer::sum).get();
            Integer validorderCount = validOrderCountList.stream().reduce(Integer::sum).get();


            //订单完成率
            Double orderCompletionRate = 0.0;
            if(totalOrderCount != 0){
                orderCompletionRate = validorderCount.doubleValue() / totalOrderCount;
            }
            return OrderReportVO.builder()
                    .dateList(StringUtils.join(dateList, ","))
                    .orderCountList(StringUtils.join(orderCountList, ","))
                    .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                    .totalOrderCount(totalOrderCount)
                    .validOrderCount(validorderCount)
                    .orderCompletionRate(orderCompletionRate)
                    .build();

    }

    /**
     * 根据时间区间统计制定状态的订单数量
     * @param beginTime
     * @param endTime
     * @return
     */
    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("status",status);
        map.put("begin",beginTime);
        map.put("end",endTime);

        return orderMapper.countByMap(map);
    }

    /**
     * 根据时间区间统计用户数量
     * @param beginTime
     * @param endTime
     * @return
     */
    private Integer getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        Map map=new HashMap();
        map.put("begin",beginTime);
        map.put("end",endTime);
        return userMapper.countByMap(map);
    }

}