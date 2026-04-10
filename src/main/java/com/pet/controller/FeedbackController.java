package com.pet.controller;

import com.pet.entity.Feedback;
import com.pet.mapper.FeedbackMapper;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    private String getUserName(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return username != null ? username : "用户";
    }

    /**
     * 用户提交反馈
     */
    @PostMapping("/submit")
    public Result<?> submit(@RequestBody Feedback feedback, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            String userName = getUserName(request);
            feedback.setUserId(userId);
            feedback.setUserName(userName);
            feedback.setStatus(0);
            feedbackMapper.insert(feedback);
            return Result.success("提交成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的反馈列表
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
}