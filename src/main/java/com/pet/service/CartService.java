// CartService.java
package com.pet.service;

import com.pet.entity.Cart;
import java.util.List;
import java.util.Map;

public interface CartService {
    List<Cart> getCartList(Integer userId);
    boolean addToCart(Integer userId, Integer productId, Integer quantity);
    boolean updateQuantity(Integer cartId, Integer quantity);
    boolean updateSelected(Integer cartId, Integer selected);
    boolean deleteCartItem(Integer cartId);
    boolean deleteSelected(Integer userId);
    Map<String, Object> getCartSummary(Integer userId);
}