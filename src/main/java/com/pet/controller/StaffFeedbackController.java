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
@RequestMapping("/api/staff/feedback")
@CrossOrigin(origins = "*")
public class StaffFeedbackController {

    @Autowired
    private FeedbackMapper feedbackMapper;

    private Integer getUserId(HttpServletRequest request) {
        return (Integer) request.getAttribute("userId");
    }

    private String getUserName(HttpServletRequest request) {
        return (String) request.getAttribute("username");
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        int offset = (page - 1) * pageSize;
        List<Feedback> list = feedbackMapper.getList(offset, pageSize, keyword, type, status);
        int total = feedbackMapper.countList(keyword, type, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return Result.success(result);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", feedbackMapper.countByStatus(0));
        stats.put("processing", feedbackMapper.countByStatus(1));
        stats.put("resolved", feedbackMapper.countByStatus(2));
        stats.put("today", feedbackMapper.countToday());
        return Result.success(stats);
    }

    @PutMapping("/process/{id}")
    public Result<?> process(@PathVariable Integer id, HttpServletRequest request) {
        Integer handlerId = getUserId(request);
        String handlerName = getUserName(request);
        feedbackMapper.updateStatus(id, 1, handlerId, handlerName);
        return Result.success("已开始处理");
    }

    @PutMapping("/resolve/{id}")
    public Result<?> resolve(@PathVariable Integer id, @RequestBody Map<String, String> params) {
        String processResult = params.get("processResult");
        if (processResult == null || processResult.isEmpty()) {
            return Result.error("处理结果不能为空");
        }
        feedbackMapper.resolve(id, processResult);
        return Result.success("已解决");
    }

    // 添加处理记录（直接更新处理结果，不需要单独的表）
    @PostMapping("/record")
    public Result<?> addRecord(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Integer feedbackId = (Integer) params.get("feedbackId");
        String content = (String) params.get("content");

        if (feedbackId == null || content == null || content.isEmpty()) {
            return Result.error("参数错误");
        }

        // 更新处理结果
        feedbackMapper.resolve(feedbackId, content);
        return Result.success("记录已添加");
    }
}