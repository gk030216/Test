package com.pet.entity;

import java.util.Date;

public class PetVaccineRecord {
    private Integer id;
    private Integer petId;
    private String vaccineName;
    private Date vaccineDate;
    private Date nextDate;
    private String hospital;
    private String doctor;
    private String remark;
    private Date createTime;

    // 关联字段
    private String petName;

    // getter/setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPetId() { return petId; }
    public void setPetId(Integer petId) { this.petId = petId; }
    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }
    public Date getVaccineDate() { return vaccineDate; }
    public void setVaccineDate(Date vaccineDate) { this.vaccineDate = vaccineDate; }
    public Date getNextDate() { return nextDate; }
    public void setNextDate(Date nextDate) { this.nextDate = nextDate; }
    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }
    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
}