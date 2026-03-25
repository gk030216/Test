// CartServiceImpl.java
package com.pet.service.impl;

import com.pet.entity.Cart;
import com.pet.entity.Product;
import com.pet.mapper.CartMapper;
import com.pet.mapper.ProductMapper;
import com.pet.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Cart> getCartList(Integer userId) {
        return cartMapper.getCartList(userId);
    }

    @Override
    @Transactional
    public boolean addToCart(Integer userId, Integer productId, Integer quantity) {
        // 检查商品是否存在
        Product product = productMapper.getById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }

        // 检查购物车是否已有该商品
        Cart existingCart = cartMapper.getByUserAndProduct(userId, productId);
        if (existingCart != null) {
            // 更新数量
            int newQuantity = existingCart.getQuantity() + quantity;
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("库存不足");
            }
            cartMapper.updateQuantity(existingCart.getId(), newQuantity);
        } else {
            // 新增购物车项
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setProductName(product.getName());
            cart.setProductImage(product.getImage());
            cart.setPrice(product.getPrice());
            cart.setQuantity(quantity);
            cart.setSelected(1);
            cartMapper.insert(cart);
        }
        return true;
    }

    @Override
    public boolean updateQuantity(Integer cartId, Integer quantity) {
        if (quantity <= 0) {
            return cartMapper.delete(cartId) > 0;
        }
        return cartMapper.updateQuantity(cartId, quantity) > 0;
    }

    @Override
    public boolean updateSelected(Integer cartId, Integer selected) {
        return cartMapper.updateSelected(cartId, selected) > 0;
    }

    @Override
    public boolean deleteCartItem(Integer cartId) {
        return cartMapper.delete(cartId) > 0;
    }

    @Override
    public boolean deleteSelected(Integer userId) {
        return cartMapper.deleteSelected(userId) > 0;
    }

    @Override
    public Map<String, Object> getCartSummary(Integer userId) {
        List<Cart> cartList = cartMapper.getCartList(userId);
        int totalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cart : cartList) {
            if (cart.getSelected() == 1) {
                totalCount += cart.getQuantity();
                totalAmount = totalAmount.add(cart.getPrice().multiply(new BigDecimal(cart.getQuantity())));
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", totalCount);
        summary.put("totalAmount", totalAmount);
        return summary;
    }
}