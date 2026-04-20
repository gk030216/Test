package com.pet.controller.admin;

import com.pet.entity.Order;
import com.pet.entity.OrderItem;
import com.pet.service.NotificationService;
import com.pet.service.OrderService;
import com.pet.util.ExcelUtil;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/order")
@CrossOrigin(origins = "*")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private NotificationService notificationService;

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
                // 发货时发送通知
                if (orderStatus == 2) {
                    Order order = orderService.getOrderByNo(orderNo);
                    if (order != null) {
                        notificationService.sendNotification(
                                order.getUserId(),
                                "order",
                                "订单已发货",
                                "您的订单【" + orderNo + "】已发货，请留意查收。",
                                "/personal/orders"
                        );
                        System.out.println("✅ 订单发货站内消息已发送");
                    }
                }
                String message = getStatusMessage(orderStatus);
                return Result.success(message);
            }
            return Result.error("更新失败");
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
    public void exportOrderList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer orderStatus,
            HttpServletResponse response) {
        try {
            // 修复：传入5个参数，payStatus传null
            Map<String, Object> result = orderService.getAdminOrderList(1, 9999, keyword, orderStatus, null);
            @SuppressWarnings("unchecked")
            List<Order> list = (List<Order>) result.get("list");

            List<Map<String, Object>> exportList = new ArrayList<>();
            for (Order item : list) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", item.getId());
                row.put("orderNo", item.getOrderNo());
                row.put("userName", item.getUserName());
                row.put("userPhone", item.getUserPhone());
                row.put("userAddress", item.getUserAddress());
                row.put("payAmount", item.getPayAmount());
                row.put("orderStatus", getOrderStatusText(item.getOrderStatus()));
                row.put("payStatus", item.getPayStatus() == 1 ? "已支付" : "未支付");
                row.put("createTime", formatDateTime(item.getCreateTime()));
                exportList.add(row);
            }

            String[] headers = {"ID", "订单号", "用户", "手机号", "收货地址", "金额", "订单状态", "支付状态", "下单时间"};
            String[] fieldNames = {"id", "orderNo", "userName", "userPhone", "userAddress", "payAmount", "orderStatus", "payStatus", "createTime"};

            String fileName = "订单列表_" + System.currentTimeMillis() + ".xlsx";
            ExcelUtil.exportExcel(response, exportList, headers, fieldNames, "订单列表", fileName);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("导出失败：" + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // 获取订单状态对应的消息文本
    private String getStatusMessage(Integer orderStatus) {
        Map<Integer, String> map = new HashMap<>();
        map.put(0, "订单已取消");
        map.put(1, "订单已支付");
        map.put(2, "订单已发货");
        map.put(3, "订单已完成");
        map.put(4, "订单已取消");
        map.put(5, "订单已退款");
        return map.getOrDefault(orderStatus, "状态已更新");
    }

    // 订单状态文本转换（用于导出）
    private String getOrderStatusText(Integer status) {
        Map<Integer, String> map = new HashMap<>();
        map.put(0, "待支付");
        map.put(1, "已支付");
        map.put(2, "已发货");
        map.put(3, "已完成");
        map.put(4, "已取消");
        map.put(5, "已退款");
        return map.getOrDefault(status, "未知");
    }

    // 格式化日期时间
    private String formatDateTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}