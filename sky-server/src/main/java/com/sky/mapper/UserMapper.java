package com.sky.mapper;

import com.sky.annotaion.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * @program: sky-take-out
 * @description:
 * @author: ${}
 * @create: 2024/4/15 21:42
 */
@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     *
     * @param openid
     * @return
     */
    @Select("select * from user where openid=#{openid}")
    User getByOpenId(String openid);


    void insert(User user);

    @Select("select * from user where openid=#{userId}")
    User getById(Long userId);

    /**
     * 根据动态条件统计用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
