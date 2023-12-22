package com.ecode.service;

import com.ecode.vo.TurnoverReportVO;

import java.time.LocalDate;

public interface ReportService {

    /**
     * 根据时间区间统计营业额
     * @param begin
     * @param end
     * @return
     */

    TurnoverReportVO getTurnover(LocalDate begin, LocalDate end);
}
