package com.pet.mapper;

import com.pet.entity.PostComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PostCommentMapper {

    // 获取帖子的顶级评论列表
    List<PostComment> getTopComments(@Param("postId") Integer postId,
                                     @Param("offset") Integer offset,
                                     @Param("limit") Integer limit);

    // 获取帖子的评论总数
    int countByPostId(@Param("postId") Integer postId);

    // 获取评论的子回复
    List<PostComment> getReplies(@Param("parentId") Integer parentId);

    // 获取用户评论列表
    List<PostComment> getUserComments(@Param("userId") Integer userId,
                                      @Param("offset") Integer offset,
                                      @Param("limit") Integer limit);

    // 统计用户评论数量
    int countUserComments(@Param("userId") Integer userId);

    // 根据ID获取评论
    PostComment getById(@Param("id") Integer id);

    // 插入评论
    int insert(PostComment comment);

    // 删除评论（软删除）
    int deleteById(@Param("id") Integer id);

    // 更新点赞数
    int updateLikeCount(@Param("id") Integer id, @Param("delta") Integer delta);

    // 获取评论列表（后台）
    List<PostComment> getAdminCommentList(@Param("offset") Integer offset,
                                          @Param("limit") Integer limit,
                                          @Param("keyword") String keyword,
                                          @Param("status") Integer status);

    // 统计评论数量（后台）
    int countAdminCommentList(@Param("keyword") String keyword,
                              @Param("status") Integer status);

    // 更新评论状态
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    int countToday();
}