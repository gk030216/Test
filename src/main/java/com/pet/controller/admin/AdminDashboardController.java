package com.pet.controller.admin;

import com.pet.mapper.*;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 统计数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", userMapper.countAll());
        statistics.put("todayVisits", 0);
        statistics.put("todayAppointments", appointmentMapper.countToday());
        statistics.put("todayIncome", orderMapper.countTodayIncome() != null ? orderMapper.countTodayIncome() : 0);
        statistics.put("pendingAppointments", appointmentMapper.countPending());
        statistics.put("totalPets", petProfileMapper.countAll());
        statistics.put("todayPosts", postMapper.countToday());
        statistics.put("todayComments", commentMapper.countToday());
        result.put("statistics", statistics);

        // 热门服务
        List<Map<String, Object>> hotServices = serviceItemMapper.getHotServices(5);
        result.put("hotServices", hotServices);

        // 近7天趋势
        List<Map<String, Object>> weeklyTrend = appointmentMapper.getWeeklyTrend();
        result.put("weeklyTrend", weeklyTrend);

        return Result.success(result);
    }
}