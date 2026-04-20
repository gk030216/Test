package com.pet.mapper;

import com.pet.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface UserMapper {

    /**
     * 根据用户名查询
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询
     */
    User findByEmail(@Param("email") String email);

    /**
     * 根据用户名或邮箱查询
     */
    User findByUsernameOrEmail(@Param("account") String account);

    /**
     * 根据ID查询用户
     */
    User findById(@Param("id") Integer id);

    /**
     * 查询所有用户（分页）
     */
    List<User> findAll(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 查询用户总数
     */
    int countAll();

    /**
     * 条件搜索用户
     */
    List<User> searchUsers(@Param("keyword") String keyword,
                           @Param("role") Integer role,
                           @Param("status") Integer status,
                           @Param("offset") Integer offset,
                           @Param("limit") Integer limit);

    /**
     * 条件搜索用户总数
     */
    int countSearch(@Param("keyword") String keyword,
                    @Param("role") Integer role,
                    @Param("status") Integer status);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户
     */
    int update(User user);

    /**
     * 更新最后登录时间
     */
    int updateLastLoginTime(@Param("id") Integer id);

    /**
     * 更新密码
     */
    int updatePassword(@Param("email") String email, @Param("password") String password);

    /**
     * 更新用户状态
     */
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 更新用户角色
     */
    int updateRole(@Param("id") Integer id, @Param("role") Integer role);

    /**
     * 删除用户
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 获取所有启用的员工列表（role=2, status=1）
     */
    List<User> getStaffList();

    // ============= 用户分析 =============

    // 统计活跃用户（最近7天有登录）
    int countActiveUsers();

    // 统计今日新增用户
    int countTodayNew();

    // 统计近7日新增用户
    int countWeekNew();

    // 获取用户增长趋势
    List<Map<String, Object>> getUserTrend(@Param("type") String type);

    // 获取角色分布
    List<Map<String, Object>> getRoleDistribution();

    // 获取性别分布
    List<Map<String, Object>> getGenderDistribution();

    // 获取活跃度分布
    List<Map<String, Object>> getActivityDistribution();

    // 统计今日登录用户数
    int countTodayLoginUsers();
}