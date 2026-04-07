package com.pet.controller.admin;

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
@RequestMapping("/api/admin/service")
@CrossOrigin(origins = "*")
public class AdminServiceController {

    @Autowired
    private ServiceCategoryService categoryService;

    @Autowired
    private ServiceItemService itemService;

    @Autowired
    private AppointmentService appointmentService;

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
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            Map<String, Object> result = appointmentService.getAdminAppointments(page, pageSize, keyword, status, startDate, endDate);
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
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            Appointment appointment = appointmentService.getByIdForAdmin(id);
            return Result.success(appointment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 确认预约
     */
    @PutMapping("/appointment/confirm/{id}")
    public Result<?> confirmAppointment(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            Integer staffId = getUserId(request);
            boolean success = appointmentService.confirmAppointment(id, staffId);
            return success ? Result.success("确认成功") : Result.error("确认失败");
        } catch (Exception e) {
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
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = appointmentService.startAppointment(id);
            return success ? Result.success("服务已开始") : Result.error("操作失败");
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
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = appointmentService.completeAppointment(id);
            return success ? Result.success("已完成") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 拒绝预约
     */
    @PutMapping("/appointment/reject/{id}")
    public Result<?> rejectAppointment(@PathVariable Integer id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            String reason = params.get("reason");
            boolean success = appointmentService.rejectAppointment(id, reason);
            return success ? Result.success("已拒绝") : Result.error("操作失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消预约（管理员）
     */
    @PutMapping("/appointment/cancel/{id}")
    public Result<?> cancelAppointment(@PathVariable Integer id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
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
     */
    @GetMapping("/appointment/statistics")
    public Result<Map<String, Object>> getAppointmentStatistics(HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 3) {
                return Result.error(403, "无权限访问");
            }
            Map<String, Object> stats = appointmentService.getStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}