package com.pet.service.impl;

import com.pet.entity.User;
import com.pet.mapper.UserMapper;
import com.pet.service.VerificationCodeService;
import com.pet.util.CodeGenerator;
import com.pet.util.EmailUtil;
import com.pet.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailUtil emailUtil;

    private static final int EXPIRE_SECONDS = 300;

    @Override
    public boolean sendRegisterCode(String email) {
        if (userMapper.findByEmail(email) != null) {
            throw new RuntimeException("该邮箱已被注册");
        }
        return sendCode(email, "register");
    }

    @Override
    public boolean sendForgetCode(String email) {
        if (userMapper.findByEmail(email) == null) {
            throw new RuntimeException("该邮箱未注册");
        }
        return sendCode(email, "forget");
    }

    @Override
    public boolean sendChangeEmailCode(String email, boolean checkExist) {
        if (checkExist) {
            User existUser = userMapper.findByEmail(email);
            if (existUser != null) {
                throw new RuntimeException("该邮箱已被其他用户绑定");
            }
        }
        return sendCode(email, "change_email");
    }

    @Override
    public boolean verifyAndDeleteCode(String email, String code, String type) {
        return redisUtil.verifyAndDeleteCode(email, code, type);
    }

    private boolean sendCode(String email, String type) {
        String code = CodeGenerator.generateCode();
        redisUtil.saveVerificationCode(email, code, type, EXPIRE_SECONDS);
        return emailUtil.sendVerificationCode(email, code, type);
    }
}