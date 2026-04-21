package com.pet.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Order {
    private Integer id;
    private String orderNo;
    private Integer userId;
    private String userName;
    private String userPhone;
    private String userAddress;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer payType;
    private Integer payStatus;
    private Integer orderStatus;
    private String tradeNo;
    private Date createTime;
    private Date payTime;
    private Date updateTime;
    private String orderType;
    private String cancelReason;

    // 非数据库字段，用于关联订单详情
    private List<OrderItem> items;

    // 导出临时字段（非数据库字段）
    private String orderStatusName;
    private String payStatusName;

    // getter和setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getUserAddress() { return userAddress; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }

    public Integer getPayType() { return payType; }
    public void setPayType(Integer payType) { this.payType = payType; }

    public Integer getPayStatus() { return payStatus; }
    public void setPayStatus(Integer payStatus) { this.payStatus = payStatus; }

    public Integer getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Integer orderStatus) { this.orderStatus = orderStatus; }

    public String getTradeNo() { return tradeNo; }
    public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getPayTime() { return payTime; }
    public void setPayTime(Date payTime) { this.payTime = payTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getOrderStatusName() { return orderStatusName; }
    public void setOrderStatusName(String orderStatusName) { this.orderStatusName = orderStatusName; }

    public String getPayStatusName() { return payStatusName; }
    public void setPayStatusName(String payStatusName) { this.payStatusName = payStatusName; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
}