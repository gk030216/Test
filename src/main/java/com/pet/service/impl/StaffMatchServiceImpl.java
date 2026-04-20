package com.pet.service.impl;

import com.pet.entity.Appointment;
import com.pet.entity.StaffStatistics;
import com.pet.entity.User;
import com.pet.mapper.AppointmentMapper;
import com.pet.mapper.StaffStatisticsMapper;
import com.pet.mapper.UserMapper;
import com.pet.service.StaffMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class StaffMatchServiceImpl implements StaffMatchService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StaffStatisticsMapper statisticsMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    private static final int WEIGHT_RATING = 50;
    private static final int WEIGHT_EXPERIENCE = 30;
    private static final int WEIGHT_COMPLETION = 20;

    @Override
    public List<Map<String, Object>> getStaffWithMatchScore(Appointment appointment) {
        List<User> staffList = userMapper.getStaffList();
        if (staffList == null || staffList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, StaffStatistics> statsMap = new HashMap<>();
        List<StaffStatistics> allStats = statisticsMapper.getAllStaffStatistics();
        if (allStats != null) {
            for (StaffStatistics stats : allStats) {
                statsMap.put(stats.getStaffId(), stats);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (User staff : staffList) {
            StaffStatistics stats = statsMap.get(staff.getId());

            // ✅ 安全获取值，处理 null
            int serviceCount = 0;
            int completedCount = 0;
            BigDecimal ratingAvg = BigDecimal.ZERO;

            if (stats != null) {
                serviceCount = stats.getServiceCount() != null ? stats.getServiceCount() : 0;
                completedCount = stats.getCompletedCount() != null ? stats.getCompletedCount() : 0;
                ratingAvg = stats.getRatingAvg() != null ? stats.getRatingAvg() : BigDecimal.ZERO;
            }

            // 检查时间冲突
            int conflict = 0;
            try {
                conflict = appointmentMapper.checkStaffTimeConflict(
                        staff.getId(),
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime(),
                        null
                );
            } catch (Exception e) {
                System.err.println("检查时间冲突失败: " + e.getMessage());
            }

            int matchScore = calculateMatchScore(serviceCount, completedCount, ratingAvg);

            Map<String, Object> item = new HashMap<>();
            item.put("id", staff.getId());
            item.put("username", staff.getUsername());
            item.put("nickname", staff.getNickname() != null ? staff.getNickname() : staff.getUsername());
            item.put("matchScore", matchScore);
            item.put("hasConflict", conflict > 0);
            item.put("serviceCount", serviceCount);
            item.put("completedCount", completedCount);
            item.put("ratingAvg", ratingAvg.doubleValue()); // 显示 1-5 分
            item.put("ratingAvgRaw", ratingAvg.doubleValue());  // 原始 0-1 分

            result.add(item);
        }

        result.sort((a, b) -> {
            boolean conflictA = (boolean) a.get("hasConflict");
            boolean conflictB = (boolean) b.get("hasConflict");
            if (conflictA != conflictB) {
                return conflictA ? 1 : -1;
            }
            return ((Integer) b.get("matchScore")).compareTo((Integer) a.get("matchScore"));
        });

        return result;
    }

    /**
     * 计算匹配度分数 (0-100)
     */
    private int calculateMatchScore(int serviceCount, int completedCount, BigDecimal ratingAvg) {
        if (completedCount == 0) {
            return 0;
        }

        int score = 0;

        // 1. 评分得分 (0-50分)
        if (ratingAvg != null && ratingAvg.compareTo(BigDecimal.ZERO) > 0) {
            int ratingScore = ratingAvg.multiply(BigDecimal.valueOf(WEIGHT_RATING)).intValue();
            score += Math.min(ratingScore, WEIGHT_RATING);
        }

        // 2. 经验得分 (0-30分)
        int expScore = Math.min(completedCount, 30);
        score += expScore;

        // 3. 完成率得分 (0-20分)
        if (serviceCount > 0) {
            double completionRate = (double) completedCount / serviceCount;
            score += (int) (completionRate * WEIGHT_COMPLETION);
        }

        return Math.min(score, 100);
    }
}