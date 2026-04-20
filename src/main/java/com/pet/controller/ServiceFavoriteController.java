package com.pet.controller;

import com.pet.service.ServiceFavoriteService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/service/favorite")
@CrossOrigin(origins = "*")
public class ServiceFavoriteController {

    @Autowired
    private ServiceFavoriteService favoriteService;

    private Integer getUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    /**
     * 添加收藏
     */
    @PostMapping("/{serviceId}")
    public Result<?> addFavorite(@PathVariable Integer serviceId, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = favoriteService.addFavorite(userId, serviceId);
            if (success) {
                return Result.success("收藏成功");
            } else {
                return Result.error("收藏失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{serviceId}")
    public Result<?> removeFavorite(@PathVariable Integer serviceId, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = favoriteService.removeFavorite(userId, serviceId);
            if (success) {
                return Result.success("取消收藏成功");
            } else {
                return Result.error("取消收藏失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查收藏状态
     */
    @GetMapping("/{serviceId}/check")
    public Result<Boolean> checkFavorite(@PathVariable Integer serviceId, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean isFavorited = favoriteService.isFavorited(userId, serviceId);
            return Result.success(isFavorited);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer pageSize,
            HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Map<String, Object> result = favoriteService.getUserFavorites(userId, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}