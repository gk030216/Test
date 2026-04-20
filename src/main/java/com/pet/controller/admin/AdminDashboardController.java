package com.pet.controller.admin;

import com.pet.mapper.*;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PetProfileMapper petProfileMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostCommentMapper commentMapper;

    @Autowired
    private ServiceItemMapper serviceItemMapper;

    @Autowired
    private PetProfileMapper petMapper;

    @Autowired
    private ServiceItemMapper serviceMapper;

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getDashboardStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> statistics = new HashMap<>();

            // 总用户数
            statistics.put("totalUsers", userMapper.countAll());
            // 今日访问
            statistics.put("todayVisits", userMapper.countTodayLoginUsers());
            // 今日预约
            statistics.put("todayAppointments", appointmentMapper.countToday());
            // 今日收入
            BigDecimal todayIncome = appointmentMapper.countTodayIncome();
            statistics.put("todayIncome", todayIncome != null ? todayIncome : BigDecimal.ZERO);
            // 待处理预约
            statistics.put("pendingAppointments", appointmentMapper.countByStatus(0));
            // 宠物总数
            statistics.put("totalPets", petMapper.countAll());
            // 今日帖子
            statistics.put("todayPosts", postMapper.countToday());
            // 今日评论
            statistics.put("todayComments", commentMapper.countToday());

            result.put("statistics", statistics);

            // 热门服务排行
            List<Map<String, Object>> hotServices = serviceMapper.getHotServices(5);
            result.put("hotServices", hotServices);

            // 近7天预约趋势
            List<Map<String, Object>> weeklyTrend = appointmentMapper.getWeeklyTrend();
            result.put("weeklyTrend", weeklyTrend);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}