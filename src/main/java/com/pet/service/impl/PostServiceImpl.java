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
        // ❌ 删除 comment.setLikeCount(0);
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

    // ========== 管理员接口 ==========

    @Override
    public Map<String, Object> getAdminPostList(Integer page, Integer pageSize,
                                                String keyword, String category,
                                                Integer status, Integer isTop, Integer isEssence) {
        int offset = (page - 1) * pageSize;
        List<Post> list = postMapper.getAdminPostList(offset, pageSize, keyword, category, status, isTop, isEssence);
        int total = postMapper.countAdminPost(keyword, category, status, isTop, isEssence);

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
    @Transactional
    public boolean adminDeletePost(Integer id) {
        // 获取帖子下的所有评论
        List<PostComment> comments = commentMapper.getByPostId(id);
        for (PostComment comment : comments) {
            commentMapper.deleteById(comment.getId());  // 删除评论
        }
        return postMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getUserComments(Integer userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<PostComment> list = commentMapper.getUserComments(userId, offset, pageSize);
        int total = commentMapper.countUserComments(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Map<String, Object> getAdminCommentList(Integer page, Integer pageSize, String keyword, Integer status) {
        int offset = (page - 1) * pageSize;
        List<PostComment> list = commentMapper.getAdminCommentList(offset, pageSize, keyword, status);
        int total = commentMapper.countAdminCommentList(keyword, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public boolean updateCommentStatus(Integer id, Integer status) {
        return commentMapper.updateStatus(id, status) > 0;
    }

    @Override
    @Transactional
    public boolean adminDeleteComment(Integer id) {
        PostComment comment = commentMapper.getById(id);
        if (comment == null) return false;

        // 递归删除所有子评论，并统计删除数量
        int childCount = deleteChildCommentsAndCount(id);

        // 删除当前评论
        int result = commentMapper.deleteById(id);

        // 如果是顶级评论，更新帖子的评论数（删除数量 = 当前评论1 + 子评论数量）
        if (result > 0 && comment.getParentId() == 0) {
            postMapper.updateCommentCount(comment.getPostId(), -(1 + childCount));
        }

        return result > 0;
    }

    /**
     * 递归删除子评论并返回删除数量
     */
    private int deleteChildCommentsAndCount(Integer parentId) {
        int count = 0;
        List<PostComment> children = commentMapper.getReplies(parentId);
        if (children != null && !children.isEmpty()) {
            for (PostComment child : children) {
                count += deleteChildCommentsAndCount(child.getId());
                commentMapper.deleteById(child.getId());
                count++;
            }
        }
        return count;
    }

    /**
     * 递归删除评论及其所有子评论
     * @param commentId 评论ID
     * @return 删除的评论总数（包括自身和所有子评论）
     */
    private int deleteCommentAndChildren(Integer commentId) {
        int count = 1; // 当前评论计数

        // 1. 获取该评论的所有子评论
        List<PostComment> children = commentMapper.getReplies(commentId);

        // 2. 递归删除每个子评论
        if (children != null && !children.isEmpty()) {
            for (PostComment child : children) {
                count += deleteCommentAndChildren(child.getId());
            }
        }

        // 3. 删除当前评论
        commentMapper.deleteById(commentId);

        return count;
    }

    @Override
    public List<PostComment> getCommentReplies(Integer commentId) {
        return commentMapper.getReplies(commentId);
    }

    @Override
    public Map<String, Object> getPostStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总帖子数 - 需要传5个参数
        int totalPosts = postMapper.countAdminPost(null, null, null, null, null);
        stats.put("totalPosts", totalPosts);

        // 总评论数
        int totalComments = commentMapper.countAdminCommentList(null, null);
        stats.put("totalComments", totalComments);

        // 今日新增帖子数
        int todayPosts = postMapper.countToday();
        stats.put("todayPosts", todayPosts);

        // 总浏览量
        Integer totalViews = postMapper.sumViewCount();
        stats.put("totalViews", totalViews != null ? totalViews : 0);

        return stats;
    }

    @Override
    public boolean softDeletePost(Integer id) {
        // 软删除：设置 status = 0
        return postMapper.updateStatus(id, 0) > 0;
    }

    @Override
    @Transactional
    public boolean physicallyDeletePost(Integer id) {
        // 1. 先删除帖子下的所有评论
        List<PostComment> comments = commentMapper.getByPostId(id);
        for (PostComment comment : comments) {
            commentMapper.deleteById(comment.getId());
        }
        // 2. 再物理删除帖子
        return postMapper.physicallyDeleteById(id) > 0;
    }

}