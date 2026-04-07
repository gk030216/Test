package com.pet.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ServiceItem {
    private Integer id;
    private Integer categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer duration;
    private String image;
    private String images;
    private String suitableFor;
    private Integer isHot;
    private Integer status;
    private Integer sales;
    private Date createTime;
    private Date updateTime;

    // 非数据库字段
    private List<String> imageList;

    // getter/setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getSuitableFor() { return suitableFor; }
    public void setSuitableFor(String suitableFor) { this.suitableFor = suitableFor; }
    public Integer getIsHot() { return isHot; }
    public void setIsHot(Integer isHot) { this.isHot = isHot; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public List<String> getImageList() { return imageList; }
    public void setImageList(List<String> imageList) { this.imageList = imageList; }
}