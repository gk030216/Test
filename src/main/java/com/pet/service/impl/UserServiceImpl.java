package com.pet.service.impl;

import com.pet.entity.User;
import com.pet.entity.VerificationCode;
import com.pet.dto.LoginRequest;
import com.pet.dto.RegisterRequest;
import com.pet.dto.ResetPasswordRequest;
import com.pet.mapper.UserMapper;
import com.pet.mapper.VerificationCodeMapper;
import com.pet.service.NotificationService;
import com.pet.service.UserService;
import com.pet.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationCodeMapper verificationCodeMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public User login(LoginRequest request) {
        User user = userMapper.findByUsernameOrEmail(request.getAccount());

        if (user != null) {
            if (MD5Util.verify(request.getPassword(), user.getPassword())) {
                if (user.getStatus() == 1) {
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
        VerificationCode validCode = verificationCodeMapper.findLatestValidCode(
                request.getEmail(), 1);

        if (validCode == null || !validCode.getCode().equals(request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(MD5Util.encrypt(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setGender(request.getGender() != null ? request.getGender() : "保密");
        user.setRole(1);
        user.setStatus(1);

        int result = userMapper.insert(user);

        if (result > 0) {
            verificationCodeMapper.markAsUsed(validCode.getId());

            // ✅ 发送站内消息通知
            notificationService.sendNotification(
                    user.getId(),
                    "system",
                    "欢迎注册",
                    "欢迎加入宠物服务系统！在这里你可以预约服务、购买商品、分享养宠经验。",
                    "/"
            );

            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        VerificationCode validCode = verificationCodeMapper.findLatestValidCode(
                request.getEmail(), 2);

        if (validCode == null || !validCode.getCode().equals(request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        User user = userMapper.findByEmail(request.getEmail());
        if (user == null) {
            throw new RuntimeException("该邮箱未注册");
        }

        String encryptedPassword = MD5Util.encrypt(request.getNewPassword());
        int result = userMapper.updatePassword(request.getEmail(), encryptedPassword);

        if (result > 0) {
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

    @Override
    public User getUserById(Integer id) {
        return userMapper.findById(id);
    }

    @Override
    public Map<String, Object> getUserList(Integer page, Integer pageSize, String keyword, Integer role, Integer status) {
        int offset = (page - 1) * pageSize;
        List<User> userList = userMapper.searchUsers(keyword, role, status, offset, pageSize);
        int total = userMapper.countSearch(keyword, role, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", userList);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);

        return result;
    }

    @Override
    public boolean updateUserStatus(Integer id, Integer status) {
        return userMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean updateUserRole(Integer id, Integer role) {
        return userMapper.updateRole(id, role) > 0;
    }

    @Override
    public boolean deleteUser(Integer id) {
        // 不能删除管理员账号
        User user = userMapper.findById(id);
        if (user != null && user.getRole() == 3) {
            throw new RuntimeException("不能删除管理员账号");
        }
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public boolean addUser(User user) {
        // 检查用户名是否存在
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 检查邮箱是否存在
        if (userMapper.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("邮箱已存在");
        }
        // 密码加密
        user.setPassword(MD5Util.encrypt(user.getPassword()));
        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean updateUser(User user) {
        // 如果修改了密码，需要加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(MD5Util.encrypt(user.getPassword()));
        }
        return userMapper.update(user) > 0;
    }

    @Override
    public boolean updateUserInfo(User user) {
        // 不允许修改用户名、角色、状态等敏感信息
        User existingUser = userMapper.findById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 只更新允许修改的字段
        existingUser.setNickname(user.getNickname());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setAvatar(user.getAvatar());
        existingUser.setGender(user.getGender());  // 添加性别字段

        return userMapper.update(existingUser) > 0;
    }

    @Override
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证原密码
        if (!MD5Util.verify(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 加密新密码并更新
        String encryptedPassword = MD5Util.encrypt(newPassword);
        return userMapper.updatePassword(user.getEmail(), encryptedPassword) > 0;
    }

    @Override
    @Transactional
    public boolean updateEmail(Integer userId, String currentEmail, String newEmail, String code) {
        // 1. 验证验证码（根据当前邮箱查询 type=3 的验证码）
        VerificationCode validCode = verificationCodeMapper.findLatestValidCode(currentEmail, 3);
        if (validCode == null || !validCode.getCode().equals(code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 2. 检查新邮箱是否已被其他用户使用
        User existUser = userMapper.findByEmail(newEmail);
        if (existUser != null && !existUser.getId().equals(userId)) {
            throw new RuntimeException("该邮箱已被其他用户绑定");
        }

        // 3. 更新邮箱
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setEmail(newEmail);

        int result = userMapper.update(user);
        if (result > 0) {
            // 标记验证码已使用
            verificationCodeMapper.markAsUsed(validCode.getId());
            return true;
        }
        return false;
    }

}