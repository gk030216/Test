package com.pet.mapper;

import com.pet.entity.Product;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface ProductFavoriteMapper {

    int insert(@Param("userId") Integer userId, @Param("productId") Integer productId);

    int delete(@Param("userId") Integer userId, @Param("productId") Integer productId);

    int checkFavorite(@Param("userId") Integer userId, @Param("productId") Integer productId);

    List<Integer> getUserFavorites(@Param("userId") Integer userId);

    List<Map<String, Object>> getRecommendByUserFavorites(@Param("userId") Integer userId, @Param("limit") Integer limit);

    /**
     * 获取用户收藏的商品列表
     */
    List<Product> getFavoriteProducts(@Param("userId") Integer userId,
                                      @Param("offset") Integer offset,
                                      @Param("limit") Integer limit);

    /**
     * 统计用户收藏的商品数量
     */
    int countFavoriteProducts(@Param("userId") Integer userId);

    /**
     * 找到与当前用户相似的用户（基于共同收藏）
     */
    List<Integer> findSimilarUsers(@Param("userId") Integer userId, @Param("limit") Integer limit);

    /**
     * 找到与当前商品相似的商品（基于被同一用户收藏）
     */
    List<Map<String, Object>> findSimilarProducts(@Param("productId") Integer productId, @Param("limit") Integer limit);
}