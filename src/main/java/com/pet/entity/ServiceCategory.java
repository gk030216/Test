package com.pet.entity;

import java.util.Date;
import java.util.List;

public class ServiceCategory {
    private Integer id;
    private String name;
    private Integer parentId;
    private Integer sortOrder;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    // 非数据库字段
    private List<ServiceCategory> children;
    private Integer serviceCount;
    private String parentName;  // 父分类名称

    // getter/setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public List<ServiceCategory> getChildren() { return children; }
    public void setChildren(List<ServiceCategory> children) { this.children = children; }
    public Integer getServiceCount() { return serviceCount; }
    public void setServiceCount(Integer serviceCount) { this.serviceCount = serviceCount; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
}