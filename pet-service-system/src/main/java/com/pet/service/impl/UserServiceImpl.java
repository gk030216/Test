package com.pet.service.impl;

import com.pet.entity.User;
import com.pet.entity.VerificationCode;
import com.pet.dto.LoginRequest;
import com.pet.dto.RegisterRequest;
import com.pet.dto.ResetPasswordRequest;
import com.pet.mapper.UserMapper;
import com.pet.mapper.VerificationCodeMapper;
import com.pet.service.UserService;
import com.pet.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationCodeMapper verificationCodeMapper;

    @Override
    public User login(LoginRequest request) {
        // 根据账号（用户名或邮箱）查询用户
        User user = userMapper.findByUsernameOrEmail(request.getAccount());

        if (user != null) {
            // 验证密码
            if (MD5Util.verify(request.getPassword(), user.getPassword())) {
                // 检查用户状态
                if (user.getStatus() == 1) {
                    // 更新最后登录时间
                    userMapper.updateLastLoginTime(user.getId());
                    return user;
                } else {
                    throw new RuntimeException("账号已被禁用");
                }
            }
        }
        throw new RuntimeException("用户名或密码错误");
    }

    @Override
    @Transactional
    public boolean register(RegisterRequest request) {
        // 验证验证码
        VerificationCode validCode = verificationCodeMapper.findLatestValidCode(
                request.getEmail(), 1); // 1表示注册类型

        if (validCode == null || !validCode.getCode().equals(request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 检查用户名是否已存在
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(MD5Util.encrypt(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole(1); // 默认普通用户
        user.setStatus(1); // 默认正常

        int result = userMapper.insert(user);

        if (result > 0) {
            // 将验证码标记为已使用
            verificationCodeMapper.markAsUsed(validCode.getId());
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        // 验证验证码
        VerificationCode validCode = verificationCodeMapper.findLatestValidCode(
                request.getEmail(), 2); // 2表示找回密码类型

        if (validCode == null || !validCode.getCode().equals(request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 检查邮箱是否存在
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null) {
            throw new RuntimeException("该邮箱未注册");
        }

        // 更新密码
        String encryptedPassword = MD5Util.encrypt(request.getNewPassword());
        int result = userMapper.updatePassword(request.getEmail(), encryptedPassword);

        if (result > 0) {
            // 将验证码标记为已使用
            verificationCodeMapper.markAsUsed(validCode.getId());
            return true;
        }

        return false;
    }

    @Override
    public boolean checkUsername(String username) {
        return userMapper.findByUsername(username) != null;
    }

    @Override
    public boolean checkEmail(String email) {
        return userMapper.findByEmail(email) != null;
    }
}