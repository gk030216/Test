package com.pet.service;

import com.pet.entity.Notification;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    /**
     * 发送消息通知给单个用户
     */
    void sendNotification(Integer userId, String type, String title, String content, String link);

    /**
     * 批量发送消息通知
     */
    void batchSendNotification(List<Notification> notifications);

    /**
     * 获取用户消息列表
     */
    Map<String, Object> getUserNotifications(Integer userId, Integer page, Integer pageSize);

    /**
     * 获取用户未读消息数量
     */
    int getUnreadCount(Integer userId);

    /**
     * 标记消息为已读
     */
    boolean markAsRead(Integer id, Integer userId);

    /**
     * 标记所有消息为已读
     */
    boolean markAllAsRead(Integer userId);

    /**
     * 删除消息
     */
    boolean deleteNotification(Integer id, Integer userId);
}