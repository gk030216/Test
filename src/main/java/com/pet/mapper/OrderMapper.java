package com.pet.mapper;

import com.pet.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {

    int insert(Order order);

    Order getByOrderNo(@Param("orderNo") String orderNo);

    // 用户订单列表
    List<Order> getOrderList(@Param("userId") Integer userId,
                             @Param("offset") Integer offset,
                             @Param("limit") Integer limit,
                             @Param("status") Integer status);

    int countOrder(@Param("userId") Integer userId, @Param("status") Integer status);

    // 后台订单列表（全量）
    List<Order> getAdminOrderList(@Param("offset") Integer offset,
                                  @Param("limit") Integer limit,
                                  @Param("keyword") String keyword,
                                  @Param("orderStatus") Integer orderStatus,
                                  @Param("payStatus") Integer payStatus);

    int countAdminOrder(@Param("keyword") String keyword,
                        @Param("orderStatus") Integer orderStatus,
                        @Param("payStatus") Integer payStatus);

    int updatePayStatus(@Param("orderNo") String orderNo,
                        @Param("payStatus") Integer payStatus,
                        @Param("orderStatus") Integer orderStatus,
                        @Param("tradeNo") String tradeNo);

    int updateOrderStatus(@Param("orderNo") String orderNo, @Param("orderStatus") Integer orderStatus);

    int deleteById(@Param("id") Integer id);
}