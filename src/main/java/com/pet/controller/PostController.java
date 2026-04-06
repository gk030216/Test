package com.pet.controller;

import com.pet.entity.Post;
import com.pet.entity.PostComment;
import com.pet.service.PostService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@CrossOrigin(origins = "*")
public class PostController {

    @Autowired
    private PostService postService;

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
     * 获取帖子列表
     */
    @GetMapping("/posts")
    public Result<Map<String, Object>> getPostList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Map<String, Object> result = postService.getPostList(page, pageSize, category, keyword, sort, userId);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/post/{id}")
    public Result<Post> getPostDetail(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Post post = postService.getPostDetail(id, userId);
            if (post != null) {
                return Result.success(post);
            } else {
                return Result.error("帖子不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发布帖子
     */
    @PostMapping("/post")
    public Result<?> addPost(@RequestBody Post post, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = postService.addPost(post, userId);
            if (success) {
                return Result.success("发布成功");
            } else {
                return Result.error("发布失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新帖子
     */
    @PutMapping("/post")
    public Result<?> updatePost(@RequestBody Post post, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = postService.updatePost(post, userId);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
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
    public Result<?> deletePost(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = postService.deletePost(id, userId);
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

    /**
     * 获取用户帖子列表
     */
    @GetMapping("/user/posts")
    public Result<Map<String, Object>> getUserPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Map<String, Object> result = postService.getUserPosts(userId, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/favorites")
    public Result<Map<String, Object>> getFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Map<String, Object> result = postService.getFavoritePosts(userId, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/post/{id}/like")
    public Result<Map<String, Object>> toggleLike(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Map<String, Object> result = postService.toggleLike(id, userId);
            return Result.success((String) result.get("message"), result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 收藏/取消收藏
     */
    @PostMapping("/post/{id}/favorite")
    public Result<Map<String, Object>> toggleFavorite(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Map<String, Object> result = postService.toggleFavorite(id, userId);
            return Result.success((String) result.get("message"), result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // ========== 评论相关 ==========

    /**
     * 获取帖子评论
     */
    @GetMapping("/post/{id}/comments")
    public Result<Map<String, Object>> getComments(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = postService.getComments(id, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加评论
     */
    @PostMapping("/comment")
    public Result<?> addComment(@RequestBody PostComment comment, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            String userName = getUserName(request);
            boolean success = postService.addComment(comment, userId, userName);
            if (success) {
                return Result.success("评论成功");
            } else {
                return Result.error("评论失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comment/{id}")
    public Result<?> deleteComment(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = postService.deleteComment(id, userId);
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