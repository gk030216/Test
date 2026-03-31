package com.pet.controller;

import com.pet.service.AIService;
import com.pet.util.JwtUtil;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Integer getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("请先登录");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new RuntimeException("无效的Token");
        }
        return userId;
    }

    /**
     * 智能问答
     */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            String question = params.get("question");
            if (question == null || question.trim().isEmpty()) {
                return Result.error("请输入问题");
            }
            Map<String, Object> result = aiService.smartChat(question, userId);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/history")
    public Result<Map<String, Object>> getHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            List<Map<String, Object>> rawList = aiService.getChatHistory(userId, page, pageSize);

            // 转换为前端期望的格式，并确保所有值都是字符串或基本类型
            List<Map<String, Object>> formattedList = new ArrayList<>();
            for (Map<String, Object> item : rawList) {
                Map<String, Object> formatted = new LinkedHashMap<>();
                formatted.put("id", item.get("id"));
                formatted.put("userQuestion", item.get("userQuestion") != null ? item.get("userQuestion").toString() : "");
                formatted.put("answerSource", item.get("answerSource"));
                formatted.put("aiAnswer", item.get("aiAnswer") != null ? item.get("aiAnswer").toString() : "");
                formatted.put("rating", item.get("rating"));
                // 格式化日期
                Object createTime = item.get("createTime");
                if (createTime != null) {
                    formatted.put("createTime", createTime.toString());
                } else {
                    formatted.put("createTime", "");
                }
                formattedList.add(formatted);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("list", formattedList);
            result.put("total", formattedList.size());
            result.put("page", page);
            result.put("pageSize", pageSize);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取常见问题列表
     */
    @GetMapping("/faq")
    public Result<List<Map<String, Object>>> getFaq() {
        try {
            List<Map<String, Object>> list = aiService.getFaqList();
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户评价答案
     */
    @PostMapping("/rate")
    public Result<?> rateAnswer(@RequestBody Map<String, Integer> params, HttpServletRequest request) {
        try {
            Integer historyId = params.get("historyId");
            Integer rating = params.get("rating");
            if (historyId == null || rating == null) {
                return Result.error("参数错误");
            }
            boolean success = aiService.rateAnswer(historyId, rating);
            if (success) {
                return Result.success("评价成功");
            } else {
                return Result.error("评价失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 清空对话历史
     */
    @DeleteMapping("/history")
    public Result<?> clearHistory(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = aiService.clearHistory(userId);
            if (success) {
                return Result.success("清空成功");
            } else {
                return Result.error("清空失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}