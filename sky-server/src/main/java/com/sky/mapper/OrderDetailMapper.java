package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

/**
 * @program: sky-take-out
 * @description: 订单明细表
 * @author: ${}
 * @create: 2024/4/18 21:43
 */
@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单映射数据
     * @param orderDetailList
     */
    void insertBatch(ArrayList<OrderDetail> orderDetailList);
}
