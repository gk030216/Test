package com.pet.controller.admin;

import com.pet.entity.PostComment;
import com.pet.service.PostService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isTop,
            @RequestParam(required = false) Integer isEssence) {
        try {
            Map<String, Object> result = postService.getAdminPostList(page, pageSize, keyword, category, status, isTop, isEssence);
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
     * 获取评论列表（后台）
     */
    @GetMapping("/comments")
    public Result<Map<String, Object>> getAdminCommentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = postService.getAdminCommentList(page, pageSize, keyword, status);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // 更新评论状态
    @PutMapping("/comment/status")
    public Result<?> updateCommentStatus(@RequestParam Integer id, @RequestParam Integer status) {
        try {
            boolean success = postService.updateCommentStatus(id, status);
            if (success) {
                return Result.success(status == 1 ? "显示成功" : "隐藏成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // 删除评论
    @DeleteMapping("/comment/{id}")
    public Result<?> deleteComment(@PathVariable Integer id) {
        try {
            boolean success = postService.adminDeleteComment(id);
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


    // 获取评论回复列表
    @GetMapping("/comment/{commentId}/replies")
    public Result<List<PostComment>> getCommentReplies(@PathVariable Integer commentId) {
        try {
            List<PostComment> replies = postService.getCommentReplies(commentId);
            return Result.success(replies);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // 批量删除评论
    @DeleteMapping("/comments/batch-delete")
    public Result<?> batchDeleteComments(@RequestParam String ids) {
        try {
            String[] idArray = ids.split(",");
            int successCount = 0;
            for (String idStr : idArray) {
                if (postService.adminDeleteComment(Integer.parseInt(idStr))) {
                    successCount++;
                }
            }
            return Result.success("批量删除成功，成功：" + successCount + "个");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取帖子统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getPostStatistics() {
        try {
            Map<String, Object> statistics = postService.getPostStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 真正删除帖子（物理删除）- 对应删除按钮
     */
    @DeleteMapping("/post/{id}")
    public Result<?> deletePostPermanently(@PathVariable Integer id) {
        try {
            boolean success = postService.physicallyDeletePost(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量置顶
     */
    @PutMapping("/posts/batch-top")
    public Result<?> batchUpdatePostTop(@RequestParam String ids, @RequestParam Integer isTop) {
        try {
            String[] idArray = ids.split(",");
            int successCount = 0;
            for (String idStr : idArray) {
                if (postService.updatePostTop(Integer.parseInt(idStr), isTop)) {
                    successCount++;
                }
            }
            return Result.success("批量置顶成功，成功：" + successCount + "个");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量精华
     */
    @PutMapping("/posts/batch-essence")
    public Result<?> batchUpdatePostEssence(@RequestParam String ids, @RequestParam Integer isEssence) {
        try {
            String[] idArray = ids.split(",");
            int successCount = 0;
            for (String idStr : idArray) {
                if (postService.updatePostEssence(Integer.parseInt(idStr), isEssence)) {
                    successCount++;
                }
            }
            return Result.success("批量精华成功，成功：" + successCount + "个");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除帖子（物理删除）
     */
    @DeleteMapping("/posts/batch-delete")
    public Result<?> batchDeletePosts(@RequestParam String ids) {
        try {
            String[] idArray = ids.split(",");
            int successCount = 0;
            for (String idStr : idArray) {
                if (postService.physicallyDeletePost(Integer.parseInt(idStr))) {
                    successCount++;
                }
            }
            return Result.success("批量删除成功，成功：" + successCount + "个");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

}