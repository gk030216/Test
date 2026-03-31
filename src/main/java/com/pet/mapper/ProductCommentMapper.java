package com.pet.mapper;

import com.pet.entity.ProductComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ProductCommentMapper {

    // 添加评价
    int insert(ProductComment comment);

    // 获取商品评价列表（分页）
    List<ProductComment> getCommentList(@Param("productId") Integer productId,
                                        @Param("offset") Integer offset,
                                        @Param("limit") Integer limit);

    // 获取商品评价总数
    int countComment(@Param("productId") Integer productId);

    // 获取后台评价列表
    List<ProductComment> getAdminCommentList(@Param("offset") Integer offset,
                                             @Param("limit") Integer limit,
                                             @Param("keyword") String keyword,
                                             @Param("rating") Integer rating,
                                             @Param("status") Integer status,
                                             @Param("replyStatus") Integer replyStatus);

    // 获取后台评价总数
    int countAdminComment(@Param("keyword") String keyword,
                          @Param("rating") Integer rating,
                          @Param("status") Integer status,
                          @Param("replyStatus") Integer replyStatus);

    // 更新评价状态
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    // 商家回复
    int reply(@Param("id") Integer id, @Param("reply") String reply);

    // 删除评价
    int deleteById(@Param("id") Integer id);

    // 获取商品评分统计
    Map<String, Object> getRatingStats(@Param("productId") Integer productId);

    // 获取整体评价统计（用于数据分析）
    List<Map<String, Object>> getRatingDistribution();

    // 获取近7天评价趋势
    List<Map<String, Object>> getCommentTrend();
}