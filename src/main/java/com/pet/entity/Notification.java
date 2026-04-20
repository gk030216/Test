package com.pet.entity;

import java.util.Date;

public class Notification {
    private Integer id;
    private Integer userId;          // 接收消息的用户ID
    private String type;              // 消息类型: appointment, order, comment, system
    private String title;             // 消息标题
    private String content;           // 消息内容
    private String link;              // 跳转链接
    private Integer isRead;           // 是否已读: 0-未读, 1-已读
    private Date createTime;          // 创建时间
    private Date updateTime;          // 更新时间

    // getter和setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}