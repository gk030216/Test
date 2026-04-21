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
     * 获取用户订单列表
     */
    Map<String, Object> getOrderList(Integer userId, Integer page, Integer pageSize, Integer status);

    /**
     * 获取后台订单列表（全量）
     */
    Map<String, Object> getAdminOrderList(Integer page, Integer pageSize, String keyword,
                                          Integer orderStatus, Integer payStatus);

    /**
     * 取消订单
     */
    boolean cancelOrder(String orderNo, Integer userId, String reason);

    /**
     * 支付成功回调
     */
    boolean handlePayCallback(String orderNo, String tradeNo);

    /**
     * 更新订单状态（后台用）
     */
    boolean updateOrderStatus(String orderNo, Integer orderStatus);

    /**
     * 删除订单
     */
    boolean deleteOrder(Integer id);

    boolean confirmReceipt(String orderNo, Integer userId);

}