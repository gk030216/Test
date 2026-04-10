package com.pet.service;

import java.util.List;
import java.util.Map;

public interface RecommendationService {

    /**
     * 基于用户的协同过滤推荐
     * @param userId 当前用户ID
     * @param limit 推荐数量
     */
    List<Map<String, Object>> getUserBasedRecommendations(Integer userId, Integer limit);

    /**
     * 基于物品的协同过滤推荐
     * @param productId 当前浏览的商品ID
     * @param limit 推荐数量
     */
    List<Map<String, Object>> getItemBasedRecommendations(Integer productId, Integer limit);

    /**
     * 混合推荐（综合用户和物品）
     */
    List<Map<String, Object>> getHybridRecommendations(Integer userId, Integer productId, Integer limit);
}