package com.pet.mapper;

import com.pet.entity.Post;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PostMapper {

    // ========== 前台接口 ==========

    /**
     * 获取帖子列表
     */
    List<Post> getPostList(@Param("offset") Integer offset,
                           @Param("limit") Integer limit,
                           @Param("category") String category,
                           @Param("keyword") String keyword,
                           @Param("sort") String sort,
                           @Param("userId") Integer userId);

    /**
     * 获取帖子总数
     */
    int countPost(@Param("category") String category,
                  @Param("keyword") String keyword);

    /**
     * 获取用户帖子列表
     */
    List<Post> getUserPosts(@Param("userId") Integer userId,
                            @Param("offset") Integer offset,
                            @Param("limit") Integer limit);

    /**
     * 获取用户帖子总数
     */
    int countUserPosts(@Param("userId") Integer userId);

    /**
     * 获取收藏帖子列表
     */
    List<Post> getFavoritePosts(@Param("userId") Integer userId,
                                @Param("offset") Integer offset,
                                @Param("limit") Integer limit);

    /**
     * 获取收藏帖子总数
     */
    int countFavoritePosts(@Param("userId") Integer userId);

    /**
     * 根据ID获取帖子详情
     */
    Post getById(@Param("id") Integer id, @Param("userId") Integer userId);

    /**
     * 增加浏览量
     */
    int incrementViewCount(@Param("id") Integer id);

    /**
     * 发布帖子
     */
    int insert(Post post);

    /**
     * 更新帖子
     */
    int update(Post post);

    /**
     * 更新评论数
     */
    int updateCommentCount(@Param("id") Integer id, @Param("delta") Integer delta);

    /**
     * 更新点赞数
     */
    int updateLikeCount(@Param("id") Integer id, @Param("delta") Integer delta);

    /**
     * 删除帖子（软删除）
     */
    int deleteById(@Param("id") Integer id);

    // ========== 管理员接口 ==========

    /**
     * 管理员获取帖子列表
     */
    List<Post> getAdminPostList(@Param("offset") Integer offset,
                                @Param("limit") Integer limit,
                                @Param("keyword") String keyword,
                                @Param("status") Integer status,
                                @Param("isTop") Integer isTop,
                                @Param("isEssence") Integer isEssence);

    /**
     * 管理员获取帖子总数
     */
    int countAdminPost(@Param("keyword") String keyword,
                       @Param("status") Integer status,
                       @Param("isTop") Integer isTop,
                       @Param("isEssence") Integer isEssence);

    /**
     * 更新帖子状态（管理员用）
     */
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 置顶/取消置顶
     */
    int updateTop(@Param("id") Integer id, @Param("isTop") Integer isTop);

    /**
     * 精华/取消精华
     */
    int updateEssence(@Param("id") Integer id, @Param("isEssence") Integer isEssence);
}