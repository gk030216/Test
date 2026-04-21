package com.pet.controller.admin;

import com.pet.entity.User;
import com.pet.mapper.UserMapper;
import com.pet.service.UserService;
import com.pet.util.ExcelUtil;
import com.pet.util.JwtUtil;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/user")
@CrossOrigin(origins = "*")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = userService.getUserList(page, pageSize, keyword, role, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            if (user != null) {
                user.setPassword(null);
                return Result.success(user);
            } else {
                return Result.error("用户不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加用户
     */
    @PostMapping("/add")
    public Result<?> addUser(@RequestBody User user) {
        try {
            boolean success = userService.addUser(user);
            if (success) {
                return Result.success("添加用户成功");
            } else {
                return Result.error("添加用户失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量添加用户
     */
    @PostMapping("/batch-add")
    public Result<?> batchAddUsers(@RequestBody List<User> users) {
        try {
            int successCount = 0;
            int failCount = 0;
            for (User user : users) {
                try {
                    if (userService.addUser(user)) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                }
            }
            return Result.success("批量添加完成，成功：" + successCount + "，失败：" + failCount);
        } catch (Exception e) {
            return Result.error("批量添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<?> updateUser(@RequestBody User user) {
        try {
            boolean success = userService.updateUser(user);
            if (success) {
                return Result.success("更新用户成功");
            } else {
                return Result.error("更新用户失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新用户
     */
    @PutMapping("/batch-update")
    public Result<?> batchUpdateUsers(@RequestBody List<User> users) {
        try {
            int successCount = 0;
            int failCount = 0;
            for (User user : users) {
                try {
                    if (userService.updateUser(user)) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                }
            }
            return Result.success("批量更新完成，成功：" + successCount + "，失败：" + failCount);
        } catch (Exception e) {
            return Result.error("批量更新失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/status")
    public Result<?> updateUserStatus(@RequestParam Integer id, @RequestParam Integer status) {
        try {
            boolean success = userService.updateUserStatus(id, status);
            if (success) {
                return Result.success(status == 1 ? "启用用户成功" : "禁用用户成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新用户状态
     */
    @PutMapping("/batch-status")
    public Result<?> batchUpdateUserStatus(@RequestParam String ids, @RequestParam Integer status) {
        try {
            // 使用 collect(Collectors.toList()) 替代 toList()
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            int successCount = 0;
            for (Integer id : idList) {
                if (userService.updateUserStatus(id, status)) {
                    successCount++;
                }
            }
            return Result.success("批量" + (status == 1 ? "启用" : "禁用") + "完成，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error("批量操作失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/role")
    public Result<?> updateUserRole(@RequestParam Integer id, @RequestParam Integer role) {
        try {
            boolean success = userService.updateUserRole(id, role);
            if (success) {
                return Result.success("更新角色成功");
            } else {
                return Result.error("更新角色失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新用户角色
     */
    @PutMapping("/batch-role")
    public Result<?> batchUpdateUserRole(@RequestParam String ids, @RequestParam Integer role) {
        try {
            // 使用 collect(Collectors.toList()) 替代 toList()
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            int successCount = 0;
            for (Integer id : idList) {
                if (userService.updateUserRole(id, role)) {
                    successCount++;
                }
            }
            String roleName = role == 1 ? "普通用户" : (role == 2 ? "员工" : "管理员");
            return Result.success("批量更新角色为[" + roleName + "]完成，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error("批量操作失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteUser(@PathVariable Integer id) {
        try {
            boolean success = userService.deleteUser(id);
            if (success) {
                return Result.success("删除用户成功");
            } else {
                return Result.error("删除用户失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch-delete")
    public Result<?> batchDeleteUsers(@RequestParam String ids) {
        try {
            // 使用 collect(Collectors.toList()) 替代 toList()
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            int successCount = 0;
            for (Integer id : idList) {
                try {
                    if (userService.deleteUser(id)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    // 跳过失败的
                }
            }
            return Result.success("批量删除完成，成功删除：" + successCount + "个");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 导出用户列表
     */
    @GetMapping("/export")
    public void exportUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            // 从请求头获取Token并验证
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (token == null || !JwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("未授权访问");
                return;
            }

            // 获取所有符合条件的用户（不分页）
            Map<String, Object> result = userService.getUserList(1, 9999, keyword, role, status);
            List<User> userList = (List<User>) result.get("list");

            // 定义表头和字段名
            String[] headers = {"ID", "用户名", "昵称", "邮箱", "手机号", "性别", "角色", "状态", "注册时间", "最后登录时间"};
            String[] fieldNames = {"id", "username", "nickname", "email", "phone", "gender", "roleName", "statusName", "createTime", "lastLoginTime"};

            // 为导出准备数据，添加角色名称和状态名称
            for (User user : userList) {
                user.setRoleName(getRoleName(user.getRole()));
                user.setStatusName(user.getStatus() == 1 ? "正常" : "禁用");
            }

            ExcelUtil.exportExcel(response, userList, headers, fieldNames, "用户列表", "用户数据_" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("导出失败：" + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private String getRoleName(Integer role) {
        if (role == 1) return "普通用户";
        if (role == 2) return "员工";
        if (role == 3) return "管理员";
        return "未知";
    }

    /**
     * 获取所有员工列表（用于下拉选择）
     */
    @GetMapping("/staff/all")
    public Result<List<User>> getAllStaff(HttpServletRequest request) {
        try {
            Integer role = (Integer) request.getAttribute("role");
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            // 查询 role=2 的员工
            Map<String, Object> result = userService.getUserList(1, 100, null, 2, 1);
            List<User> staffList = (List<User>) result.get("list");
            return Result.success(staffList);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 用户分析接口 ==========

    /**
     * 获取用户统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 总用户数
            int totalUsers = userMapper.countAll();
            stats.put("totalUsers", totalUsers);

            // 活跃用户（最近7天有登录）
            int activeUsers = userMapper.countActiveUsers();
            stats.put("activeUsers", activeUsers);

            // 今日新增
            int todayNew = userMapper.countTodayNew();
            stats.put("todayNew", todayNew);

            // 近7日新增
            int weekNew = userMapper.countWeekNew();
            stats.put("weekNew", weekNew);

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户增长趋势
     */
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getUserTrend(@RequestParam(defaultValue = "daily") String type) {
        try {
            List<Map<String, Object>> trend = userMapper.getUserTrend(type);
            return Result.success(trend);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户角色分布
     */
    @GetMapping("/role-distribution")
    public Result<List<Map<String, Object>>> getUserRoleDistribution() {
        try {
            List<Map<String, Object>> distribution = userMapper.getRoleDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户性别分布
     */
    @GetMapping("/gender-distribution")
    public Result<List<Map<String, Object>>> getUserGenderDistribution() {
        try {
            List<Map<String, Object>> distribution = userMapper.getGenderDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户活跃度分布
     */
    @GetMapping("/activity-distribution")
    public Result<List<Map<String, Object>>> getUserActivityDistribution() {
        try {
            List<Map<String, Object>> distribution = userMapper.getActivityDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}