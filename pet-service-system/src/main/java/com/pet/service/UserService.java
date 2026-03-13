package com.pet.service;

import com.pet.entity.User;
import com.pet.dto.LoginRequest;
import com.pet.dto.RegisterRequest;
import com.pet.dto.ResetPasswordRequest;

public interface UserService {

    /**
     * 用户登录
     */
    User login(LoginRequest request);

    /**
     * 用户注册
     */
    boolean register(RegisterRequest request);

    /**
     * 重置密码
     */
    boolean resetPassword(ResetPasswordRequest request);

    /**
     * 检查用户名是否存在
     */
    boolean checkUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean checkEmail(String email);
}