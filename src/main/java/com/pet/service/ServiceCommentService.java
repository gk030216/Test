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
    Map<String, Object> getAdminList(Integer page, Integer pageSize, String keyword, Integer rating, Integer status);
}