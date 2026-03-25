package com.pet.service;

import com.pet.entity.User;
import com.pet.dto.LoginRequest;
import com.pet.dto.RegisterRequest;
import com.pet.dto.ResetPasswordRequest;

import java.util.List;
import java.util.Map;

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

    /**
     * 根据ID获取用户
     */
    User getUserById(Integer id);

    /**
     * 获取用户列表（分页）
     */
    Map<String, Object> getUserList(Integer page, Integer pageSize, String keyword, Integer role, Integer status);

    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Integer id, Integer status);

    /**
     * 更新用户角色
     */
    boolean updateUserRole(Integer id, Integer role);

    /**
     * 删除用户
     */
    boolean deleteUser(Integer id);

    /**
     * 添加用户（管理员）
     */
    boolean addUser(User user);

    /**
     * 更新用户信息（管理员）
     */
    boolean updateUser(User user);
}