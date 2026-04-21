package com.pet.controller;

import com.pet.service.impl.VerificationCodeServiceImpl;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
@CrossOrigin(origins = "*")
public class VerificationCodeController {

    @Autowired
    private VerificationCodeServiceImpl verificationCodeService;  // 使用实现类

    @PostMapping("/register")
    public Result<?> sendRegisterCode(@RequestParam String email) {
        try {
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                return Result.error("邮箱格式不正确");
            }
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

    @PostMapping("/change-email")
    public Result<?> sendChangeEmailCode(@RequestParam String email) {
        try {
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