package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @program: sky-take-out
 * @description: 套餐mapper
 * @author: ${}
 * @create: 2024/4/13 21:45
 */
@Mapper
public interface SetmealDishMapper {


    /**
     * 根据菜品id查询套餐id
     *
     * @param dishIds
     * @return
     */
    List<Long> getSetmealDishIds(List<Long> dishIds);
}
