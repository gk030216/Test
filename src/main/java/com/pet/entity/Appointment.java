package com.pet.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Appointment {
    private Integer id;
    private String appointmentNo;
    private Integer userId;
    private String userName;
    private Integer petId;
    private String petName;
    private Integer serviceId;
    private String serviceName;
    private BigDecimal servicePrice;
    private Date appointmentDate;
    private String appointmentTime;
    private Integer staffId;
    private String staffName;
    private String remark;
    private Integer status;
    private String cancelReason;
    private Date createTime;
    private Date updateTime;

    // getter/setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getAppointmentNo() { return appointmentNo; }
    public void setAppointmentNo(String appointmentNo) { this.appointmentNo = appointmentNo; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Integer getPetId() { return petId; }
    public void setPetId(Integer petId) { this.petId = petId; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public BigDecimal getServicePrice() { return servicePrice; }
    public void setServicePrice(BigDecimal servicePrice) { this.servicePrice = servicePrice; }
    public Date getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Date appointmentDate) { this.appointmentDate = appointmentDate; }
    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}