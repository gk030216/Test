package com.pet.service;

import com.pet.entity.ServiceComment;
import java.util.Map;

public interface ServiceCommentService {
    boolean addComment(ServiceComment comment);
    Map<String, Object> getCommentList(Integer serviceId, Integer page, Integer pageSize);
    ServiceComment getByAppointmentId(Integer appointmentId);
    Map<String, Object> getRatingStats(Integer serviceId);
    boolean replyComment(Integer id, String reply);
    boolean deleteComment(Integer id);
    Map<String, Object> getAdminList(Integer page, Integer pageSize,
                                     String keyword, Integer rating,
                                     Integer status, Integer replyStatus);
    /**
     * 更新评价状态
     */
    boolean updateStatus(Integer id, Integer status);

    // 获取评价统计
    Map<String, Object> getStatistics();

    // 获取员工自己的评价统计
    Map<String, Object> getStatisticsByStaff(Integer staffId);

    /**
     * 获取用户的所有服务评价
     */
    Map<String, Object> getUserServiceComments(Integer userId, Integer page, Integer pageSize);

}