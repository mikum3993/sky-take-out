package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * @program: sky-take-out
 * @description:
 * @author: {}
 * @create: 2024/4/22 20:08
 */
public interface ReportService {

    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
}
