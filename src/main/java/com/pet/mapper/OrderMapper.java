package com.pet.mapper;

import com.pet.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {

    int insert(Order order);

    Order getByOrderNo(@Param("orderNo") String orderNo);

    List<Order> getOrderList(@Param("userId") Integer userId,
                             @Param("offset") Integer offset,
                             @Param("limit") Integer limit,
                             @Param("status") Integer status);

    int countOrder(@Param("userId") Integer userId, @Param("status") Integer status);

    int updatePayStatus(@Param("orderNo") String orderNo,
                        @Param("payStatus") Integer payStatus,
                        @Param("orderStatus") Integer orderStatus,
                        @Param("tradeNo") String tradeNo);

    int updateOrderStatus(@Param("orderNo") String orderNo, @Param("orderStatus") Integer orderStatus);
}