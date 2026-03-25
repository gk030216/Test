package com.pet.dto;

public class ForgetPasswordRequest {
    private String email;
    private String code;        // 验证码

    // getter和setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}