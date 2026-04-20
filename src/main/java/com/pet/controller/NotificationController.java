package com.pet.controller;

import com.pet.service.NotificationService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    private Integer getUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            int count = notificationService.getUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取消息列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getNotificationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Map<String, Object> result = notificationService.getUserNotifications(userId, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记单条消息为已读
     */
    @PutMapping("/read/{id}")
    public Result<?> markAsRead(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = notificationService.markAsRead(id, userId);
            if (success) {
                return Result.success("标记成功");
            } else {
                return Result.error("标记失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记所有消息为已读
     */
    @PutMapping("/read/all")
    public Result<?> markAllAsRead(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = notificationService.markAllAsRead(userId);
            if (success) {
                return Result.success("已全部标记为已读");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteNotification(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = notificationService.deleteNotification(id, userId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}