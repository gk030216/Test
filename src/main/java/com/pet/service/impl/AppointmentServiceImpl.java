package com.pet.service.impl;

import com.alipay.api.AlipayApiException;
import com.pet.entity.*;
import com.pet.mapper.*;
import com.pet.service.AlipayService;
import com.pet.service.AppointmentService;
import com.pet.service.NotificationService;
import com.pet.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ServiceItemMapper serviceItemMapper;

    @Autowired
    private PetProfileMapper petProfileMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StaffStatisticsMapper statisticsMapper;

    @Autowired
    private ServiceCommentMapper serviceCommentMapper;

    @Override
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        // 1. 生成预约编号
        String appointmentNo = appointmentMapper.generateAppointmentNo();
        appointment.setAppointmentNo(appointmentNo);

        // 2. 获取服务信息
        ServiceItem service = serviceItemMapper.getById(appointment.getServiceId());
        if (service == null) {
            throw new RuntimeException("服务不存在");
        }
        appointment.setServiceName(service.getName());
        appointment.setServicePrice(service.getPrice());
        appointment.setServiceImage(service.getImage());

        // 3. 获取宠物名称
        String petName = petProfileMapper.getPetNameById(appointment.getPetId());
        appointment.setPetName(petName);

        // 4. 设置初始状态
        appointment.setPayStatus(0);
        appointment.setStatus(0);

        // 5. 检查预约日期不能是过去
        Date today = new Date();
        today.setHours(0);
        today.setMinutes(0);
        today.setSeconds(0);

        if (appointment.getAppointmentDate().before(today)) {
            throw new RuntimeException("不能预约过去的日期");
        }

        // 6. 检查预约时间是否有效（不能预约当天已过的时间段）
        Calendar now = Calendar.getInstance();
        Calendar appointmentCal = Calendar.getInstance();
        appointmentCal.setTime(appointment.getAppointmentDate());

        if (appointmentCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                appointmentCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {

            String timeSlot = appointment.getAppointmentTime();
            int slotHour = Integer.parseInt(timeSlot.split(":")[0]);
            int currentHour = now.get(Calendar.HOUR_OF_DAY);

            if (slotHour <= currentHour) {
                throw new RuntimeException("不能预约当天已过的时间段");
            }
        }

        // 7. ✅ 检查用户时间冲突（同一用户同一时间段不能有多个预约）
        int userConflict = appointmentMapper.checkUserTimeConflict(
                appointment.getUserId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                null
        );
        if (userConflict > 0) {
            throw new RuntimeException("您在该时间段已有其他预约，请选择其他时间");
        }

        // 8. 保存到数据库
        appointmentMapper.insert(appointment);

        return appointment;
    }

    @Override
    public Map<String, Object> getUserAppointments(Integer userId, Integer page, Integer pageSize, Integer status) {
        int offset = (page - 1) * pageSize;
        List<Appointment> list = appointmentMapper.getUserAppointments(userId, offset, pageSize, status);
        int total = appointmentMapper.countUserAppointments(userId, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Appointment getById(Integer id, Integer userId) {
        return appointmentMapper.getById(id);
    }

    @Override
    @Transactional
    public boolean cancelAppointment(Integer id, Integer userId, String reason) {
        Appointment appointment = appointmentMapper.getById(id);
        if (appointment == null || !appointment.getUserId().equals(userId)) {
            throw new RuntimeException("预约不存在或无权限");
        }

        if (appointment.getStatus() != 0 && appointment.getStatus() != 1) {
            throw new RuntimeException("当前状态不允许取消");
        }

        boolean success;
        if (appointment.getPayStatus() != null && appointment.getPayStatus() == 1) {
            // 已支付，需要退款
            try {
                boolean refundSuccess = alipayService.refund(
                        appointment.getAppointmentNo(),
                        appointment.getServicePrice(),
                        reason != null ? reason : "用户取消预约"
                );

                if (!refundSuccess) {
                    throw new RuntimeException("退款失败，请稍后重试");
                }
                // ✅ 修改：退款成功，状态改为 4（已取消），同时 pay_status 会在 Mapper 中更新为 3
                appointmentMapper.updateRefundStatus(appointment.getAppointmentNo(), 4, reason);
                success = true;

                // 发送退款成功站内消息
                notificationService.sendNotification(
                        userId,
                        "appointment",
                        "预约退款成功",
                        "您的服务【" + appointment.getServiceName() + "】已取消，退款金额 ¥" + appointment.getServicePrice() + " 将原路返回。",
                        "/personal/appointments"
                );

            } catch (AlipayApiException e) {
                throw new RuntimeException("退款异常: " + e.getMessage());
            }
        } else {
            // 未支付，直接取消
            success = appointmentMapper.cancelAppointment(id, userId, reason) > 0;

            // 发送取消成功站内消息（未支付）
            if (success) {
                notificationService.sendNotification(
                        userId,
                        "appointment",
                        "预约已取消",
                        "您的服务【" + appointment.getServiceName() + "】已取消。" + (reason != null ? " 原因：" + reason : ""),
                        "/personal/appointments"
                );
            }
        }

        return success;
    }

    @Override
    public Map<String, Object> getAdminAppointments(Integer page, Integer pageSize,
                                                    String keyword, Integer status,
                                                    String startDate, String endDate,
                                                    Integer staffId, Boolean isStaff) {
        int offset = (page - 1) * pageSize;
        List<Appointment> list = appointmentMapper.getAdminAppointments(
                offset, pageSize, keyword, status, startDate, endDate, staffId, isStaff
        );
        int total = appointmentMapper.countAdminAppointments(
                keyword, status, startDate, endDate, staffId, isStaff
        );

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Appointment getByIdForAdmin(Integer id) {
        return appointmentMapper.getByIdForAdmin(id);
    }

    @Override
    @Transactional
    public boolean confirmAppointment(Integer id, Integer staffId) {
        Appointment appointment = appointmentMapper.getById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 检查状态是否为待确认
        if (appointment.getStatus() != 0) {
            throw new RuntimeException("预约状态不允许确认");
        }

        return appointmentMapper.confirmAppointment(id, staffId) > 0;
    }

    @Override
    @Transactional
    public boolean startAppointment(Integer id) {
        return appointmentMapper.startAppointment(id) > 0;
    }

    @Override
    @Transactional
    public boolean completeAppointment(Integer id) {
        Appointment appointment = appointmentMapper.getById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 完成预约
        int result = appointmentMapper.completeAppointment(id);

        if (result > 0) {
            // 增加服务销量
            serviceItemMapper.incrementSales(appointment.getServiceId(), 1);
            System.out.println("✅ 服务销量已更新，服务ID: " + appointment.getServiceId());

            // ❌ 删除这里的员工统计更新，因为评价时才会更新
            // 员工统计由 addComment 方法负责更新
        }

        return result > 0;
    }

    @Override
    @Transactional
    public boolean rejectAppointment(Integer id, String reason) {
        return appointmentMapper.rejectAppointment(id, reason) > 0;
    }

    @Override
    @Transactional
    public boolean cancelAppointmentByAdmin(Integer id, String reason) {
        Appointment appointment = appointmentMapper.getById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        boolean success;
        if (appointment.getPayStatus() != null && appointment.getPayStatus() == 1) {
            try {
                boolean refundSuccess = alipayService.refund(
                        appointment.getAppointmentNo(),
                        appointment.getServicePrice(),
                        reason != null ? reason : "商家取消预约"
                );

                if (!refundSuccess) {
                    throw new RuntimeException("退款失败，请稍后重试");
                }
                appointmentMapper.updateRefundStatus(appointment.getAppointmentNo(), 4, reason);
                success = true;

                // ✅ 添加：发送退款成功站内消息
                notificationService.sendNotification(
                        appointment.getUserId(),
                        "appointment",
                        "预约已被商家取消",
                        "您的服务【" + appointment.getServiceName() + "】已被商家取消，退款金额 ¥" + appointment.getServicePrice() + " 将原路返回。" +
                                (reason != null ? " 原因：" + reason : ""),
                        "/personal/appointments"
                );

            } catch (AlipayApiException e) {
                throw new RuntimeException("退款失败: " + e.getMessage());
            }
        } else {
            success = appointmentMapper.cancelAppointmentByAdmin(id, reason) > 0;

            // ✅ 添加：发送取消成功站内消息（未支付）
            if (success) {
                notificationService.sendNotification(
                        appointment.getUserId(),
                        "appointment",
                        "预约已被商家取消",
                        "您的服务【" + appointment.getServiceName() + "】已被商家取消。" + (reason != null ? " 原因：" + reason : ""),
                        "/personal/appointments"
                );
            }
        }

        return success;
    }

    @Override
    public Map<String, Object> getStatistics() {
        List<Map<String, Object>> list = appointmentMapper.getStatistics();
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        Map<String, Object> empty = new HashMap<>();
        empty.put("total", 0);
        empty.put("pending", 0);
        empty.put("confirmed", 0);
        empty.put("inService", 0);
        empty.put("completed", 0);
        empty.put("cancelled", 0);
        empty.put("rejected", 0);
        empty.put("totalIncome", 0);
        return empty;
    }

    @Override
    public Map<String, Object> getStaffStatistics(Integer staffId) {
        List<Map<String, Object>> list = appointmentMapper.getStaffStatistics(staffId);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        Map<String, Object> empty = new HashMap<>();
        empty.put("total", 0);
        empty.put("pending", 0);
        empty.put("confirmed", 0);
        empty.put("inService", 0);
        empty.put("completed", 0);
        empty.put("cancelled", 0);
        empty.put("rejected", 0);
        empty.put("totalIncome", 0);
        return empty;
    }

    @Override
    public Appointment getByAppointmentNo(String appointmentNo) {
        return appointmentMapper.getByAppointmentNo(appointmentNo);
    }


}