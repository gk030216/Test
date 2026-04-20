package com.pet.service;

import com.pet.entity.PetProfile;
import com.pet.entity.PetVaccineRecord;
import com.pet.entity.PetHealthRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PetService {

    // ========== 用户端 ==========

    List<PetProfile> getUserPets(Integer userId);
    PetProfile getDefaultPet(Integer userId);
    PetProfile getPetDetail(Integer id, Integer userId);
    boolean addPet(PetProfile pet, Integer userId);
    boolean updatePet(PetProfile pet, Integer userId);
    boolean deletePet(Integer id, Integer userId);
    boolean setDefaultPet(Integer id, Integer userId);

    // ========== 疫苗记录 ==========

    List<PetVaccineRecord> getVaccineRecords(Integer petId);
    boolean addVaccineRecord(PetVaccineRecord record);
    boolean updateVaccineRecord(PetVaccineRecord record);
    boolean deleteVaccineRecord(Integer id);
    List<PetVaccineRecord> getUpcomingVaccines(Integer userId);

    // ========== 体检记录 ==========

    List<PetHealthRecord> getHealthRecords(Integer petId);
    boolean addHealthRecord(PetHealthRecord record);
    boolean updateHealthRecord(PetHealthRecord record);
    boolean deleteHealthRecord(Integer id);

    // ========== 管理员接口 ==========

    // 获取所有宠物列表（分页）
    Map<String, Object> getAdminPetList(Integer page, Integer pageSize, String keyword, String type, Integer status);

    // 管理员删除宠物
    boolean adminDeletePet(Integer id);

    // 获取所有疫苗记录（分页）
    Map<String, Object> getAllVaccineRecords(Integer page, Integer pageSize, String keyword);

    // 获取所有体检记录（分页）
    Map<String, Object> getAllHealthRecords(Integer page, Integer pageSize, String keyword);

    // 获取宠物统计数据
    Map<String, Object> getPetStatistics();

    /**
     * 批量删除疫苗记录
     */
    boolean batchDeleteVaccineRecords(List<Integer> ids);

    /**
     * 批量删除体检记录
     */
    boolean batchDeleteHealthRecords(List<Integer> ids);

    /**
     * 获取即将到期的宠物数量（7天内需要接种疫苗的宠物）
     */
    int getUpcomingPetsCount();

    /**
     * 获取有疫苗记录的正常宠物数量（去重）
     */
    int getDistinctPetsWithVaccineCount();

    /**
     * 获取有体检记录的正常宠物数量（去重）
     */
    int getDistinctPetsWithHealthRecordCount();

    /**
     * 获取正常宠物的平均体重（基于每只宠物的最新体重）
     */
    BigDecimal getAvgWeight();
}