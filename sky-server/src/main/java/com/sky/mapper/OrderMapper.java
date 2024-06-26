package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @program: sky-take-out
 * @description: 订单表
 * @author: ${}
 * @create: 2024/4/18 21:43
 */
@Mapper
public interface OrderMapper {

    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据订单状态和时间
     *
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status=#{status} and order_time<(#{orderTime})")
    List<Orders> getByStatusAndTimeLT(Integer status, LocalDateTime orderTime);

    @Select("select * from orders where id=#{id} ")
    Orders getById(Long id);


    Double sumByMap(Map map);

    Integer countByMap(Map map);

    // 销量排名统计
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
