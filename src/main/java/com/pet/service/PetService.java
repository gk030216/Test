package com.pet.service;

import com.pet.entity.PetProfile;
import com.pet.entity.PetVaccineRecord;
import com.pet.entity.PetHealthRecord;
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
}