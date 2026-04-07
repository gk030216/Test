package com.pet.service;

import com.pet.entity.Appointment;
import java.util.Map;

public interface AppointmentService {

    // 创建预约
    Appointment createAppointment(Appointment appointment);

    // 获取用户预约列表
    Map<String, Object> getUserAppointments(Integer userId, Integer page, Integer pageSize, Integer status);

    // 获取预约详情（用户）
    Appointment getById(Integer id, Integer userId);

    // 取消预约（用户）
    boolean cancelAppointment(Integer id, Integer userId, String reason);

    // 后台：获取预约列表
    Map<String, Object> getAdminAppointments(Integer page, Integer pageSize,
                                             String keyword, Integer status,
                                             String startDate, String endDate);

    // 获取预约详情（后台）
    Appointment getByIdForAdmin(Integer id);

    // 确认预约
    boolean confirmAppointment(Integer id, Integer staffId);

    // 开始服务
    boolean startAppointment(Integer id);

    // 完成预约
    boolean completeAppointment(Integer id);

    // 拒绝预约
    boolean rejectAppointment(Integer id, String reason);

    // 取消预约（管理员）
    boolean cancelAppointmentByAdmin(Integer id, String reason);

    // 获取统计数据
    Map<String, Object> getStatistics();
}