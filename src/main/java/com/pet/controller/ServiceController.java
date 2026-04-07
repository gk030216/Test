package com.pet.controller;

import com.pet.entity.ServiceCategory;
import com.pet.entity.ServiceItem;
import com.pet.entity.Appointment;
import com.pet.service.ServiceCategoryService;
import com.pet.service.ServiceItemService;
import com.pet.service.AppointmentService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/service")
@CrossOrigin(origins = "*")
public class ServiceController {

    @Autowired
    private ServiceCategoryService categoryService;

    @Autowired
    private ServiceItemService itemService;

    @Autowired
    private AppointmentService appointmentService;

    private Integer getUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    // ========== 服务分类接口 ==========

    /**
     * 获取所有启用的服务分类（树形结构）
     */
    @GetMapping("/category/list")
    public Result<List<ServiceCategory>> getCategoryList() {
        try {
            List<ServiceCategory> list = categoryService.getEnabledCategories();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 服务项目接口 ==========

    /**
     * 获取服务列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getServiceList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort) {
        try {
            Map<String, Object> result = itemService.getServiceList(page, pageSize, categoryId, keyword, sort);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取服务详情
     */
    @GetMapping("/{id}")
    public Result<ServiceItem> getServiceDetail(@PathVariable Integer id) {
        try {
            ServiceItem item = itemService.getById(id);
            if (item != null) {
                return Result.success(item);
            } else {
                return Result.error("服务不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取热门服务
     */
    @GetMapping("/hot")
    public Result<List<ServiceItem>> getHotServices() {
        try {
            List<ServiceItem> list = itemService.getHotServices();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 预约接口 ==========

    /**
     * 创建预约
     */
    @PostMapping("/appointment")
    public Result<Appointment> createAppointment(@RequestBody Appointment appointment, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            appointment.setUserId(userId);
            Appointment result = appointmentService.createAppointment(appointment);
            return Result.success("预约成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户预约列表
     */
    @GetMapping("/appointment/list")
    public Result<Map<String, Object>> getAppointmentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Map<String, Object> result = appointmentService.getUserAppointments(userId, page, pageSize, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/appointment/{id}")
    public Result<Appointment> getAppointmentDetail(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Appointment appointment = appointmentService.getById(id, userId);
            if (appointment != null) {
                return Result.success(appointment);
            } else {
                return Result.error("预约不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消预约
     */
    @PutMapping("/appointment/cancel/{id}")
    public Result<?> cancelAppointment(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> params, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            String reason = params != null ? params.get("reason") : null;
            boolean success = appointmentService.cancelAppointment(id, userId, reason);
            if (success) {
                return Result.success("取消成功");
            } else {
                return Result.error("取消失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}