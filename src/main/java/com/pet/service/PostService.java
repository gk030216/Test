package com.pet.service;

import com.pet.entity.Post;
import com.pet.entity.PostComment;
import java.util.Map;

public interface PostService {

    // ========== 帖子相关 ==========

    /**
     * 获取帖子列表
     */
    Map<String, Object> getPostList(Integer page, Integer pageSize, String category,
                                    String keyword, String sort, Integer userId);

    /**
     * 获取帖子详情
     */
    Post getPostDetail(Integer id, Integer userId);

    /**
     * 发布帖子
     */
    boolean addPost(Post post, Integer userId);

    /**
     * 更新帖子
     */
    boolean updatePost(Post post, Integer userId);

    /**
     * 删除帖子
     */
    boolean deletePost(Integer id, Integer userId);

    /**
     * 获取用户帖子列表
     */
    Map<String, Object> getUserPosts(Integer userId, Integer page, Integer pageSize);

    /**
     * 获取收藏帖子列表
     */
    Map<String, Object> getFavoritePosts(Integer userId, Integer page, Integer pageSize);

    /**
     * 点赞/取消点赞
     */
    Map<String, Object> toggleLike(Integer postId, Integer userId);

    /**
     * 收藏/取消收藏
     */
    Map<String, Object> toggleFavorite(Integer postId, Integer userId);

    // ========== 评论相关 ==========

    /**
     * 获取帖子评论
     */
    Map<String, Object> getComments(Integer postId, Integer page, Integer pageSize);

    /**
     * 添加评论
     */
    boolean addComment(PostComment comment, Integer userId, String userName);

    /**
     * 删除评论
     */
    boolean deleteComment(Integer id, Integer userId);

    /**
     * 点赞评论
     */
    boolean toggleCommentLike(Integer commentId, Integer userId);

    // ========== 管理员接口 ==========

    /**
     * 管理员获取帖子列表
     */
    Map<String, Object> getAdminPostList(Integer page, Integer pageSize, String keyword,
                                         Integer status, Integer isTop, Integer isEssence);

    /**
     * 更新帖子状态
     */
    boolean updatePostStatus(Integer id, Integer status);

    /**
     * 置顶/取消置顶
     */
    boolean updatePostTop(Integer id, Integer isTop);

    /**
     * 精华/取消精华
     */
    boolean updatePostEssence(Integer id, Integer isEssence);

    /**
     * 管理员删除帖子
     */
    boolean adminDeletePost(Integer id);
}