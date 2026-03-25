package com.pet.controller;

import com.pet.entity.Cart;
import com.pet.service.CartService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取用户ID（从Token中获取）
     */
    private Integer getUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public Result<List<Cart>> getCartList(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            List<Cart> list = cartService.getCartList(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取购物车统计
     */
    @GetMapping("/summary")
    public Result<Map<String, Object>> getCartSummary(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Map<String, Object> summary = cartService.getCartSummary(userId);
            return Result.success(summary);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Result<?> addToCart(HttpServletRequest request,
                               @RequestParam Integer productId,
                               @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            Integer userId = getUserId(request);
            boolean success = cartService.addToCart(userId, productId, quantity);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新购物车数量
     */
    @PutMapping("/quantity")
    public Result<?> updateQuantity(@RequestParam Integer cartId, @RequestParam Integer quantity) {
        try {
            boolean success = cartService.updateQuantity(cartId, quantity);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新选中状态
     */
    @PutMapping("/selected")
    public Result<?> updateSelected(@RequestParam Integer cartId, @RequestParam Integer selected) {
        try {
            boolean success = cartService.updateSelected(cartId, selected);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/delete/{cartId}")
    public Result<?> deleteCartItem(@PathVariable Integer cartId) {
        try {
            boolean success = cartService.deleteCartItem(cartId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除已选中的商品
     */
    @DeleteMapping("/delete-selected")
    public Result<?> deleteSelected(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = cartService.deleteSelected(userId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}