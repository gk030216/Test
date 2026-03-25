package com.pet.service;

public interface VerificationCodeService {

    /**
     * 发送注册验证码
     */
    boolean sendRegisterCode(String email);

    /**
     * 发送找回密码验证码
     */
    boolean sendForgetCode(String email);
}