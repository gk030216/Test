package com.pet.service;

import com.pet.entity.ProductComment;
import java.util.List;
import java.util.Map;

public interface ProductCommentService {

    // 添加评价
    boolean addComment(ProductComment comment);

    // 获取商品评价列表
    Map<String, Object> getCommentList(Integer productId, Integer page, Integer pageSize);

    // 获取后台评价列表
    Map<String, Object> getAdminCommentList(Integer page, Integer pageSize, String keyword, Integer rating, Integer status);

    // 更新评价状态
    boolean updateStatus(Integer id, Integer status);

    // 商家回复
    boolean replyComment(Integer id, String reply);

    // 删除评价
    boolean deleteComment(Integer id);

    // 获取商品评分统计
    Map<String, Object> getRatingStats(Integer productId);

    // 获取整体评价统计（数据分析用）
    Map<String, Object> getStatistics();

    // 获取最新评价
    List<Map<String, Object>> getRecentComments(Integer limit);
}