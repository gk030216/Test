package com.pet.controller;

import com.pet.dto.CreateOrderRequest;
import com.pet.dto.PayRequest;
import com.pet.entity.Order;
import com.pet.service.AlipayService;
import com.pet.service.OrderService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayService alipayService;

    private Integer getUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    private Integer getUserRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody CreateOrderRequest request, HttpServletRequest req) {
        try {
            Integer userId = getUserId(req);
            Order order = orderService.createOrder(request, userId);
            return Result.success("订单创建成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取订单详情 - 用户可查看自己的订单，管理员可查看所有订单
     */
    @GetMapping("/detail/{orderNo}")
    public Result<Order> getOrderDetail(@PathVariable String orderNo, HttpServletRequest req) {
        try {
            Integer userId = getUserId(req);
            Integer userRole = getUserRole(req);
            Order order = orderService.getOrderByNo(orderNo);

            if (order == null) {
                return Result.error("订单不存在");
            }

            // 管理员可以查看所有订单
            if (userRole != null && userRole == 3) {
                return Result.success(order);
            }

            // 普通用户只能查看自己的订单
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权查看此订单");
            }

            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取订单列表（用户端）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            HttpServletRequest req) {
        try {
            Integer userId = getUserId(req);
            Map<String, Object> result = orderService.getOrderList(userId, page, pageSize, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{orderNo}")
    public Result<?> cancelOrder(@PathVariable String orderNo, HttpServletRequest req) {
        try {
            Integer userId = getUserId(req);
            boolean success = orderService.cancelOrder(orderNo, userId);
            if (success) {
                return Result.success("取消成功");
            } else {
                return Result.error("取消失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发起支付
     */
    @PostMapping("/pay")
    public Result<String> pay(@RequestBody PayRequest request, HttpServletRequest req) {
        try {
            Integer userId = getUserId(req);
            Order order = orderService.getOrderByNo(request.getOrderNo());
            if (order == null || !order.getUserId().equals(userId)) {
                return Result.error("订单不存在");
            }
            if (order.getPayStatus() == 1) {
                return Result.error("订单已支付");
            }

            String payForm = alipayService.createPay(request.getOrderNo(), order.getPayAmount());
            return Result.success(payForm);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询支付结果
     */
    @PostMapping("/query-pay-result")
    public Result<Boolean> queryPayResult(@RequestBody PayRequest request, HttpServletRequest req) {
        try {
            Integer userId = getUserId(req);
            Order order = orderService.getOrderByNo(request.getOrderNo());
            if (order == null || !order.getUserId().equals(userId)) {
                return Result.error("订单不存在");
            }

            if (order.getPayStatus() == 1) {
                return Result.success(true);
            }

            boolean result = alipayService.queryPayResult(request.getOrderNo());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}