package com.pet.service.impl;

import com.pet.entity.Post;
import com.pet.entity.PostComment;
import com.pet.mapper.*;
import com.pet.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostCommentMapper commentMapper;

    @Autowired
    private PostLikeMapper likeMapper;

    @Autowired
    private PostFavoriteMapper favoriteMapper;

    // ========== 帖子相关 ==========

    @Override
    public Map<String, Object> getPostList(Integer page, Integer pageSize, String category,
                                           String keyword, String sort, Integer userId) {
        int offset = (page - 1) * pageSize;
        List<Post> list = postMapper.getPostList(offset, pageSize, category, keyword, sort, userId);
        int total = postMapper.countPost(category, keyword);

        // 处理空值
        for (Post post : list) {
            if (post.getLikeCount() == null) post.setLikeCount(0);
            if (post.getCommentCount() == null) post.setCommentCount(0);
            if (post.getViewCount() == null) post.setViewCount(0);
            if (post.getIsLiked() == null) post.setIsLiked(false);
            if (post.getIsFavorited() == null) post.setIsFavorited(false);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Post getPostDetail(Integer id, Integer userId) {
        postMapper.incrementViewCount(id);
        Post post = postMapper.getById(id, userId);
        if (post != null) {
            if (post.getLikeCount() == null) post.setLikeCount(0);
            if (post.getCommentCount() == null) post.setCommentCount(0);
            if (post.getViewCount() == null) post.setViewCount(0);
            if (post.getIsLiked() == null) post.setIsLiked(false);
            if (post.getIsFavorited() == null) post.setIsFavorited(false);
        }
        return post;
    }

    @Override
    @Transactional
    public boolean addPost(Post post, Integer userId) {
        post.setUserId(userId);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setViewCount(0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setStatus(1);
        return postMapper.insert(post) > 0;
    }

    @Override
    public boolean updatePost(Post post, Integer userId) {
        post.setUserId(userId);
        return postMapper.update(post) > 0;
    }

    @Override
    @Transactional
    public boolean deletePost(Integer id, Integer userId) {
        return postMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getUserPosts(Integer userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Post> list = postMapper.getUserPosts(userId, offset, pageSize);
        int total = postMapper.countUserPosts(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Map<String, Object> getFavoritePosts(Integer userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Post> list = postMapper.getFavoritePosts(userId, offset, pageSize);
        int total = postMapper.countFavoritePosts(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Integer postId, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        int exists = likeMapper.checkLike(postId, userId);
        if (exists > 0) {
            likeMapper.delete(postId, userId);
            postMapper.updateLikeCount(postId, -1);
            result.put("isLiked", false);
            result.put("message", "取消点赞成功");
        } else {
            likeMapper.insert(postId, userId);
            postMapper.updateLikeCount(postId, 1);
            result.put("isLiked", true);
            result.put("message", "点赞成功");
        }
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> toggleFavorite(Integer postId, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        int exists = favoriteMapper.checkFavorite(postId, userId);
        if (exists > 0) {
            favoriteMapper.delete(postId, userId);
            result.put("isFavorited", false);
            result.put("message", "取消收藏成功");
        } else {
            favoriteMapper.insert(postId, userId);
            result.put("isFavorited", true);
            result.put("message", "收藏成功");
        }
        return result;
    }

    // ========== 评论相关 ==========

    @Override
    public Map<String, Object> getComments(Integer postId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;

        List<PostComment> topComments = commentMapper.getTopComments(postId, offset, pageSize);
        int total = commentMapper.countByPostId(postId);

        for (PostComment comment : topComments) {
            List<PostComment> replies = commentMapper.getReplies(comment.getId());
            comment.setReplies(replies);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", topComments);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    @Transactional
    public boolean addComment(PostComment comment, Integer userId, String userName) {
        comment.setUserId(userId);
        comment.setStatus(1);
        comment.setLikeCount(0);
        int result = commentMapper.insert(comment);

        if (result > 0) {
            postMapper.updateCommentCount(comment.getPostId(), 1);
        }
        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteComment(Integer id, Integer userId) {
        PostComment comment = commentMapper.getById(id);
        if (comment == null || !comment.getUserId().equals(userId)) {
            return false;
        }
        int result = commentMapper.deleteById(id);
        if (result > 0) {
            postMapper.updateCommentCount(comment.getPostId(), -1);
        }
        return result > 0;
    }

    @Override
    @Transactional
    public boolean toggleCommentLike(Integer commentId, Integer userId) {
        // 简化实现，实际可创建单独的评论点赞表
        return true;
    }

    // ========== 管理员接口 ==========

    @Override
    public Map<String, Object> getAdminPostList(Integer page, Integer pageSize, String keyword,
                                                Integer status, Integer isTop, Integer isEssence) {
        int offset = (page - 1) * pageSize;
        List<Post> list = postMapper.getAdminPostList(offset, pageSize, keyword, status, isTop, isEssence);
        int total = postMapper.countAdminPost(keyword, status, isTop, isEssence);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public boolean updatePostStatus(Integer id, Integer status) {
        return postMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean updatePostTop(Integer id, Integer isTop) {
        return postMapper.updateTop(id, isTop) > 0;
    }

    @Override
    public boolean updatePostEssence(Integer id, Integer isEssence) {
        return postMapper.updateEssence(id, isEssence) > 0;
    }

    @Override
    public boolean adminDeletePost(Integer id) {
        return postMapper.deleteById(id) > 0;
    }
}