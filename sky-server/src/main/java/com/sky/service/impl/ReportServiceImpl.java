package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private WorkspaceService workspaceService;

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

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        // 用于存放begin - end 范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        while (!(begin.plusDays(1) == end)) {
            dateList.add(begin);
            begin.plusDays(1);
        }

        // 遍历dateList集合区间的有效订单数和订单总数
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 查询每天的订单总数 select count(id) from orders where order_time = ? and order_time < ?
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            // 查每天的有效订单数 select count(id) from orders where order_time = ? and order_time < ? and status = 5
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);

            validOrderCountList.add(validOrderCount);
        }
        // 计算时间区间内的订单总数量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        // 订单时间区间内的有效订单数量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        // 订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / (totalOrderCount);

        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                // 订单总数
                .orderCountList(StringUtils.join(orderCountList, ","))
                .totalOrderCount(totalOrderCount)
                // 有效订单集合
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .validOrderCount(validOrderCount)
                // 订单完成率
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量排名
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出运营数据表
     */
    public void exportBusinessData(HttpServletResponse response) {
        // 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        // 查询概览数据
        BusinessDataVO businessDataVO = workspaceService
                .getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN)
                        , LocalDateTime.of(dateEnd, LocalTime.MIN));

        // 通过POI将数据写入到excel中
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            // 基于模板文件创建一个新的excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            // 获取表格文件的Sheet页
            XSSFSheet excelSheet = excel.getSheet("sheet1");

            // 数据填充--时间
            excelSheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + " - " + dateEnd);

            // 获得第4行
            XSSFRow datarow = excelSheet.getRow(3);
            datarow.getCell(2).setCellValue(businessDataVO.getTurnover());
            datarow.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            datarow.getCell(6).setCellValue(businessDataVO.getNewUsers());

            // 获得第5行
            datarow = excelSheet.getRow(4);
            datarow.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            datarow.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            // 填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                // 查询某一天的营业数据
                BusinessDataVO businessData = workspaceService
                        .getBusinessData(LocalDateTime.of(date, LocalTime.MIN)
                                , LocalDateTime.of(date, LocalTime.MIN));
                // 获得某一行
                datarow = excelSheet.getRow(7 + i);

                datarow.getCell(1).setCellValue(date.toString());
                datarow.getCell(2).setCellValue(businessData.getTurnover());
                datarow.getCell(3).setCellValue(businessData.getValidOrderCount());
                datarow.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                datarow.getCell(5).setCellValue(businessData.getUnitPrice());
                datarow.getCell(6).setCellValue(businessData.getNewUsers());

            }

            // 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            // 关闭资源
            out.close();
            excel.close();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map map = new HashMap();
        map.put("begin", beginTime);
        map.put("end", endTime);
        map.put("status", status);

        return orderMapper.countByMap(map);
    }

}
