package com.pet.controller;

import com.pet.entity.ServiceComment;
import com.pet.service.ServiceCommentService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/service/comment")
@CrossOrigin(origins = "*")
public class ServiceCommentController {

    @Autowired
    private ServiceCommentService commentService;

    private Integer getUserId(HttpServletRequest request) {
        return (Integer) request.getAttribute("userId");
    }

    private String getUserName(HttpServletRequest request) {
        return (String) request.getAttribute("username");
    }

    private Integer getUserRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    /**
     * 添加服务评价
     */
    @PostMapping("/add")
    public Result<?> addComment(@RequestBody ServiceComment comment, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            String userName = getUserName(request);

            comment.setUserId(userId);
            comment.setUserName(userName);
            comment.setStatus(1);

            boolean success = commentService.addComment(comment);
            return success ? Result.success("评价成功") : Result.error("评价失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取服务的评价列表
     */
    @GetMapping("/service/{serviceId}")
    public Result<Map<String, Object>> getCommentList(
            @PathVariable Integer serviceId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = commentService.getCommentList(serviceId, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据预约ID获取评价
     */
    @GetMapping("/appointment/{appointmentId}")
    public Result<ServiceComment> getByAppointmentId(@PathVariable Integer appointmentId) {
        try {
            ServiceComment comment = commentService.getByAppointmentId(appointmentId);
            return Result.success(comment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取服务评分统计
     */
    @GetMapping("/service/{serviceId}/stats")
    public Result<Map<String, Object>> getRatingStats(@PathVariable Integer serviceId) {
        try {
            Map<String, Object> stats = commentService.getRatingStats(serviceId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 管理员接口 ==========

    /**
     * 获取评价列表（后台）
     */
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> getAdminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer replyStatus,
            HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            Map<String, Object> result = commentService.getAdminList(page, pageSize, keyword, rating, status, replyStatus);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 商家回复
     */
    @PutMapping("/admin/reply")
    public Result<?> replyComment(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            Integer id = (Integer) params.get("id");
            String reply = (String) params.get("reply");
            boolean success = commentService.replyComment(id, reply);
            return success ? Result.success("回复成功") : Result.error("回复失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/admin/delete/{id}")
    public Result<?> deleteComment(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = commentService.deleteComment(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新评价状态（显示/隐藏）
     */
    @PutMapping("/admin/status")
    public Result<?> updateCommentStatus(@RequestParam Integer id, @RequestParam Integer status,
                                         HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = commentService.updateStatus(id, status);
            if (success) {
                return Result.success(status == 1 ? "显示成功" : "隐藏成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}