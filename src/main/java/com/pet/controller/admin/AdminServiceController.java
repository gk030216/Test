package com.pet.controller.admin;

import com.pet.entity.ServiceCategory;
import com.pet.entity.ServiceComment;
import com.pet.entity.ServiceItem;
import com.pet.entity.Appointment;
import com.pet.mapper.AppointmentMapper;
import com.pet.mapper.ServiceCommentMapper;
import com.pet.service.*;
import com.pet.util.ExcelUtil;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/service")
@CrossOrigin(origins = "*")
public class AdminServiceController {

    @Autowired
    private ServiceCategoryService categoryService;

    @Autowired
    private ServiceItemService itemService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ServiceCommentMapper commentMapper;

    @Autowired
    private StaffMatchService staffMatchService;

    @Autowired
    private ServiceCommentService serviceCommentService;

    private Integer getUserId(HttpServletRequest request) {
        return (Integer) request.getAttribute("userId");
    }

    private Integer getUserRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    // ========== 服务分类管理 ==========

    /**
     * 获取分类列表（分页 - 用于表格显示）
     */
    @GetMapping("/category/list")
    public Result<Map<String, Object>> getCategoryList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            Map<String, Object> result = categoryService.getCategoryList(page, pageSize, keyword, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有分类（用于父分类下拉选择）
     */
    @GetMapping("/category/all")
    public Result<List<ServiceCategory>> getAllCategories(HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            List<ServiceCategory> list = categoryService.getAllCategories();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/category/{id}")
    public Result<ServiceCategory> getCategoryById(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            ServiceCategory category = categoryService.getById(id);
            return Result.success(category);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加分类
     */
    @PostMapping("/category/add")
    public Result<?> addCategory(@RequestBody ServiceCategory category, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = categoryService.addCategory(category);
            return success ? Result.success("添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新分类
     */
    @PutMapping("/category/update")
    public Result<?> updateCategory(@RequestBody ServiceCategory category, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = categoryService.updateCategory(category);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新分类状态
     */
    @PutMapping("/category/status")
    public Result<?> updateCategoryStatus(@RequestParam Integer id, @RequestParam Integer status, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = categoryService.updateStatus(id, status);
            return success ? Result.success(status == 1 ? "启用成功" : "禁用成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/category/delete/{id}")
    public Result<?> deleteCategory(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = categoryService.deleteCategory(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除分类
     */
    @DeleteMapping("/category/batch-delete")
    public Result<?> batchDeleteCategories(@RequestParam String ids, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            String[] idArray = ids.split(",");
            int successCount = 0;
            for (String idStr : idArray) {
                if (categoryService.deleteCategory(Integer.parseInt(idStr))) {
                    successCount++;
                }
            }
            return Result.success("批量删除成功，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 服务项目管理 ==========

    /**
     * 获取服务列表（后台）
     */
    @GetMapping("/item/list")
    public Result<Map<String, Object>> getItemList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            Map<String, Object> result = itemService.getAdminItemList(page, pageSize, keyword, categoryId, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加服务
     */
    @PostMapping("/item/add")
    public Result<?> addItem(@RequestBody ServiceItem item, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = itemService.addItem(item);
            return success ? Result.success("添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新服务
     */
    @PutMapping("/item/update")
    public Result<?> updateItem(@RequestBody ServiceItem item, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = itemService.updateItem(item);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新服务状态
     */
    @PutMapping("/item/status")
    public Result<?> updateItemStatus(@RequestParam Integer id, @RequestParam Integer status, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = itemService.updateStatus(id, status);
            return success ? Result.success(status == 1 ? "上架成功" : "下架成功") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除服务
     */
    @DeleteMapping("/item/delete/{id}")
    public Result<?> deleteItem(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = itemService.deleteItem(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除服务
     */
    @DeleteMapping("/item/batch-delete")
    public Result<?> batchDeleteItems(@RequestParam String ids, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            String[] idArray = ids.split(",");
            int successCount = 0;
            for (String idStr : idArray) {
                if (itemService.deleteItem(Integer.parseInt(idStr))) {
                    successCount++;
                }
            }
            return Result.success("批量删除成功，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 预约管理 ==========

    /**
     * 获取预约列表（后台）
     * 管理员看全部，员工只看分配给自己的
     */
    @GetMapping("/appointment/list")
    public Result<Map<String, Object>> getAppointmentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            Integer userId = getUserId(request);

            // 权限检查：只有员工和管理员能访问
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }

            // 判断是否是员工
            Boolean isStaff = (role == 2);
            Integer staffId = isStaff ? userId : null;

            Map<String, Object> result = appointmentService.getAdminAppointments(
                    page, pageSize, keyword, status, startDate, endDate, staffId, isStaff
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取预约详情（后台）
     */
    @GetMapping("/appointment/{id}")
    public Result<Appointment> getAppointmentDetail(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            Appointment appointment = appointmentService.getByIdForAdmin(id);
            return Result.success(appointment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 确认预约（支持指定员工）
     */
    @PutMapping("/appointment/confirm/{id}")
    public Result<?> confirmAppointment(@PathVariable Integer id,
                                        @RequestBody(required = false) Map<String, Integer> params,
                                        HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            System.out.println("当前用户角色: " + role);

            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }

            Integer staffId;
            Integer oldStaffId = null;

            // 获取原预约信息（用于冲突检查）
            Appointment oldAppointment = appointmentService.getByIdForAdmin(id);
            if (oldAppointment == null) {
                return Result.error("预约不存在");
            }
            oldStaffId = oldAppointment.getStaffId();

            // 管理员：使用前端传递的员工ID
            if (role == 3 && params != null && params.get("staffId") != null) {
                staffId = params.get("staffId");
                System.out.println("管理员分配员工ID: " + staffId);
            }
            // 员工：分配给当前员工自己
            else if (role == 2) {
                staffId = getUserId(request);
                System.out.println("员工自己确认，员工ID: " + staffId);
            }
            // 管理员但没有传递员工ID（不应该发生）
            else {
                return Result.error("请选择要分配的员工");
            }

            // ✅ 检查员工时间冲突
            int staffConflict = appointmentMapper.checkStaffTimeConflict(
                    staffId,
                    oldAppointment.getAppointmentDate(),
                    oldAppointment.getAppointmentTime(),
                    id
            );
            if (staffConflict > 0) {
                return Result.error("该员工在所选时间段已有其他预约，请选择其他员工");
            }

            boolean success = appointmentService.confirmAppointment(id, staffId);

            if (success) {
                Appointment appointment = appointmentService.getByIdForAdmin(id);

                // 1. 通知用户
                if (appointment != null) {
                    notificationService.sendNotification(
                            appointment.getUserId(),
                            "appointment",
                            "预约已确认",
                            "您的服务【" + appointment.getServiceName() + "】已确认，请按时到达。",
                            "/personal/appointments"
                    );
                    System.out.println("✅ 已通知用户: " + appointment.getUserId());
                }

                // 2. 通知被分配的员工
                notificationService.sendNotification(
                        staffId,
                        "appointment",
                        "您有新的预约需要处理",
                        "预约编号：" + appointment.getAppointmentNo() + "，服务：" + appointment.getServiceName() +
                                "，时间：" + appointment.getAppointmentDate() + " " + appointment.getAppointmentTime(),
                        "/staff/appointments"
                );
                System.out.println("✅ 已通知新员工: " + staffId);

                // 3. 如果是重新分配，通知原员工
                if (oldStaffId != null && !oldStaffId.equals(staffId)) {
                    notificationService.sendNotification(
                            oldStaffId,
                            "appointment",
                            "预约已被重新分配",
                            "您之前负责的预约【" + appointment.getServiceName() + "】已被重新分配给其他员工。",
                            "/staff/appointments"
                    );
                    System.out.println("✅ 已通知原员工: " + oldStaffId);
                }

                return Result.success("确认成功");
            }
            return Result.error("确认失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }


    /**
     * 开始服务
     */
    @PutMapping("/appointment/start/{id}")
    public Result<?> startAppointment(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }

            if (role == 2) {
                Appointment appointment = appointmentService.getByIdForAdmin(id);
                Integer staffId = getUserId(request);
                if (appointment.getStaffId() == null || !appointment.getStaffId().equals(staffId)) {
                    return Result.error("只能操作分配给自己的预约");
                }
            }

            boolean success = appointmentService.startAppointment(id);

            if (success) {
                // ✅ 发送站内消息通知
                Appointment appointment = appointmentService.getByIdForAdmin(id);
                if (appointment != null) {
                    notificationService.sendNotification(
                            appointment.getUserId(),
                            "appointment",
                            "服务已开始",
                            "您的服务【" + appointment.getServiceName() + "】已开始，工作人员正在为您服务。",
                            "/personal/appointments"
                    );
                }
                return Result.success("服务已开始");
            }
            return Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 完成预约
     */
    @PutMapping("/appointment/complete/{id}")
    public Result<?> completeAppointment(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }

            if (role == 2) {
                Appointment appointment = appointmentService.getByIdForAdmin(id);
                Integer staffId = getUserId(request);
                if (appointment.getStaffId() == null || !appointment.getStaffId().equals(staffId)) {
                    return Result.error("只能操作分配给自己的预约");
                }
            }

            boolean success = appointmentService.completeAppointment(id);

            if (success) {
                // ✅ 发送站内消息通知
                Appointment appointment = appointmentService.getByIdForAdmin(id);
                if (appointment != null) {
                    notificationService.sendNotification(
                            appointment.getUserId(),
                            "appointment",
                            "服务已完成",
                            "您的服务【" + appointment.getServiceName() + "】已完成，欢迎评价！",
                            "/personal/appointments"
                    );
                }
                return Result.success("已完成");
            }
            return Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 拒绝预约
     */
    @PutMapping("/appointment/reject/{id}")
    public Result<?> rejectAppointment(@PathVariable Integer id,
                                       @RequestBody Map<String, String> params,
                                       HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            String reason = params.get("reason");
            boolean success = appointmentService.rejectAppointment(id, reason);

            if (success) {
                // ✅ 发送站内消息通知
                Appointment appointment = appointmentService.getByIdForAdmin(id);
                if (appointment != null) {
                    String content = "您的服务【" + appointment.getServiceName() + "】已被拒绝";
                    if (reason != null && !reason.isEmpty()) {
                        content += "，原因：" + reason;
                    }
                    notificationService.sendNotification(
                            appointment.getUserId(),
                            "appointment",
                            "预约被拒绝",
                            content,
                            "/personal/appointments"
                    );
                }
                return Result.success("已拒绝");
            }
            return Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消预约（管理员/员工）
     */
    @PutMapping("/appointment/cancel/{id}")
    public Result<?> cancelAppointment(@PathVariable Integer id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            String reason = params.get("reason");
            boolean success = appointmentService.cancelAppointmentByAdmin(id, reason);
            return success ? Result.success("取消成功") : Result.error("取消失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取预约统计
     * 管理员看全部，员工看自己的
     */
    @GetMapping("/appointment/statistics")
    public Result<Map<String, Object>> getAppointmentStatistics(HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }

            Map<String, Object> stats;
            if (role == 2) {
                // 员工只看自己的统计数据
                Integer staffId = getUserId(request);
                stats = appointmentService.getStaffStatistics(staffId);
            } else {
                // 管理员看全部
                stats = appointmentService.getStatistics();
            }
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取服务统计数据（基于筛选条件）
     */
    @GetMapping("/item/statistics")
    public Result<Map<String, Object>> getItemStatistics(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            Map<String, Object> stats = itemService.getStatistics(keyword, categoryId, status);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新分类排序
     */
    @PutMapping("/category/batch-sort")
    public Result<?> batchUpdateSort(@RequestBody List<Map<String, Integer>> sortList) {
        System.out.println("========== 收到排序更新请求 ==========");
        System.out.println("接收到的数据: " + sortList);
        try {
            if (sortList == null || sortList.isEmpty()) {
                return Result.error("排序数据不能为空");
            }
            categoryService.batchUpdateSort(sortList);
            System.out.println("排序更新成功");
            return Result.success("排序更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新分类状态
     */
    @PutMapping("/category/batch-status")
    public Result<?> batchUpdateStatus(@RequestParam String ids, @RequestParam Integer status) {
        try {
            String[] idArray = ids.split(",");
            int successCount = 0;
            for (String idStr : idArray) {
                if (categoryService.updateStatus(Integer.parseInt(idStr), status)) {
                    successCount++;
                }
            }
            return Result.success("批量" + (status == 1 ? "启用" : "禁用") + "成功，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 导出分类列表
     */
    @GetMapping("/category/export")
    public void exportCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) {
        try {
            List<ServiceCategory> list = categoryService.getAllCategories(keyword);
            if (status != null) {
                list = list.stream().filter(c -> c.getStatus().equals(status)).collect(Collectors.toList());
            }

            String[] headers = {"ID", "分类名称", "排序值", "服务数量", "状态", "创建时间"};
            String[] fieldNames = {"id", "name", "sortOrder", "serviceCount", "statusName", "createTime"};

            for (ServiceCategory item : list) {
                item.setStatusName(item.getStatus() == 1 ? "启用" : "禁用");
            }

            ExcelUtil.exportExcel(response, list, headers, fieldNames, "服务分类", "服务分类_" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出服务列表
     */
    @GetMapping("/item/export")
    public void exportServiceList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) {
        try {
            // 获取所有数据（不分页）
            Map<String, Object> result = itemService.getAdminItemList(1, 9999, keyword, categoryId, status);
            List<ServiceItem> list = (List<ServiceItem>) result.get("list");

            // 准备导出数据
            List<Map<String, Object>> exportList = new ArrayList<>();
            for (ServiceItem item : list) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", item.getId());
                row.put("name", item.getName());
                row.put("categoryName", item.getCategoryName() != null ? item.getCategoryName() : "未分类");
                row.put("price", item.getPrice());
                row.put("originalPrice", item.getOriginalPrice() != null ? item.getOriginalPrice() : "");
                row.put("duration", item.getDuration() + "分钟");
                row.put("sales", item.getSales() != null ? item.getSales() : 0);
                row.put("isHot", item.getIsHot() != null && item.getIsHot() == 1 ? "是" : "否");
                row.put("status", item.getStatus() != null && item.getStatus() == 1 ? "上架" : "下架");
                row.put("suitableFor", getSuitableText(item.getSuitableFor()));
                row.put("description", item.getDescription() != null ? item.getDescription() : "");
                row.put("createTime", formatDate(item.getCreateTime()));
                exportList.add(row);
            }

            // ✅ 定义表头和字段名
            String[] headers = {"ID", "服务名称", "分类", "价格", "原价", "时长", "销量", "热门", "状态", "适用对象", "描述", "创建时间"};
            String[] fieldNames = {"id", "name", "categoryName", "price", "originalPrice", "duration", "sales", "isHot", "status", "suitableFor", "description", "createTime"};

            // ✅ 文件名不要重复添加 .xlsx
            String fileName = "服务列表_" + System.currentTimeMillis() + ".xlsx";

            // ✅ 调用 ExcelUtil 导出（响应头在 ExcelUtil 中设置）
            ExcelUtil.exportExcel(response, exportList, headers, fieldNames, "服务列表", fileName);

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

    private String getSuitableText(String suitableFor) {
        if (suitableFor == null) return "全部";
        switch (suitableFor) {
            case "dog": return "狗狗";
            case "cat": return "猫咪";
            default: return "全部";
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 导出预约列表
     */
    @GetMapping("/appointment/export")
    public void exportAppointmentList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) {
        try {
            // 获取所有数据（不分页）
            Map<String, Object> result = appointmentService.getAdminAppointments(
                    1, 9999, keyword, status, startDate, endDate, null, false
            );
            List<Appointment> list = (List<Appointment>) result.get("list");

            // 准备导出数据
            List<Map<String, Object>> exportList = new ArrayList<>();
            for (Appointment item : list) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", item.getId());
                row.put("appointmentNo", item.getAppointmentNo());
                row.put("userName", item.getUserName());
                row.put("petName", item.getPetName() != null ? item.getPetName() : "未指定");
                row.put("serviceName", item.getServiceName());
                row.put("servicePrice", item.getServicePrice());
                row.put("appointmentDate", formatDate(item.getAppointmentDate()));
                row.put("appointmentTime", item.getAppointmentTime());
                row.put("staffName", item.getStaffName() != null ? item.getStaffName() : "待分配");
                row.put("status", getStatusText(item.getStatus()));
                row.put("cancelReason", item.getCancelReason() != null ? item.getCancelReason() : "");
                row.put("remark", item.getRemark() != null ? item.getRemark() : "");
                row.put("createTime", formatDateTime(item.getCreateTime()));
                exportList.add(row);
            }

            // 定义表头和字段名
            String[] headers = {"ID", "预约编号", "用户", "宠物", "服务", "金额", "预约日期", "预约时段", "服务人员", "状态", "取消/拒绝原因", "备注", "下单时间"};
            String[] fieldNames = {"id", "appointmentNo", "userName", "petName", "serviceName", "servicePrice",
                    "appointmentDate", "appointmentTime", "staffName", "status", "cancelReason", "remark", "createTime"};

            String fileName = "预约列表_" + System.currentTimeMillis() + ".xlsx";

            ExcelUtil.exportExcel(response, exportList, headers, fieldNames, "预约列表", fileName);

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

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待确认";
            case 1: return "已确认";
            case 2: return "服务中";
            case 3: return "已完成";
            case 4: return "已取消";
            case 5: return "已拒绝";
            default: return "未知";
        }
    }

    private String formatDateTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 获取员工列表及匹配度（用于分配预约）
     */
    @GetMapping("/staff/match")
    public Result<List<Map<String, Object>>> getStaffWithMatchScore(
            @RequestParam Integer appointmentId,
            @RequestParam String appointmentDate,
            @RequestParam String appointmentTime,
            @RequestParam Integer serviceId) {
        try {
            System.out.println("========== 获取员工匹配度 ==========");
            System.out.println("预约ID: " + appointmentId);
            System.out.println("预约日期: " + appointmentDate);
            System.out.println("预约时间: " + appointmentTime);
            System.out.println("服务ID: " + serviceId);

            // ✅ 修复：日期已经是字符串格式 yyyy-MM-dd
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Appointment appointment = new Appointment();
            appointment.setId(appointmentId);
            appointment.setAppointmentDate(sdf.parse(appointmentDate));
            appointment.setAppointmentTime(appointmentTime);
            appointment.setServiceId(serviceId);

            List<Map<String, Object>> staffList = staffMatchService.getStaffWithMatchScore(appointment);
            System.out.println("返回员工数量: " + staffList.size());
            return Result.success(staffList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // ========== 服务评价分析接口 ==========

    /**
     * 获取服务评价统计（用于服务评价分析页面）
     */
    @GetMapping("/comment/service/{serviceId}/statistics")
    public Result<Map<String, Object>> getServiceCommentStatistics(@PathVariable Integer serviceId) {
        try {
            Map<String, Object> statistics = getServiceCommentStats(serviceId);
            return Result.success(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取服务评价列表（用于服务评价分析页面）
     */
    @GetMapping("/comment/service/{serviceId}/list")
    public Result<Map<String, Object>> getServiceCommentsList(
            @PathVariable Integer serviceId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer rating) {
        try {
            Map<String, Object> result = getServiceCommentsListByServiceId(serviceId, page, pageSize, rating);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取服务评价统计（内部方法）
     */
    private Map<String, Object> getServiceCommentStats(Integer serviceId) {
        Map<String, Object> result = new HashMap<>();

        // 获取评分统计
        Map<String, Object> stats = commentMapper.getRatingStats(serviceId);

        // 获取评分分布
        List<Map<String, Object>> ratingDistribution = getServiceRatingDistribution(serviceId);

        // 获取近7天评价趋势
        List<Map<String, Object>> commentTrend = getServiceCommentTrend(serviceId);

        int totalComments = 0;
        double avgRating = 0;
        if (stats != null && stats.get("total_count") != null) {
            totalComments = Integer.parseInt(stats.get("total_count").toString());
            avgRating = stats.get("avg_rating") != null ? Double.parseDouble(stats.get("avg_rating").toString()) : 0;
        }

        // 计算好评率（4星及以上）
        int positiveCount = 0;
        if (ratingDistribution != null) {
            for (Map<String, Object> item : ratingDistribution) {
                int rating = Integer.parseInt(item.get("rating").toString());
                int count = Integer.parseInt(item.get("count").toString());
                if (rating >= 4) {
                    positiveCount += count;
                }
            }
        }
        double positiveRate = totalComments > 0 ? (positiveCount * 100.0 / totalComments) : 0;

        // 计算回复率
        int repliedCount = getServiceRepliedCommentCount(serviceId);
        double replyRate = totalComments > 0 ? (repliedCount * 100.0 / totalComments) : 0;

        result.put("totalComments", totalComments);
        result.put("avgRating", Math.round(avgRating * 10) / 10.0);
        result.put("positiveRate", Math.round(positiveRate));
        result.put("replyRate", Math.round(replyRate));
        result.put("ratingDistribution", ratingDistribution != null ? ratingDistribution : new ArrayList<>());
        result.put("commentTrend", commentTrend != null ? commentTrend : new ArrayList<>());

        return result;
    }

    /**
     * 获取服务评价列表（内部方法）
     */
    private Map<String, Object> getServiceCommentsListByServiceId(Integer serviceId, Integer page, Integer pageSize, Integer rating) {
        int offset = (page - 1) * pageSize;
        List<ServiceComment> list = getServiceCommentListWithRating(serviceId, offset, pageSize, rating);
        int total = countServiceCommentWithRating(serviceId, rating);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * 获取服务评分分布
     */
    private List<Map<String, Object>> getServiceRatingDistribution(Integer serviceId) {
        return commentMapper.getServiceRatingDistribution(serviceId);
    }

    /**
     * 获取服务近7天评价趋势
     */
    private List<Map<String, Object>> getServiceCommentTrend(Integer serviceId) {
        return commentMapper.getServiceCommentTrend(serviceId);
    }

    /**
     * 获取服务已回复评价数量
     */
    private int getServiceRepliedCommentCount(Integer serviceId) {
        return commentMapper.getServiceRepliedCommentCount(serviceId);
    }

    /**
     * 获取服务评价列表（支持评分筛选）
     */
    private List<ServiceComment> getServiceCommentListWithRating(Integer serviceId, Integer offset, Integer pageSize, Integer rating) {
        return commentMapper.getServiceCommentListWithRating(serviceId, offset, pageSize, rating);
    }

    /**
     * 统计服务评价数量（支持评分筛选）
     */
    private int countServiceCommentWithRating(Integer serviceId, Integer rating) {
        return commentMapper.countServiceCommentWithRating(serviceId, rating);
    }

    /**
     * 管理员和员工获取服务评价统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            Integer userId = getUserId(request);

            Map<String, Object> stats;
            if (role == 3) {
                // 管理员：查看所有统计
                stats = serviceCommentService.getStatistics();
            } else {
                // 员工：查看自己的统计
                stats = serviceCommentService.getStatisticsByStaff(userId);
            }
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}