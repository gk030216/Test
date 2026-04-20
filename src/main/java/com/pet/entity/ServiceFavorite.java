package com.pet.entity;

import java.util.Date;

public class ServiceFavorite {
    private Integer id;
    private Integer userId;
    private Integer serviceId;
    private Date createTime;

    // 关联字段（非数据库字段）
    private String serviceName;
    private String serviceImage;
    private String serviceDescription;
    private java.math.BigDecimal servicePrice;
    private Integer serviceDuration;

    // getter/setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceImage() { return serviceImage; }
    public void setServiceImage(String serviceImage) { this.serviceImage = serviceImage; }

    public String getServiceDescription() { return serviceDescription; }
    public void setServiceDescription(String serviceDescription) { this.serviceDescription = serviceDescription; }

    public java.math.BigDecimal getServicePrice() { return servicePrice; }
    public void setServicePrice(java.math.BigDecimal servicePrice) { this.servicePrice = servicePrice; }

    public Integer getServiceDuration() { return serviceDuration; }
    public void setServiceDuration(Integer serviceDuration) { this.serviceDuration = serviceDuration; }
}