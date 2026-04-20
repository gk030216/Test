package com.pet.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Product {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer categoryId;
    private String categoryName;
    private String image;
    private String images;  // 轮播图，逗号分隔
    private Integer sales;
    private Integer status;
    private Integer isHot;
    private Integer isNew;
    private Date createTime;
    private Date updateTime;

    // 临时字段，用于显示库存（不从数据库映射）
    private Integer stock;

    // ✅ 新增：用于接收前端传递的库存ID
    private Integer inventoryId;
    private BigDecimal avgRating;  // 平均评分

    // getter和setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getIsHot() { return isHot; }
    public void setIsHot(Integer isHot) { this.isHot = isHot; }

    public Integer getIsNew() { return isNew; }
    public void setIsNew(Integer isNew) { this.isNew = isNew; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    // ✅ inventoryId 的 getter/setter
    public Integer getInventoryId() { return inventoryId; }
    public void setInventoryId(Integer inventoryId) { this.inventoryId = inventoryId; }

    public BigDecimal getAvgRating() { return avgRating; }
    public void setAvgRating(BigDecimal avgRating) { this.avgRating = avgRating; }
}