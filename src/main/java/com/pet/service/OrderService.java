package com.pet.service;

import com.pet.dto.CreateOrderRequest;
import com.pet.entity.Order;
import java.util.Map;

public interface OrderService {

    /**
     * 创建订单
     */
    Order createOrder(CreateOrderRequest request, Integer userId);

    /**
     * 获取订单详情
     */
    Order getOrderByNo(String orderNo);

    /**
     * 获取订单列表
     */
    Map<String, Object> getOrderList(Integer userId, Integer page, Integer pageSize, Integer status);

    /**
     * 取消订单
     */
    boolean cancelOrder(String orderNo, Integer userId);

    /**
     * 支付成功回调
     */
    boolean handlePayCallback(String orderNo, String tradeNo);
}