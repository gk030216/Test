package com.pet.service.impl;

import com.pet.entity.Appointment;
import com.pet.entity.ServiceComment;
import com.pet.entity.StaffStatistics;
import com.pet.mapper.AppointmentMapper;
import com.pet.mapper.ProductMapper;
import com.pet.mapper.ServiceCommentMapper;
import com.pet.mapper.StaffStatisticsMapper;
import com.pet.service.ServiceCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceCommentServiceImpl implements ServiceCommentService {

    @Autowired
    private ServiceCommentMapper commentMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StaffStatisticsMapper statisticsMapper;

    @Autowired
    private ServiceCommentMapper serviceCommentMapper;

    @Override
    @Transactional
    public boolean addComment(ServiceComment comment) {
        System.out.println("========== addComment 被调用 ==========");
        System.out.println("预约ID: " + comment.getAppointmentId());
        System.out.println("评分: " + comment.getRating());

        // 检查是否已经评价过
        ServiceComment existComment = commentMapper.getByAppointmentId(comment.getAppointmentId());
        if (existComment != null) {
            throw new RuntimeException("该预约已评价过");
        }

        int result = commentMapper.insert(comment);
        System.out.println("插入评价结果: " + result);

        if (result > 0) {
            // 获取预约信息，找到对应的员工
            Appointment appointment = appointmentMapper.getById(comment.getAppointmentId());
            System.out.println("预约信息: " + (appointment != null ? "存在" : "不存在"));
            System.out.println("员工ID: " + (appointment != null ? appointment.getStaffId() : "null"));

            if (appointment != null && appointment.getStaffId() != null) {
                // 更新员工统计
                updateStaffStatistics(appointment.getStaffId(), comment.getRating());
            }

            // 更新服务评分（原有逻辑）
            Map<String, Object> stats = commentMapper.getRatingStats(comment.getServiceId());
            if (stats != null && stats.get("avg_rating") != null) {
                BigDecimal avgRating = new BigDecimal(stats.get("avg_rating").toString());
                int totalCount = Integer.parseInt(stats.get("total_count").toString());
                productMapper.updateRating(comment.getServiceId(), avgRating, totalCount);
            }
        }
        return result > 0;
    }

    // 更新员工统计的方法
    private void updateStaffStatistics(Integer staffId, Integer rating) {
        System.out.println("更新员工统计 - staffId: " + staffId + ", rating: " + rating);

        StaffStatistics stats = statisticsMapper.getByStaffId(staffId);
        System.out.println("现有统计: serviceCount=" + (stats != null ? stats.getServiceCount() : "null") +
                ", completedCount=" + (stats != null ? stats.getCompletedCount() : "null") +
                ", ratingSum=" + (stats != null ? stats.getRatingSum() : "null"));

        if (stats == null) {
            StaffStatistics newStats = new StaffStatistics();
            newStats.setStaffId(staffId);
            statisticsMapper.insert(newStats);
            System.out.println("初始化员工统计记录");
            stats = newStats;
        }

        int updateResult = statisticsMapper.updateStatistics(staffId, rating);
        System.out.println("更新结果: " + updateResult);

        // 验证更新后的数据
        StaffStatistics updated = statisticsMapper.getByStaffId(staffId);
        System.out.println("更新后统计 - serviceCount=" + updated.getServiceCount() +
                ", completedCount=" + updated.getCompletedCount() +
                ", ratingSum=" + updated.getRatingSum() +
                ", ratingAvg=" + updated.getRatingAvg());
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
                                            String keyword, Integer rating,
                                            Integer status, Integer replyStatus) {
        int offset = (page - 1) * pageSize;
        List<ServiceComment> list = commentMapper.getAdminList(
                offset, pageSize, keyword, rating, status, replyStatus
        );
        int total = commentMapper.countAdminList(keyword, rating, status, replyStatus);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    @Transactional
    public boolean updateStatus(Integer id, Integer status) {
        return commentMapper.updateStatus(id, status) > 0;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = commentMapper.getStatistics();
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("total", 0);
            stats.put("avgRating", 0);
            stats.put("replyCount", 0);
            stats.put("unreplyCount", 0);
        }
        return stats;
    }

    @Override
    public Map<String, Object> getStatisticsByStaff(Integer staffId) {
        Map<String, Object> stats = commentMapper.getStatisticsByStaff(staffId);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("total", 0);
            stats.put("avgRating", 0);
            stats.put("replyCount", 0);
            stats.put("unreplyCount", 0);
        }
        return stats;
    }

    @Override
    public Map<String, Object> getUserServiceComments(Integer userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ServiceComment> list = serviceCommentMapper.getUserServiceComments(userId, offset, pageSize);
        int total = serviceCommentMapper.countUserServiceComments(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }
}