package com.pet.controller;

import com.pet.entity.ProductComment;
import com.pet.service.ProductCommentService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private ProductCommentService commentService;

    private Integer getUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    private String getUserName(HttpServletRequest request) {
        return (String) request.getAttribute("username");
    }

    /**
     * 添加评价
     */
    @PostMapping("/add")
    public Result<?> addComment(@RequestBody ProductComment comment, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            String userName = getUserName(request);

            comment.setUserId(userId);
            comment.setUserName(userName);
            comment.setStatus(1); // 默认显示

            boolean success = commentService.addComment(comment);
            if (success) {
                return Result.success("评价成功");
            } else {
                return Result.error("评价失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取商品评价列表
     */
    @GetMapping("/product/{productId}")
    public Result<Map<String, Object>> getProductComments(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = commentService.getCommentList(productId, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取商品评分统计
     */
    @GetMapping("/product/{productId}/stats")
    public Result<Map<String, Object>> getProductRatingStats(@PathVariable Integer productId) {
        try {
            Map<String, Object> stats = commentService.getRatingStats(productId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}