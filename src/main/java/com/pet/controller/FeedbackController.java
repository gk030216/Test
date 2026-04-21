package com.pet.controller;

import com.pet.entity.Feedback;
import com.pet.mapper.FeedbackMapper;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    @Autowired
    private FeedbackMapper feedbackMapper;

    private Integer getUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    private Integer getUserRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    /**
     * 用户提交反馈（只存 user_id，不存 user_name）
     */
    @PostMapping("/submit")
    public Result<?> submit(@RequestBody Feedback feedback, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            feedback.setUserId(userId);
            // 不设置 userName
            feedback.setStatus(0);
            feedbackMapper.insert(feedback);
            return Result.success("提交成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的反馈列表（用户端）
     */
    @GetMapping("/my")
    public Result<List<Feedback>> getMyFeedbacks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            int offset = (page - 1) * pageSize;
            List<Feedback> list = feedbackMapper.getUserFeedbacks(userId, offset, pageSize);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 后台管理接口（管理员/员工） =============

    /**
     * 获取反馈列表（后台）
     * 管理员：查看所有反馈
     * 员工：只查看待处理 + 自己处理的
     */
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> getAdminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Integer role = getUserRole(request);
            int offset = (page - 1) * pageSize;

            List<Feedback> list;
            int total;

            if (role == 3) {
                // 管理员：查看所有反馈
                list = feedbackMapper.getList(offset, pageSize, keyword, type, status);
                total = feedbackMapper.countList(keyword, type, status);
            } else {
                // 员工：只查看待处理 + 自己处理的
                list = feedbackMapper.getStaffList(offset, pageSize, keyword, type, status, userId);
                total = feedbackMapper.countStaffList(keyword, type, status, userId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取反馈统计（后台）
     * 管理员：统计所有
     * 员工：待处理（全部），处理中和已解决（只统计自己的）
     */
    @GetMapping("/admin/statistics")
    public Result<Map<String, Object>> getAdminStatistics(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Integer role = getUserRole(request);

            Map<String, Object> stats = new HashMap<>();

            if (role == 3) {
                // 管理员：统计所有
                stats.put("pending", feedbackMapper.countByStatus(0));
                stats.put("processing", feedbackMapper.countByStatus(1));
                stats.put("resolved", feedbackMapper.countByStatus(2));
                stats.put("today", feedbackMapper.countToday());
            } else {
                // 员工：待处理（全部），处理中和已解决（只统计自己的）
                stats.put("pending", feedbackMapper.countByStatus(0));
                stats.put("processing", feedbackMapper.countByStatusAndHandler(1, userId));
                stats.put("resolved", feedbackMapper.countByStatusAndHandler(2, userId));
                stats.put("today", feedbackMapper.countToday());
            }

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 开始处理反馈（只传 handler_id，不传 handler_name）
     */
    @PutMapping("/admin/process/{id}")
    public Result<?> processFeedback(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer handlerId = getUserId(request);
            // 只传 handlerId，不传 handlerName
            feedbackMapper.updateStatus(id, 1, handlerId);
            return Result.success("已开始处理");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 解决反馈（只传 handler_id，不传 handler_name）
     */
    @PutMapping("/admin/resolve/{id}")
    public Result<?> resolveFeedback(@PathVariable Integer id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Integer handlerId = getUserId(request);
            String processResult = params.get("processResult");
            if (processResult == null || processResult.isEmpty()) {
                return Result.error("处理结果不能为空");
            }
            // 只传 handlerId
            feedbackMapper.resolve(id, processResult, handlerId);
            return Result.success("已解决");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加处理记录
     */
    @PostMapping("/admin/record")
    public Result<?> addRecord(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Integer handlerId = getUserId(request);
            Integer feedbackId = (Integer) params.get("feedbackId");
            String content = (String) params.get("content");

            if (feedbackId == null || content == null || content.isEmpty()) {
                return Result.error("参数错误");
            }

            feedbackMapper.resolve(feedbackId, content, handlerId);
            return Result.success("记录已添加");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}