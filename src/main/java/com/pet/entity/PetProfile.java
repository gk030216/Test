package com.pet.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class PetProfile {
    private Integer id;
    private Integer userId;
    private String name;
    private String type;
    private String breed;
    private Integer gender;
    private Date birthday;
    private BigDecimal weight;
    private String color;
    private String avatar;
    private String description;
    private Integer isDefault;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    // 关联字段
    private String userName;
    private Integer age;  // 计算年龄
    private List<PetVaccineRecord> vaccineRecords;
    private List<PetHealthRecord> healthRecords;

    // getter/setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    public Date getBirthday() { return birthday; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public List<PetVaccineRecord> getVaccineRecords() { return vaccineRecords; }
    public void setVaccineRecords(List<PetVaccineRecord> vaccineRecords) { this.vaccineRecords = vaccineRecords; }
    public List<PetHealthRecord> getHealthRecords() { return healthRecords; }
    public void setHealthRecords(List<PetHealthRecord> healthRecords) { this.healthRecords = healthRecords; }
}