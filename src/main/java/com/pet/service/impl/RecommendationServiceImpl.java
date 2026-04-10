package com.pet.service.impl;

import com.pet.entity.Product;
import com.pet.mapper.ProductMapper;
import com.pet.mapper.ProductFavoriteMapper;
import com.pet.mapper.OrderItemMapper;
import com.pet.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private ProductFavoriteMapper favoriteMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 基于用户的协同过滤
     */
    @Override
    public List<Map<String, Object>> getUserBasedRecommendations(Integer userId, Integer limit) {
        if (userId == null) {
            return getPopularProducts(limit);
        }

        // 1. 获取当前用户的收藏商品
        List<Integer> userFavorites = favoriteMapper.getUserFavorites(userId);
        // 获取当前用户的购买商品
        List<Integer> userPurchases = orderItemMapper.getUserPurchasedProductIds(userId);

        Set<Integer> userItems = new HashSet<>();
        if (userFavorites != null) userItems.addAll(userFavorites);
        if (userPurchases != null) userItems.addAll(userPurchases);

        if (userItems.isEmpty()) {
            return getPopularProducts(limit);
        }

        // 2. 找到相似用户
        Map<Integer, Double> userSimilarityMap = new HashMap<>();
        List<Integer> similarUsers = favoriteMapper.findSimilarUsers(userId, 50);

        if (similarUsers == null) similarUsers = new ArrayList<>();

        for (Integer similarUser : similarUsers) {
            List<Integer> similarUserFavorites = favoriteMapper.getUserFavorites(similarUser);
            List<Integer> similarUserPurchases = orderItemMapper.getUserPurchasedProductIds(similarUser);

            Set<Integer> similarUserItems = new HashSet<>();
            if (similarUserFavorites != null) similarUserItems.addAll(similarUserFavorites);
            if (similarUserPurchases != null) similarUserItems.addAll(similarUserPurchases);

            // 计算Jaccard相似度
            Set<Integer> intersection = new HashSet<>(userItems);
            intersection.retainAll(similarUserItems);

            Set<Integer> union = new HashSet<>(userItems);
            union.addAll(similarUserItems);

            double similarity = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
            userSimilarityMap.put(similarUser, similarity);
        }

        // 3. 计算商品推荐分数
        Map<Integer, Double> productScores = new HashMap<>();
        Map<Integer, Integer> productFrequency = new HashMap<>();

        for (Map.Entry<Integer, Double> entry : userSimilarityMap.entrySet()) {
            Integer similarUser = entry.getKey();
            Double similarity = entry.getValue();

            if (similarity < 0.1) continue;

            List<Integer> favorites = favoriteMapper.getUserFavorites(similarUser);
            List<Integer> purchases = orderItemMapper.getUserPurchasedProductIds(similarUser);

            Set<Integer> candidateItems = new HashSet<>();
            if (favorites != null) candidateItems.addAll(favorites);
            if (purchases != null) candidateItems.addAll(purchases);

            for (Integer productId : candidateItems) {
                if (userItems.contains(productId)) continue;

                productScores.put(productId, productScores.getOrDefault(productId, 0.0) + similarity);
                productFrequency.put(productId, productFrequency.getOrDefault(productId, 0) + 1);
            }
        }

        // 4. 排序返回结果
        List<Map<String, Object>> result = productScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Product product = productMapper.getById(entry.getKey());
                    if (product == null) return null;

                    Map<String, Object> item = new HashMap<>();
                    item.put("id", product.getId());
                    item.put("name", product.getName());
                    item.put("price", product.getPrice());
                    item.put("image", product.getImage());
                    item.put("sales", product.getSales());
                    item.put("score", entry.getValue());
                    item.put("frequency", productFrequency.get(entry.getKey()));
                    item.put("reason", "和你兴趣相似的用户也喜欢");
                    return item;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 如果推荐结果不足，用热门商品补充
        if (result.size() < limit) {
            List<Map<String, Object>> popular = getPopularProducts(limit - result.size());
            for (Map<String, Object> item : popular) {
                boolean exists = result.stream().anyMatch(r -> r.get("id").equals(item.get("id")));
                if (!exists) {
                    result.add(item);
                }
            }
        }

        return result;
    }

    /**
     * 基于物品的协同过滤
     */
    @Override
    public List<Map<String, Object>> getItemBasedRecommendations(Integer productId, Integer limit) {
        if (productId == null) {
            return getPopularProducts(limit);
        }

        // 获取与当前商品相似的商品
        List<Map<String, Object>> similarProducts = favoriteMapper.findSimilarProducts(productId, limit * 2);

        if (similarProducts == null) {
            similarProducts = new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : similarProducts) {
            Integer id = (Integer) item.get("id");
            Object similarityObj = item.get("similarity");

            // 处理不同类型的相似度值
            Double similarity;
            if (similarityObj instanceof Integer) {
                similarity = ((Integer) similarityObj).doubleValue();
            } else if (similarityObj instanceof Long) {
                similarity = ((Long) similarityObj).doubleValue();
            } else if (similarityObj instanceof Double) {
                similarity = (Double) similarityObj;
            } else if (similarityObj instanceof Number) {
                similarity = ((Number) similarityObj).doubleValue();
            } else {
                similarity = 0.0;
            }

            Product product = productMapper.getById(id);
            if (product != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", product.getId());
                map.put("name", product.getName());
                map.put("price", product.getPrice());
                map.put("image", product.getImage());
                map.put("sales", product.getSales());
                map.put("score", similarity);
                map.put("reason", "浏览此商品的用户也喜欢");
                result.add(map);
            }

            if (result.size() >= limit) break;
        }

        // 如果推荐结果不足，用热门商品补充
        if (result.size() < limit) {
            List<Map<String, Object>> popular = getPopularProducts(limit - result.size());
            for (Map<String, Object> item : popular) {
                boolean exists = result.stream().anyMatch(r -> r.get("id").equals(item.get("id")));
                if (!exists) {
                    result.add(item);
                }
            }
        }

        return result.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 混合推荐
     */
    @Override
    public List<Map<String, Object>> getHybridRecommendations(Integer userId, Integer productId, Integer limit) {
        List<Map<String, Object>> userBased = new ArrayList<>();
        List<Map<String, Object>> itemBased = new ArrayList<>();

        if (userId != null) {
            userBased = getUserBasedRecommendations(userId, limit / 2);
        }

        if (productId != null) {
            itemBased = getItemBasedRecommendations(productId, limit / 2);
        }

        // 合并去重
        Set<Integer> seenIds = new HashSet<>();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> item : userBased) {
            Integer id = (Integer) item.get("id");
            if (!seenIds.contains(id)) {
                seenIds.add(id);
                result.add(item);
            }
        }

        for (Map<String, Object> item : itemBased) {
            Integer id = (Integer) item.get("id");
            if (!seenIds.contains(id)) {
                seenIds.add(id);
                // 物品协同过滤权重稍低
                Object scoreObj = item.get("score");
                Double score;
                if (scoreObj instanceof Integer) {
                    score = ((Integer) scoreObj).doubleValue() * 0.8;
                } else if (scoreObj instanceof Long) {
                    score = ((Long) scoreObj).doubleValue() * 0.8;
                } else if (scoreObj instanceof Double) {
                    score = (Double) scoreObj * 0.8;
                } else if (scoreObj instanceof Number) {
                    score = ((Number) scoreObj).doubleValue() * 0.8;
                } else {
                    score = 0.0;
                }
                item.put("score", score);
                result.add(item);
            }
        }

        // 按分数排序
        result.sort((a, b) -> {
            Object scoreAObj = a.get("score");
            Object scoreBObj = b.get("score");
            Double scoreA = scoreAObj instanceof Number ? ((Number) scoreAObj).doubleValue() : 0.0;
            Double scoreB = scoreBObj instanceof Number ? ((Number) scoreBObj).doubleValue() : 0.0;
            return scoreB.compareTo(scoreA);
        });

        // 用热门商品补充
        if (result.size() < limit) {
            List<Map<String, Object>> popular = getPopularProducts(limit);
            for (Map<String, Object> item : popular) {
                Integer id = (Integer) item.get("id");
                if (!seenIds.contains(id)) {
                    seenIds.add(id);
                    result.add(item);
                    if (result.size() >= limit) break;
                }
            }
        }

        return result.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 获取热门商品作为默认推荐
     */
    private List<Map<String, Object>> getPopularProducts(Integer limit) {
        List<Product> hotProducts = productMapper.getProductList(0, limit, null, null, 1, null);
        if (hotProducts == null) {
            return new ArrayList<>();
        }
        return hotProducts.stream().map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("price", p.getPrice());
            item.put("image", p.getImage());
            item.put("sales", p.getSales());
            item.put("score", p.getSales() != null ? p.getSales() : 0);
            item.put("reason", "热门推荐");
            return item;
        }).collect(Collectors.toList());
    }
}