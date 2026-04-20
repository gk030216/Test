package com.pet.entity;

import java.math.BigDecimal;
import java.util.Date;

public class StaffStatistics {
    private Integer id;
    private Integer staffId;
    private Integer serviceCount;      // 服务总次数
    private Integer completedCount;    // 完成次数
    private Integer ratingSum;          // 评分总和
    private BigDecimal ratingAvg;       // 平均评分
    private Date updateTime;

    // getter/setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }
    public Integer getServiceCount() { return serviceCount; }
    public void setServiceCount(Integer serviceCount) { this.serviceCount = serviceCount; }
    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }
    public Integer getRatingSum() { return ratingSum; }
    public void setRatingSum(Integer ratingSum) { this.ratingSum = ratingSum; }
    public BigDecimal getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(BigDecimal ratingAvg) { this.ratingAvg = ratingAvg; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}