package com.pet.entity;

import java.math.BigDecimal;
import java.util.Date;

public class ProductComment {
    private Integer id;
    private String orderNo;
    private Integer productId;
    private Integer userId;
    private String userName;
    private Integer rating;
    private String content;
    private String images;
    private String reply;
    private Date replyTime;
    private Integer status;
    private Date createTime;

    // 关联查询字段
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
    private String userAvatar;

    // getter/setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public Date getReplyTime() { return replyTime; }
    public void setReplyTime(Date replyTime) { this.replyTime = replyTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
}