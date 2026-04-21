package com.pet.entity;

import java.util.Date;

public class Feedback {
    private Integer id;
    private Integer userId;        // 只存ID
    private String title;
    private String content;
    private String images;
    private String type;
    private Integer status;
    private String orderNo;
    private String appointmentNo;
    private String contact;
    private Integer handlerId;      // 只存ID
    private String processResult;
    private Date processTime;
    private Date createTime;
    private Date updateTime;

    // 查询时关联填充的字段（不存数据库）
    private String userNickname;
    private String userAvatar;
    private String handlerNickname;  // 处理人昵称

    // getter/setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getAppointmentNo() { return appointmentNo; }
    public void setAppointmentNo(String appointmentNo) { this.appointmentNo = appointmentNo; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public Integer getHandlerId() { return handlerId; }
    public void setHandlerId(Integer handlerId) { this.handlerId = handlerId; }
    public String getProcessResult() { return processResult; }
    public void setProcessResult(String processResult) { this.processResult = processResult; }
    public Date getProcessTime() { return processTime; }
    public void setProcessTime(Date processTime) { this.processTime = processTime; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public String getHandlerNickname() { return handlerNickname; }
    public void setHandlerNickname(String handlerNickname) { this.handlerNickname = handlerNickname; }
}