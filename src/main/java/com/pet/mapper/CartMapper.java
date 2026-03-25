package com.pet.mapper;

import com.pet.entity.Cart;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CartMapper {
    List<Cart> getCartList(@Param("userId") Integer userId);
    Cart getByUserAndProduct(@Param("userId") Integer userId, @Param("productId") Integer productId);
    int insert(Cart cart);
    int updateQuantity(@Param("id") Integer id, @Param("quantity") Integer quantity);
    int updateSelected(@Param("id") Integer id, @Param("selected") Integer selected);
    int delete(@Param("id") Integer id);
    int deleteSelected(@Param("userId") Integer userId);
    int batchDelete(@Param("ids") List<Integer> ids);
}