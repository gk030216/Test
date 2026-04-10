package com.pet.service.impl;

import com.alipay.api.AlipayApiException;
import com.pet.entity.Appointment;
import com.pet.entity.ServiceItem;
import com.pet.entity.User;
import com.pet.mapper.*;
import com.pet.service.AlipayService;
import com.pet.service.AppointmentService;
import com.pet.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        String appointmentNo = appointmentMapper.generateAppointmentNo();
        appointment.setAppointmentNo(appointmentNo);

        ServiceItem service = serviceItemMapper.getById(appointment.getServiceId());
        if (service == null) {
            throw new RuntimeException("服务不存在");
        }
        appointment.setServiceName(service.getName());
        appointment.setServicePrice(service.getPrice());
        appointment.setServiceImage(service.getImage());

        String petName = petProfileMapper.getPetNameById(appointment.getPetId());
        appointment.setPetName(petName);

        appointment.setPayStatus(0);

        appointmentMapper.insert(appointment);

        try {
            User user = userMapper.findById(appointment.getUserId());
            if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
                String formattedDate = dateFormat.format(appointment.getAppointmentDate());

                emailUtil.sendAppointmentNotification(
                        user.getEmail(),
                        user.getNickname() != null ? user.getNickname() : user.getUsername(),
                        service.getName(),
                        formattedDate,
                        appointment.getAppointmentTime(),
                        petName,
                        appointmentNo
                );
                System.out.println("预约邮件已发送至: " + user.getEmail());
            }
        } catch (Exception e) {
            System.err.println("发送预约邮件失败: " + e.getMessage());
            e.printStackTrace();
        }

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
        // 先获取预约信息
        Appointment appointment = appointmentMapper.getById(id);
        if (appointment == null || !appointment.getUserId().equals(userId)) {
            throw new RuntimeException("预约不存在或无权限");
        }

        // 检查状态：只有待确认(0)和已确认(1)可以取消
        if (appointment.getStatus() != 0 && appointment.getStatus() != 1) {
            throw new RuntimeException("当前状态不允许取消");
        }

        // 如果已支付，需要退款
        if (appointment.getPayStatus() != null && appointment.getPayStatus() == 1) {
            try {
                boolean refundSuccess = alipayService.refund(
                        appointment.getAppointmentNo(),
                        appointment.getServicePrice(),
                        reason != null ? reason : "用户取消预约"
                );

                if (!refundSuccess) {
                    throw new RuntimeException("退款失败，请稍后重试");
                }
                // 退款成功，更新状态为已退款(5)
                appointmentMapper.updateRefundStatus(appointment.getAppointmentNo(), 5);
                return true;
            } catch (AlipayApiException e) {
                throw new RuntimeException("退款异常: " + e.getMessage());
            }
        } else {
            // 未支付，直接取消
            return appointmentMapper.cancelAppointment(id, userId, reason) > 0;
        }
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
        return appointmentMapper.completeAppointment(id) > 0;
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

        // 如果已支付，需要退款
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
                // 退款成功，更新状态为已退款(5)
                appointmentMapper.updateRefundStatus(appointment.getAppointmentNo(), 5);
                return true;
            } catch (AlipayApiException e) {
                throw new RuntimeException("退款失败: " + e.getMessage());
            }
        } else {
            // 未支付，直接取消
            return appointmentMapper.cancelAppointmentByAdmin(id, reason) > 0;
        }
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