package com.pet.controller.admin;

import com.pet.entity.Order;
import com.pet.entity.OrderItem;
import com.pet.service.OrderService;
import com.pet.util.ExcelUtil;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/order")
@CrossOrigin(origins = "*")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取订单列表（后台）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) Integer payStatus) {
        try {
            Map<String, Object> result = orderService.getAdminOrderList(page, pageSize, keyword, orderStatus, payStatus);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取订单详情（后台，管理员可查看所有订单）
     */
    @GetMapping("/detail/{orderNo}")
    public Result<Order> getOrderDetail(@PathVariable String orderNo) {
        try {
            Order order = orderService.getOrderByNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }
            // 管理员无需验证用户权限，可以直接查看
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/status")
    public Result<?> updateOrderStatus(@RequestParam String orderNo, @RequestParam Integer orderStatus) {
        try {
            boolean success = orderService.updateOrderStatus(orderNo, orderStatus);
            if (success) {
                String message = getStatusMessage(orderStatus);
                return Result.success(message);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新订单状态
     */
    @PutMapping("/batch-status")
    public Result<?> batchUpdateOrderStatus(@RequestParam String ids, @RequestParam Integer orderStatus) {
        try {
            List<String> orderNoList = Arrays.stream(ids.split(","))
                    .collect(Collectors.toList());
            int successCount = 0;
            for (String orderNo : orderNoList) {
                if (orderService.updateOrderStatus(orderNo, orderStatus)) {
                    successCount++;
                }
            }
            return Result.success("批量更新成功，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteOrder(@PathVariable Integer id) {
        try {
            boolean success = orderService.deleteOrder(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除订单
     */
    @DeleteMapping("/batch-delete")
    public Result<?> batchDeleteOrders(@RequestParam String ids) {
        try {
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            int successCount = 0;
            for (Integer id : idList) {
                if (orderService.deleteOrder(id)) {
                    successCount++;
                }
            }
            return Result.success("批量删除成功，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 导出订单列表
     */
    @GetMapping("/export")
    public void exportOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) Integer payStatus,
            HttpServletResponse response) {
        try {
            Map<String, Object> result = orderService.getAdminOrderList(1, 9999, keyword, orderStatus, payStatus);
            List<Order> orderList = (List<Order>) result.get("list");

            String[] headers = {"订单号", "用户", "手机号", "收货地址", "订单金额", "订单状态", "支付状态", "下单时间", "支付时间"};
            String[] fieldNames = {"orderNo", "userName", "userPhone", "userAddress", "payAmount", "orderStatusName", "payStatusName", "createTime", "payTime"};

            for (Order order : orderList) {
                order.setOrderStatusName(getStatusName(order.getOrderStatus()));
                order.setPayStatusName(order.getPayStatus() == 1 ? "已支付" : "未支付");
            }

            ExcelUtil.exportExcel(response, orderList, headers, fieldNames, "订单列表", "订单数据_" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.getWriter().write("导出失败：" + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            case 5: return "已退款";
            default: return "未知";
        }
    }

    private String getStatusMessage(Integer orderStatus) {
        switch (orderStatus) {
            case 1: return "订单已支付";
            case 2: return "订单已发货";
            case 3: return "订单已完成";
            case 4: return "订单已取消";
            case 5: return "订单已退款";
            default: return "订单状态已更新";
        }
    }
}