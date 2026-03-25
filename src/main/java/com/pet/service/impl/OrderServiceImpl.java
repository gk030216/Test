package com.pet.service.impl;

import com.pet.dto.CreateOrderRequest;
import com.pet.entity.*;
import com.pet.mapper.*;
import com.pet.service.OrderService;
import com.pet.util.OrderNoGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request, Integer userId) {
        // 获取收货地址
        Address address = addressMapper.getById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("收货地址无效");
        }

        List<CreateOrderRequest.OrderItemDto> items = request.getItems();
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("请选择要购买的商品");
        }

        // 计算总金额并验证商品
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<CreateOrderRequest.OrderItemDto> validItems = new ArrayList<>();

        for (CreateOrderRequest.OrderItemDto item : items) {
            Product product = productMapper.getById(item.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品 " + product.getName() + " 库存不足");
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
            validItems.add(item);
        }

        // 生成订单号
        String orderNo = OrderNoGenerator.generate();

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setUserName(address.getReceiverName());
        order.setUserPhone(address.getReceiverPhone());
        order.setUserAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setPayStatus(0);
        order.setOrderStatus(0);

        orderMapper.insert(order);

        // 创建订单详情并扣减库存
        List<OrderItem> orderItems = new ArrayList<>();
        for (CreateOrderRequest.OrderItemDto item : validItems) {
            Product product = productMapper.getById(item.getProductId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderNo(orderNo);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setTotalAmount(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
            orderItems.add(orderItem);

            // 扣减库存
            productMapper.updateStock(product.getId(), -item.getQuantity());
        }

        if (!orderItems.isEmpty()) {
            orderItemMapper.batchInsert(orderItems);
        }

        // 设置订单详情
        order.setItems(orderItems);

        // 如果是通过购物车结算，删除购物车中对应的商品
        // 这里根据实际情况决定是否需要删除购物车商品

        return order;
    }

    @Override
    public Order getOrderByNo(String orderNo) {
        Order order = orderMapper.getByOrderNo(orderNo);
        if (order != null) {
            List<OrderItem> items = orderItemMapper.getByOrderNo(orderNo);
            order.setItems(items);
        }
        return order;
    }

    @Override
    public Map<String, Object> getOrderList(Integer userId, Integer page, Integer pageSize, Integer status) {
        int offset = (page - 1) * pageSize;
        List<Order> list = orderMapper.getOrderList(userId, offset, pageSize, status);
        int total = orderMapper.countOrder(userId, status);

        // 获取每个订单的详情
        for (Order order : list) {
            List<OrderItem> items = orderItemMapper.getByOrderNo(order.getOrderNo());
            order.setItems(items);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    @Transactional
    public boolean cancelOrder(String orderNo, Integer userId) {
        Order order = orderMapper.getByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("订单状态不允许取消");
        }

        // 恢复库存
        List<OrderItem> items = orderItemMapper.getByOrderNo(orderNo);
        for (OrderItem item : items) {
            productMapper.updateStock(item.getProductId(), item.getQuantity());
        }

        // 更新订单状态
        return orderMapper.updateOrderStatus(orderNo, 4) > 0;
    }

    @Override
    @Transactional
    public boolean handlePayCallback(String orderNo, String tradeNo) {
        Order order = orderMapper.getByOrderNo(orderNo);
        if (order == null) {
            return false;
        }
        if (order.getPayStatus() == 1) {
            return true; // 已支付
        }

        // 更新订单支付状态
        return orderMapper.updatePayStatus(orderNo, 1, 1, tradeNo) > 0;
    }
}