package com.pet.controller;

import com.pet.entity.User;
import com.pet.dto.LoginRequest;
import com.pet.dto.RegisterRequest;
import com.pet.dto.ResetPasswordRequest;
import com.pet.service.UserService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request);
            // 登录成功，返回用户信息（注意不要返回密码）
            user.setPassword(null);
            return Result.success("登录成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        try {
            boolean success = userService.register(request);
            if (success) {
                return Result.success("注册成功");
            } else {
                return Result.error("注册失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            boolean success = userService.resetPassword(request);
            if (success) {
                return Result.success("密码重置成功");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.checkUsername(username);
        return Result.success(exists);
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.checkEmail(email);
        return Result.success(exists);
    }
}