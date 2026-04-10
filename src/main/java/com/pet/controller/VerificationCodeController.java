package com.pet.controller;

import com.pet.service.VerificationCodeService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
@CrossOrigin(origins = "*")
public class VerificationCodeController {

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 发送注册验证码
     */
    @PostMapping("/register")
    public Result<?> sendRegisterCode(@RequestParam String email) {
        try {
            boolean success = verificationCodeService.sendRegisterCode(email);
            if (success) {
                return Result.success("验证码发送成功");
            } else {
                return Result.error("验证码发送失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送找回密码验证码
     */
    @PostMapping("/forget")
    public Result<?> sendForgetCode(@RequestParam String email) {
        try {
            boolean success = verificationCodeService.sendForgetCode(email);
            if (success) {
                return Result.success("验证码发送成功");
            } else {
                return Result.error("验证码发送失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送邮箱验证码（通用）
     * 用于：验证当前邮箱、绑定新邮箱
     */
    @PostMapping("/change-email")
    public Result<?> sendChangeEmailCode(@RequestParam String email) {
        try {
            // 不检查邮箱是否被占用，由 updateEmail 接口负责检查
            boolean success = verificationCodeService.sendChangeEmailCode(email, false);
            if (success) {
                return Result.success("验证码发送成功");
            } else {
                return Result.error("验证码发送失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}