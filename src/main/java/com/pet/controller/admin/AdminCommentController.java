package com.pet.controller.admin;

import com.pet.service.ProductCommentService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/comment")
@CrossOrigin(origins = "*")
public class AdminCommentController {

    @Autowired
    private ProductCommentService commentService;

    /**
     * 获取评价列表（后台）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getCommentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer replyStatus) {
        try {
            Map<String, Object> result = commentService.getAdminCommentList(page, pageSize, keyword, rating, status, replyStatus);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取评价统计（数据分析）
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = commentService.getStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取最新评价
     */
    @GetMapping("/recent")
    public Result<List<Map<String, Object>>> getRecentComments(@RequestParam(defaultValue = "5") Integer limit) {
        try {
            List<Map<String, Object>> comments = commentService.getRecentComments(limit);
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新评价状态
     */
    @PutMapping("/status")
    public Result<?> updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        try {
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

    /**
     * 回复评价
     */
    @PostMapping("/reply")
    public Result<?> reply(@RequestBody Map<String, Object> params) {
        try {
            Integer id = (Integer) params.get("id");
            String reply = (String) params.get("reply");
            boolean success = commentService.replyComment(id, reply);
            if (success) {
                return Result.success("回复成功");
            } else {
                return Result.error("回复失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteComment(@PathVariable Integer id) {
        try {
            boolean success = commentService.deleteComment(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除评价
     */
    @DeleteMapping("/batch-delete")
    public Result<?> batchDelete(@RequestParam String ids) {
        try {
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            int successCount = 0;
            for (Integer id : idList) {
                if (commentService.deleteComment(id)) {
                    successCount++;
                }
            }
            return Result.success("批量删除成功，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}