package com.pet.entity;

import java.util.Date;

public class VerificationCode {
    private Integer id;
    private String email;
    private String code;
    private Integer type;      // 1-注册 2-找回密码 3-修改邮箱
    private Date expireTime;
    private Integer isUsed;    // 0-未使用 1-已使用
    private Date createTime;

    public VerificationCode() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Integer isUsed) {
        this.isUsed = isUsed;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "VerificationCode{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", code='" + code + '\'' +
                ", type=" + type +
                ", expireTime=" + expireTime +
                ", isUsed=" + isUsed +
                ", createTime=" + createTime +
                '}';
    }
}