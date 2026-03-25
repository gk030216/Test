package com.pet.dto;

public class LoginRequest {
    private String account;     // 可以是用户名或邮箱
    private String password;

    // getter和setter
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}