package com.pet.mapper;

import com.pet.entity.Appointment;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AppointmentMapper {

    // 生成预约编号
    String generateAppointmentNo();

    // 插入预约
    int insert(Appointment appointment);

    // 获取用户预约列表
    List<Appointment> getUserAppointments(@Param("userId") Integer userId,
                                          @Param("offset") Integer offset,
                                          @Param("limit") Integer limit,
                                          @Param("status") Integer status);

    // 统计用户预约数量
    int countUserAppointments(@Param("userId") Integer userId,
                              @Param("status") Integer status);

    // 获取预约详情
    Appointment getById(@Param("id") Integer id);

    // 获取预约详情（含用户信息）
    Appointment getByIdForAdmin(@Param("id") Integer id);

    // 取消预约
    int cancelAppointment(@Param("id") Integer id,
                          @Param("userId") Integer userId,
                          @Param("reason") String reason);

    // 后台：获取预约列表
    List<Appointment> getAdminAppointments(@Param("offset") Integer offset,
                                           @Param("limit") Integer limit,
                                           @Param("keyword") String keyword,
                                           @Param("status") Integer status,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

    // 后台：统计数量
    int countAdminAppointments(@Param("keyword") String keyword,
                               @Param("status") Integer status,
                               @Param("startDate") String startDate,
                               @Param("endDate") String endDate);

    // 确认预约
    int confirmAppointment(@Param("id") Integer id, @Param("staffId") Integer staffId);

    // 开始服务
    int startAppointment(@Param("id") Integer id);

    // 完成预约
    int completeAppointment(@Param("id") Integer id);

    // 拒绝预约
    int rejectAppointment(@Param("id") Integer id, @Param("reason") String reason);

    // 管理员取消预约
    int cancelAppointmentByAdmin(@Param("id") Integer id, @Param("reason") String reason);

    // 获取统计数据
    List<Map<String, Object>> getStatistics();
}