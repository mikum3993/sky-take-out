package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * @program: sky-take-out
 * @description: 微信登录
 * @author: ${}
 * @create: 2024/4/15 21:16
 */
public interface UserServer {

    /**
     * 微信登录功能
     * @param userLoginDTO
     * @return
     */
    User wxlogin(UserLoginDTO userLoginDTO);
}
