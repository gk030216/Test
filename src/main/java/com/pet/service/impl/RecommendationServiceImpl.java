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

        // 如果推荐分数为空，返回热门商品
        if (productScores.isEmpty()) {
            return getPopularProducts(limit);
        }

        // 4. 排序返回结果 - 找到最大分数用于归一化
        double maxScore = productScores.values().stream().max(Double::compareTo).orElse(1.0);

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
                    // 归一化分数到 0-1 范围
                    double normalizedScore = entry.getValue() / maxScore;
                    if (normalizedScore > 1.0) normalizedScore = 1.0;
                    item.put("score", normalizedScore);
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

        if (similarProducts == null || similarProducts.isEmpty()) {
            return getPopularProducts(limit);
        }

        // ✅ 步骤1：先计算最大相似度（普通循环）
        double maxSimilarity = 0;
        for (Map<String, Object> item : similarProducts) {
            Object similarityObj = item.get("similarity");
            double similarity = 0;
            if (similarityObj instanceof Number) {
                similarity = ((Number) similarityObj).doubleValue();
            }
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
            }
        }

        // 如果最大相似度为0，设为1避免除零
        if (maxSimilarity <= 0) {
            maxSimilarity = 1;
        }

        // ✅ 步骤2：声明 final 变量供 lambda 使用
        final double finalMaxSimilarity = maxSimilarity;

        // ✅ 步骤3：使用 lambda 处理结果
        List<Map<String, Object>> result = similarProducts.stream()
                .map(item -> {
                    Integer id = (Integer) item.get("id");

                    double similarity = 0;
                    Object similarityObj = item.get("similarity");
                    if (similarityObj instanceof Number) {
                        similarity = ((Number) similarityObj).doubleValue();
                    }

                    // 归一化到 0-1 范围
                    double normalizedScore = similarity / finalMaxSimilarity;
                    if (normalizedScore > 1.0) {
                        normalizedScore = 1.0;
                    }

                    Product product = productMapper.getById(id);
                    if (product == null) return null;

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", product.getId());
                    map.put("name", product.getName());
                    map.put("price", product.getPrice());
                    map.put("image", product.getImage());
                    map.put("sales", product.getSales());
                    map.put("score", normalizedScore);
                    map.put("reason", "浏览此商品的用户也喜欢");
                    return map;
                })
                .filter(Objects::nonNull)
                .limit(limit)
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

        // 先添加用户协同过滤的结果
        for (Map<String, Object> item : userBased) {
            Integer id = (Integer) item.get("id");
            if (!seenIds.contains(id)) {
                seenIds.add(id);
                result.add(item);
            }
        }

        // 再添加物品协同过滤的结果（不重复）
        for (Map<String, Object> item : itemBased) {
            Integer id = (Integer) item.get("id");
            if (!seenIds.contains(id)) {
                seenIds.add(id);
                // 物品协同过滤直接使用原有分数（已经是归一化的）
                result.add(item);
            }
        }

        // 按分数排序
        result.sort((a, b) -> {
            Double scoreA = getDoubleValue(a.get("score"));
            Double scoreB = getDoubleValue(b.get("score"));
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
        if (hotProducts == null || hotProducts.isEmpty()) {
            return new ArrayList<>();
        }

        // 找出最大销量用于归一化
        int maxSales = 0;
        for (Product p : hotProducts) {
            if (p.getSales() != null && p.getSales() > maxSales) {
                maxSales = p.getSales();
            }
        }
        if (maxSales <= 0) maxSales = 1;

        final int finalMaxSales = maxSales;

        return hotProducts.stream().map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("price", p.getPrice());
            item.put("image", p.getImage());
            item.put("sales", p.getSales());
            // 使用 finalMaxSales 而不是 maxSales
            double normalizedScore = (double) (p.getSales() != null ? p.getSales() : 0) / finalMaxSales;
            if (normalizedScore > 1.0) normalizedScore = 1.0;
            item.put("score", normalizedScore);
            item.put("reason", "热门推荐");
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 安全获取 Double 值
     */
    private Double getDoubleValue(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Float) return ((Float) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}