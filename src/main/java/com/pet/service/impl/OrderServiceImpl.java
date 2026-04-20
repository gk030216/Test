package com.pet.service.impl;

import com.alipay.api.AlipayApiException;
import com.pet.dto.CreateOrderRequest;
import com.pet.entity.*;
import com.pet.mapper.*;
import com.pet.service.AlipayService;
import com.pet.service.NotificationService;
import com.pet.service.OrderService;
import com.pet.util.OrderNoGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private InventoryMapper inventoryMapper;  // ✅ 新增：库存Mapper

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AlipayService alipayService;

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

        // 计算总金额并验证商品库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<CreateOrderRequest.OrderItemDto> validItems = new ArrayList<>();

        for (CreateOrderRequest.OrderItemDto item : items) {
            Product product = productMapper.getById(item.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }

            // ✅ 从库存表获取库存
            Inventory inventory = inventoryMapper.getByTypeAndItemId("product", item.getProductId());
            if (inventory == null) {
                throw new RuntimeException("商品 " + product.getName() + " 库存不存在");
            }
            if (inventory.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品 " + product.getName() + " 库存不足，当前库存：" + inventory.getStock());
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
        order.setOrderType("product");

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

            // ✅ 扣减库存表（inventory）的库存
            inventoryMapper.updateStockByItemId("product", product.getId(), -item.getQuantity());

            // 删除购物车中对应的商品
            cartMapper.deleteByUserIdAndProductId(userId, product.getId());
        }

        if (!orderItems.isEmpty()) {
            orderItemMapper.batchInsert(orderItems);
        }

        order.setItems(orderItems);

        // 发送订单创建成功站内消息
        try {
            notificationService.sendNotification(
                    userId,
                    "order",
                    "订单创建成功",
                    "您的订单【" + orderNo + "】已创建成功，请尽快完成支付。",
                    "/personal/orders"
            );
            System.out.println("✅ 订单创建成功站内消息已发送");
        } catch (Exception e) {
            System.err.println("发送订单创建消息失败: " + e.getMessage());
        }

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
    public Map<String, Object> getAdminOrderList(Integer page, Integer pageSize, String keyword,
                                                 Integer orderStatus, Integer payStatus) {
        int offset = (page - 1) * pageSize;
        List<Order> list = orderMapper.getAdminOrderList(offset, pageSize, keyword, orderStatus, payStatus);
        int total = orderMapper.countAdminOrder(keyword, orderStatus, payStatus);

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
    public boolean cancelOrder(String orderNo, Integer userId, String reason) {
        Order order = orderMapper.getByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 0 && order.getOrderStatus() != 1) {
            throw new RuntimeException("当前订单状态无法取消");
        }

        boolean success;
        if (order.getPayStatus() == 1) {
            try {
                boolean refundSuccess = alipayService.refund(orderNo, order.getPayAmount(), reason);
                if (!refundSuccess) {
                    throw new RuntimeException("退款失败，请稍后重试");
                }
                success = orderMapper.updateOrderStatus(orderNo, 5, reason) > 0;
                if (success) {
                    notificationService.sendNotification(
                            userId,
                            "order",
                            "订单退款成功",
                            "您的订单【" + orderNo + "】已退款成功，退款金额 ¥" + order.getPayAmount() + " 将原路返回。",
                            "/personal/orders"
                    );
                }
            } catch (Exception e) {
                throw new RuntimeException("退款失败：" + e.getMessage());
            }
        } else {
            // 未支付，取消，恢复库存
            List<OrderItem> items = orderItemMapper.getByOrderNo(orderNo);
            for (OrderItem item : items) {
                // ✅ 恢复库存表（inventory）的库存
                inventoryMapper.updateStockByItemId("product", item.getProductId(), item.getQuantity());
            }
            success = orderMapper.updateOrderStatus(orderNo, 4, reason) > 0;
            if (success) {
                notificationService.sendNotification(
                        userId,
                        "order",
                        "订单已取消",
                        "您的订单【" + orderNo + "】已取消成功。",
                        "/personal/orders"
                );
            }
        }
        return success;
    }

    @Override
    @Transactional
    public boolean handlePayCallback(String orderNo, String tradeNo) {
        Order order = orderMapper.getByOrderNo(orderNo);
        if (order == null) {
            System.out.println("订单不存在: " + orderNo);
            return false;
        }
        if (order.getPayStatus() == 1) {
            System.out.println("订单已支付: " + orderNo);
            return true;
        }

        int result = orderMapper.updatePayStatus(orderNo, 1, 1, tradeNo);
        System.out.println("更新订单支付状态结果: " + result);

        if (result > 0) {
            // 声明 items 变量在外部
            List<OrderItem> items = null;

            try {
                items = orderItemMapper.getByOrderNo(orderNo);

                // 更新商品销量
                for (OrderItem item : items) {
                    productMapper.updateSales(item.getProductId(), item.getQuantity());
                    System.out.println("更新商品销量: productId=" + item.getProductId() + ", 数量=" + item.getQuantity());
                }

                // 删除购物车商品
                for (OrderItem item : items) {
                    cartMapper.deleteByUserIdAndProductId(order.getUserId(), item.getProductId());
                }
                System.out.println("删除购物车商品成功");

            } catch (Exception e) {
                System.err.println("更新商品销量或删除购物车失败: " + e.getMessage());
            }

            // 发送站内消息通知
            try {
                notificationService.sendNotification(
                        order.getUserId(),
                        "order",
                        "订单支付成功",
                        "您的订单【" + orderNo + "】已支付成功，共" + (items != null ? items.size() : 0) + "件商品，我们将尽快发货。",
                        "/personal/orders"
                );
                System.out.println("✅ 订单支付成功站内消息已发送");
            } catch (Exception e) {
                System.err.println("发送站内消息失败: " + e.getMessage());
            }
        }

        return result > 0;
    }

    @Override
    public boolean deleteOrder(Integer id) {
        return orderMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean confirmReceipt(String orderNo, Integer userId) {
        Order order = orderMapper.getByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 2) {
            throw new RuntimeException("当前状态无法确认收货");
        }

        boolean success = orderMapper.updateOrderStatus(orderNo, 3, null) > 0;

        if (success) {
            notificationService.sendNotification(
                    userId,
                    "order",
                    "确认收货成功",
                    "您的订单【" + orderNo + "】已确认收货，感谢您的购买！欢迎评价。",
                    "/personal/orders"
            );
            System.out.println("✅ 确认收货站内消息已发送");
        }

        return success;
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(String orderNo, Integer orderStatus) {
        return orderMapper.updateOrderStatus(orderNo, orderStatus, null) > 0;
    }
}