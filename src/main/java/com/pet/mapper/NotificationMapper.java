package com.pet.mapper;

import com.pet.entity.Notification;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NotificationMapper {

    /**
     * 插入消息通知
     */
    int insert(Notification notification);

    /**
     * 批量插入消息通知
     */
    int batchInsert(@Param("list") List<Notification> list);

    /**
     * 获取用户的消息列表
     */
    List<Notification> getByUserId(@Param("userId") Integer userId,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);

    /**
     * 统计用户消息总数
     */
    int countByUserId(@Param("userId") Integer userId);

    /**
     * 获取用户未读消息数量
     */
    int countUnread(@Param("userId") Integer userId);

    /**
     * 标记单条消息为已读
     */
    int markAsRead(@Param("id") Integer id, @Param("userId") Integer userId);

    /**
     * 标记所有消息为已读
     */
    int markAllAsRead(@Param("userId") Integer userId);

    /**
     * 删除消息
     */
    int deleteById(@Param("id") Integer id, @Param("userId") Integer userId);
}