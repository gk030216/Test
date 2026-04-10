package com.pet.service.impl;

import com.pet.entity.User;
import com.pet.entity.VerificationCode;
import com.pet.mapper.UserMapper;
import com.pet.mapper.VerificationCodeMapper;
import com.pet.service.VerificationCodeService;
import com.pet.util.CodeGenerator;
import com.pet.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private VerificationCodeMapper verificationCodeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailUtil emailUtil;

    @Override
    @Transactional
    public boolean sendRegisterCode(String email) {
        // 检查邮箱是否已被注册
        if (userMapper.findByEmail(email) != null) {
            throw new RuntimeException("该邮箱已被注册");
        }

        return sendCode(email, "register", 1);
    }

    @Override
    @Transactional
    public boolean sendForgetCode(String email) {
        // 检查邮箱是否存在
        if (userMapper.findByEmail(email) == null) {
            throw new RuntimeException("该邮箱未注册");
        }

        return sendCode(email, "forget", 2);
    }

    /**
     * 发送验证码通用方法
     */
    private boolean sendCode(String email, String typeStr, Integer typeInt) {
        // 生成6位验证码
        String code = CodeGenerator.generateCode();

        // 设置过期时间（5分钟）
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        Date expireTime = calendar.getTime();

        // 保存验证码到数据库
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setType(typeInt);
        verificationCode.setExpireTime(expireTime);
        verificationCode.setIsUsed(0);

        verificationCodeMapper.insert(verificationCode);

        // 发送邮件
        return emailUtil.sendVerificationCode(email, code, typeStr);
    }

    @Override
    @Transactional
    public boolean sendChangeEmailCode(String email, boolean checkExist) {
        if (checkExist) {
            // 只有在需要检查时才检查邮箱是否已被使用
            User existUser = userMapper.findByEmail(email);
            if (existUser != null) {
                throw new RuntimeException("该邮箱已被其他用户绑定");
            }
        }
        return sendCode(email, "change_email", 3);
    }
}