package com.pet.service.impl;

import com.pet.entity.ServiceComment;
import com.pet.mapper.ServiceCommentMapper;
import com.pet.service.ServiceCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceCommentServiceImpl implements ServiceCommentService {

    @Autowired
    private ServiceCommentMapper commentMapper;

    @Override
    @Transactional
    public boolean addComment(ServiceComment comment) {
        // 检查是否已经评价过
        ServiceComment existComment = commentMapper.getByAppointmentId(comment.getAppointmentId());
        if (existComment != null) {
            throw new RuntimeException("该预约已评价过");
        }
        return commentMapper.insert(comment) > 0;
    }

    @Override
    public Map<String, Object> getCommentList(Integer serviceId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ServiceComment> list = commentMapper.getByServiceId(serviceId, offset, pageSize);
        int total = commentMapper.countByServiceId(serviceId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public ServiceComment getByAppointmentId(Integer appointmentId) {
        return commentMapper.getByAppointmentId(appointmentId);
    }

    @Override
    public Map<String, Object> getRatingStats(Integer serviceId) {
        Map<String, Object> stats = commentMapper.getRatingStats(serviceId);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("avg_rating", 0);
            stats.put("total_count", 0);
            stats.put("star5", 0);
            stats.put("star4", 0);
            stats.put("star3", 0);
            stats.put("star2", 0);
            stats.put("star1", 0);
        }
        return stats;
    }

    @Override
    @Transactional
    public boolean replyComment(Integer id, String reply) {
        return commentMapper.updateReply(id, reply) > 0;
    }

    @Override
    @Transactional
    public boolean deleteComment(Integer id) {
        return commentMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getAdminList(Integer page, Integer pageSize,
                                            String keyword, Integer rating, Integer status) {
        int offset = (page - 1) * pageSize;
        List<ServiceComment> list = commentMapper.getAdminList(offset, pageSize, keyword, rating, status);
        int total = commentMapper.countAdminList(keyword, rating, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

}