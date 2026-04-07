package com.pet.service.impl;

import com.pet.entity.Appointment;
import com.pet.entity.ServiceItem;
import com.pet.mapper.AppointmentMapper;
import com.pet.mapper.ServiceItemMapper;
import com.pet.mapper.PetProfileMapper;
import com.pet.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        // 生成预约编号
        String appointmentNo = appointmentMapper.generateAppointmentNo();
        appointment.setAppointmentNo(appointmentNo);

        // 获取服务信息
        ServiceItem service = serviceItemMapper.getById(appointment.getServiceId());
        if (service == null) {
            throw new RuntimeException("服务不存在");
        }
        appointment.setServiceName(service.getName());
        appointment.setServicePrice(service.getPrice());

        // 获取宠物名称
        String petName = petProfileMapper.getPetNameById(appointment.getPetId());
        appointment.setPetName(petName);

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
        return appointmentMapper.cancelAppointment(id, userId, reason) > 0;
    }

    @Override
    public Map<String, Object> getAdminAppointments(Integer page, Integer pageSize,
                                                    String keyword, Integer status,
                                                    String startDate, String endDate) {
        int offset = (page - 1) * pageSize;
        List<Appointment> list = appointmentMapper.getAdminAppointments(offset, pageSize, keyword, status, startDate, endDate);
        int total = appointmentMapper.countAdminAppointments(keyword, status, startDate, endDate);

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
        return appointmentMapper.cancelAppointmentByAdmin(id, reason) > 0;
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
}