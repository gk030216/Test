package com.pet.controller.admin;

import com.pet.service.PostService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/community")
@CrossOrigin(origins = "*")
public class AdminPostController {

    @Autowired
    private PostService postService;

    /**
     * 获取帖子列表（后台）
     */
    @GetMapping("/posts")
    public Result<Map<String, Object>> getPostList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isTop,
            @RequestParam(required = false) Integer isEssence) {
        try {
            Map<String, Object> result = postService.getAdminPostList(page, pageSize, keyword, status, isTop, isEssence);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新帖子状态
     */
    @PutMapping("/post/status")
    public Result<?> updatePostStatus(@RequestParam Integer id, @RequestParam Integer status) {
        try {
            boolean success = postService.updatePostStatus(id, status);
            if (success) {
                return Result.success(status == 1 ? "恢复成功" : "下架成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 置顶/取消置顶
     */
    @PutMapping("/post/top")
    public Result<?> updatePostTop(@RequestParam Integer id, @RequestParam Integer isTop) {
        try {
            boolean success = postService.updatePostTop(id, isTop);
            if (success) {
                return Result.success(isTop == 1 ? "置顶成功" : "取消置顶成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 精华/取消精华
     */
    @PutMapping("/post/essence")
    public Result<?> updatePostEssence(@RequestParam Integer id, @RequestParam Integer isEssence) {
        try {
            boolean success = postService.updatePostEssence(id, isEssence);
            if (success) {
                return Result.success(isEssence == 1 ? "设为精华成功" : "取消精华成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/post/{id}")
    public Result<?> deletePost(@PathVariable Integer id) {
        try {
            boolean success = postService.adminDeletePost(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}