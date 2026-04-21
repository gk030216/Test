package com.pet.controller;

import com.pet.entity.Product;
import com.pet.mapper.ProductFavoriteMapper;
import com.pet.service.ProductService;
import com.pet.service.RecommendationService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductFavoriteMapper favoriteMapper;

    @Autowired
    private RecommendationService recommendationService;


    @Autowired
    private ProductFavoriteMapper productFavoriteMapper;

    /**
     * 获取当前登录用户ID
     */
    private Integer getUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    /**
     * 获取商品列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer isHot,
            @RequestParam(required = false) Integer isNew) {
        try {
            Map<String, Object> result = productService.getProductList(page, pageSize, keyword, categoryId, isHot, isNew);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Integer id) {
        try {
            Product product = productService.getProductById(id);
            if (product != null) {
                return Result.success(product);
            } else {
                return Result.error("商品不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取热门商品
     */
    @GetMapping("/hot")
    public Result<List<Product>> getHotProducts() {
        try {
            List<Product> list = productService.getHotProducts();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取新品推荐
     */
    @GetMapping("/new")
    public Result<List<Product>> getNewProducts() {
        try {
            List<Product> list = productService.getNewProducts();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加收藏
     */
    @PostMapping("/favorite/{productId}")
    public Result<?> addFavorite(@PathVariable Integer productId, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            int result = favoriteMapper.insert(userId, productId);
            return result > 0 ? Result.success("收藏成功") : Result.error("收藏失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/favorite/{productId}")
    public Result<?> removeFavorite(@PathVariable Integer productId, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            int result = favoriteMapper.delete(userId, productId);
            return result > 0 ? Result.success("取消成功") : Result.error("取消失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查收藏状态
     */
    @GetMapping("/favorite/{productId}/check")
    public Result<Boolean> checkFavorite(@PathVariable Integer productId, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            int count = favoriteMapper.checkFavorite(userId, productId);
            return Result.success(count > 0);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/favorites")
    public Result<List<Integer>> getUserFavorites(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            List<Integer> favorites = favoriteMapper.getUserFavorites(userId);
            return Result.success(favorites);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取协同过滤推荐
     */
    @GetMapping("/recommend")
    public Result<List<Map<String, Object>>> getRecommendProducts(
            @RequestParam(required = false) Integer productId,
            @RequestParam(defaultValue = "8") Integer limit,
            HttpServletRequest request) {
        try {
            Integer userId = null;
            try {
                userId = getUserId(request);
            } catch (Exception e) {
                // 未登录用户，userId 为 null
            }

            List<Map<String, Object>> list;
            if (userId != null) {
                // 登录用户使用混合推荐
                list = recommendationService.getHybridRecommendations(userId, productId, limit);
            } else {
                // 未登录用户：如果浏览了商品则用物品协同过滤，否则用热门商品
                if (productId != null) {
                    list = recommendationService.getItemBasedRecommendations(productId, limit);
                } else {
                    // 未登录且没有浏览商品，返回热门商品
                    List<Product> hotProducts = productService.getHotProducts();
                    list = new java.util.ArrayList<>();
                    for (Product p : hotProducts) {
                        if (list.size() >= limit) break;
                        Map<String, Object> item = new java.util.HashMap<>();
                        item.put("id", p.getId());
                        item.put("name", p.getName());
                        item.put("price", p.getPrice());
                        item.put("image", p.getImage());
                        item.put("sales", p.getSales());
                        item.put("score", 0);
                        item.put("reason", "热门推荐");
                        list.add(item);
                    }
                }
            }

            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户浏览历史推荐
     */
    @GetMapping("/recommend/user")
    public Result<List<Map<String, Object>>> getUserRecommendations(
            @RequestParam(defaultValue = "8") Integer limit,
            HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            List<Map<String, Object>> list = recommendationService.getUserBasedRecommendations(userId, limit);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取相似商品推荐
     */
    @GetMapping("/recommend/similar/{productId}")
    public Result<List<Map<String, Object>>> getSimilarProducts(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "8") Integer limit) {
        try {
            List<Map<String, Object>> list = recommendationService.getItemBasedRecommendations(productId, limit);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/favorites/detail")
    public Result<Map<String, Object>> getProductFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer pageSize,
            HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            int offset = (page - 1) * pageSize;
            List<Product> list = productFavoriteMapper.getFavoriteProducts(userId, offset, pageSize);
            int total = productFavoriteMapper.countFavoriteProducts(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}