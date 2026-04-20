package com.pet.service.impl;

import com.pet.entity.Notification;
import com.pet.mapper.NotificationMapper;
import com.pet.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void sendNotification(Integer userId, String type, String title, String content, String link) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setLink(link);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    @Transactional
    public void batchSendNotification(List<Notification> notifications) {
        if (notifications != null && !notifications.isEmpty()) {
            notificationMapper.batchInsert(notifications);
        }
    }

    @Override
    public Map<String, Object> getUserNotifications(Integer userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Notification> list = notificationMapper.getByUserId(userId, offset, pageSize);
        int total = notificationMapper.countByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public int getUnreadCount(Integer userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public boolean markAsRead(Integer id, Integer userId) {
        return notificationMapper.markAsRead(id, userId) > 0;
    }

    @Override
    public boolean markAllAsRead(Integer userId) {
        return notificationMapper.markAllAsRead(userId) > 0;
    }

    @Override
    public boolean deleteNotification(Integer id, Integer userId) {
        return notificationMapper.deleteById(id, userId) > 0;
    }
}