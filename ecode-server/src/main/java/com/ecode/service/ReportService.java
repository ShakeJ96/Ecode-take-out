package com.ecode.service;

import com.ecode.vo.OrderReportVO;
import com.ecode.vo.TurnoverReportVO;
import com.ecode.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {

    /**
     * 根据时间区间统计营业额
     * @param begin
     * @param end
     * @return
     */

    TurnoverReportVO getTurnover(LocalDate begin, LocalDate end);

    /**
     * 用户数据统计
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUsersData(LocalDate begin, LocalDate end);

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);
}
