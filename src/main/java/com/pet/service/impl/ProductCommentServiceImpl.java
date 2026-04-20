package com.pet.service.impl;

import com.pet.entity.Product;
import com.pet.entity.ProductComment;
import com.pet.mapper.ProductCommentMapper;
import com.pet.mapper.ProductMapper;
import com.pet.service.NotificationService;
import com.pet.service.ProductCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductCommentServiceImpl implements ProductCommentService {

    @Autowired
    private ProductCommentMapper commentMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public boolean addComment(ProductComment comment) {
        int result = commentMapper.insert(comment);
        if (result > 0) {
            // 更新商品评分
            Map<String, Object> stats = commentMapper.getRatingStats(comment.getProductId());
            if (stats != null && stats.get("avg_rating") != null) {
                BigDecimal avgRating = new BigDecimal(stats.get("avg_rating").toString());
                int totalCount = Integer.parseInt(stats.get("total_count").toString());
                productMapper.updateRating(comment.getProductId(), avgRating, totalCount);
            }

            // ✅ 发送评价成功站内消息
            notificationService.sendNotification(
                    comment.getUserId(),
                    "comment",
                    "评价成功",
                    "感谢您对【" + comment.getProductName() + "】的评价，您的反馈对我们很重要！",
                    "/personal/orders"
            );
            System.out.println("✅ 评价成功站内消息已发送");
        }
        return result > 0;
    }


    @Override
    public Map<String, Object> getCommentList(Integer productId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ProductComment> list = commentMapper.getCommentList(productId, offset, pageSize);
        int total = commentMapper.countComment(productId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Map<String, Object> getAdminCommentList(Integer page, Integer pageSize, String keyword,
                                                   Integer rating, Integer status, Integer replyStatus) {
        int offset = (page - 1) * pageSize;
        List<ProductComment> list = commentMapper.getAdminCommentList(offset, pageSize, keyword, rating, status, replyStatus);
        int total = commentMapper.countAdminComment(keyword, rating, status, replyStatus);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        return commentMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean replyComment(Integer id, String reply) {
        return commentMapper.reply(id, reply) > 0;
    }

    @Override
    public boolean deleteComment(Integer id) {
        return commentMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getRatingStats(Integer productId) {
        return commentMapper.getRatingStats(productId);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 评分分布
        List<Map<String, Object>> distribution = commentMapper.getRatingDistribution();
        result.put("ratingDistribution", distribution);

        // 近7天评价趋势
        List<Map<String, Object>> trend = commentMapper.getCommentTrend();
        result.put("commentTrend", trend);

        // 总评价数
        int total = commentMapper.countAdminComment(null, null, null, null);
        result.put("totalComments", total);

        // 平均评分
        if (total > 0) {
            List<Map<String, Object>> allStats = commentMapper.getRatingDistribution();
            BigDecimal sum = BigDecimal.ZERO;
            int totalCount = 0;
            for (Map<String, Object> stat : allStats) {
                int rating = Integer.parseInt(stat.get("rating").toString());
                int count = Integer.parseInt(stat.get("count").toString());
                sum = sum.add(BigDecimal.valueOf(rating * count));
                totalCount += count;
            }
            BigDecimal avg = sum.divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP);
            result.put("avgRating", avg);

            // 好评率（4星及以上）
            int positiveCount = 0;
            for (Map<String, Object> stat : allStats) {
                int rating = Integer.parseInt(stat.get("rating").toString());
                int count = Integer.parseInt(stat.get("count").toString());
                if (rating >= 4) {
                    positiveCount += count;
                }
            }
            result.put("positiveRate", totalCount > 0 ? positiveCount * 100 / totalCount : 0);
        } else {
            result.put("avgRating", 0);
            result.put("positiveRate", 0);
        }

        // 回复率
        List<ProductComment> allComments = commentMapper.getAdminCommentList(0, 9999, null, null, null, null);
        long repliedCount = allComments.stream().filter(c -> c.getReply() != null && !c.getReply().isEmpty()).count();
        result.put("replyRate", allComments.size() > 0 ? repliedCount * 100 / allComments.size() : 0);

        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentComments(Integer limit) {
        List<ProductComment> comments = commentMapper.getAdminCommentList(0, limit, null, null, 1, null);
        return comments.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("productId", c.getProductId());
            map.put("userName", c.getUserName());
            map.put("rating", c.getRating());
            map.put("content", c.getContent());
            map.put("reply", c.getReply());
            map.put("replyTime", c.getReplyTime());
            map.put("createTime", c.getCreateTime());
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * ✅ 新增：根据ID获取评价
     */
    @Override
    public ProductComment getById(Integer id) {
        return commentMapper.getById(id);
    }

    @Override
    public Map<String, Object> getProductCommentStatistics(Integer productId) {
        Map<String, Object> result = new HashMap<>();

        // 获取评分统计
        Map<String, Object> stats = commentMapper.getRatingStats(productId);

        // 获取评分分布
        List<Map<String, Object>> ratingDistribution = getProductRatingDistribution(productId);

        // 获取近7天评价趋势
        List<Map<String, Object>> commentTrend = getProductCommentTrend(productId);

        int totalComments = 0;
        double avgRating = 0;
        if (stats != null && stats.get("total_count") != null) {
            totalComments = Integer.parseInt(stats.get("total_count").toString());
            avgRating = stats.get("avg_rating") != null ? Double.parseDouble(stats.get("avg_rating").toString()) : 0;
        }

        // 计算好评率（4星及以上）
        int positiveCount = 0;
        if (ratingDistribution != null) {
            for (Map<String, Object> item : ratingDistribution) {
                int rating = Integer.parseInt(item.get("rating").toString());
                int count = Integer.parseInt(item.get("count").toString());
                if (rating >= 4) {
                    positiveCount += count;
                }
            }
        }
        double positiveRate = totalComments > 0 ? (positiveCount * 100.0 / totalComments) : 0;

        // 计算回复率
        int repliedCount = getProductRepliedCommentCount(productId);
        double replyRate = totalComments > 0 ? (repliedCount * 100.0 / totalComments) : 0;

        result.put("totalComments", totalComments);
        result.put("avgRating", Math.round(avgRating * 10) / 10.0);
        result.put("positiveRate", Math.round(positiveRate));
        result.put("replyRate", Math.round(replyRate));
        result.put("ratingDistribution", ratingDistribution != null ? ratingDistribution : new ArrayList<>());
        result.put("commentTrend", commentTrend != null ? commentTrend : new ArrayList<>());

        return result;
    }

    @Override
    public Map<String, Object> getProductCommentsList(Integer productId, Integer page, Integer pageSize, Integer rating) {
        int offset = (page - 1) * pageSize;
        List<ProductComment> list = commentMapper.getCommentListWithRating(productId, offset, pageSize, rating);
        int total = commentMapper.countCommentWithRating(productId, rating);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    // 辅助方法：获取商品评分分布
    private List<Map<String, Object>> getProductRatingDistribution(Integer productId) {
        return commentMapper.getProductRatingDistribution(productId);
    }

    // 辅助方法：获取商品近7天评价趋势
    private List<Map<String, Object>> getProductCommentTrend(Integer productId) {
        return commentMapper.getProductCommentTrend(productId);
    }

    // 辅助方法：获取商品已回复评价数量
    private int getProductRepliedCommentCount(Integer productId) {
        return commentMapper.getProductRepliedCommentCount(productId);
    }

    // ProductCommentServiceImpl.java
    @Override
    public Map<String, Object> getUserProductComments(Integer userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ProductComment> list = commentMapper.getUserProductComments(userId, offset, pageSize);
        int total = commentMapper.countUserProductComments(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }
}