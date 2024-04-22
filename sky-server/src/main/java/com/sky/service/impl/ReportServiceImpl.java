package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: sky-take-out
 * @description:
 * @author: {}
 * @create: 2024/4/22 20:10
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 用于存放begin - end 范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        while (!(begin.plusDays(1) == end)) {
            dateList.add(begin);
            begin.plusDays(1);
        }

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 状态为"已完成"的订单金额的合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // select sum(amount) from orders where order_time > beginTime and order_time < endTime and status=5
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }


        return TurnoverReportVO
                .builder()
                .dateList(StringUtils
                        .join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 用于存放begin - end 范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        while (!(begin.plusDays(1) == end)) {
            dateList.add(begin);
            begin.plusDays(1);
        }
        // 每天的新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        // select sum(totaluser) from orders where create_time > beginTime and create_time < endTime and status=1
        // 存放每天的总用户数量 select count(id) from user where create_from < ?
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            // 状态为"已完成"的订单金额的合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // select sum(totaluser) from orders where order_time > beginTime and order_time < endTime and status=1
            Map map = new HashMap();
            map.put("end", endTime);
            // 用户总量
            Integer totalUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);

            map.put("begin", beginTime);
            // 新增用户
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }
}
