package com.pet.service.impl;

import com.pet.entity.Inventory;
import com.pet.entity.PetProfile;
import com.pet.entity.PetVaccineRecord;
import com.pet.entity.PetHealthRecord;
import com.pet.mapper.InventoryMapper;
import com.pet.mapper.PetProfileMapper;
import com.pet.mapper.PetVaccineRecordMapper;
import com.pet.mapper.PetHealthRecordMapper;
import com.pet.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PetServiceImpl implements PetService {

    @Autowired
    private PetProfileMapper petProfileMapper;

    @Autowired
    private PetVaccineRecordMapper vaccineRecordMapper;

    @Autowired
    private PetHealthRecordMapper healthRecordMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    // ========== 宠物档案 ==========

    @Override
    public List<PetProfile> getUserPets(Integer userId) {
        return petProfileMapper.getByUserId(userId);
    }

    @Override
    public PetProfile getDefaultPet(Integer userId) {
        return petProfileMapper.getDefaultByUserId(userId);
    }

    @Override
    public PetProfile getPetDetail(Integer id, Integer userId) {
        PetProfile pet = petProfileMapper.getById(id, userId);
        if (pet != null) {
            // 计算年龄
            if (pet.getBirthday() != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                int currentYear = cal.get(java.util.Calendar.YEAR);
                cal.setTime(pet.getBirthday());
                int birthYear = cal.get(java.util.Calendar.YEAR);
                pet.setAge(currentYear - birthYear);
            }
            // 加载疫苗记录
            pet.setVaccineRecords(vaccineRecordMapper.getByPetId(id));
            // 加载体检记录
            pet.setHealthRecords(healthRecordMapper.getByPetId(id));
        }
        return pet;
    }

    @Override
    @Transactional
    public boolean addPet(PetProfile pet, Integer currentUserId) {
        // 优先使用前端传递的 userId，如果没有则使用当前登录用户ID（普通用户添加自己的宠物时）
        if (pet.getUserId() == null || pet.getUserId() <= 0) {
            pet.setUserId(currentUserId);
        }

        pet.setStatus(1);

        // 如果没有设置 isDefault，默认为 0
        if (pet.getIsDefault() == null) {
            pet.setIsDefault(0);
        }

        // 如果要设为默认，先清除该用户的其他默认宠物
        if (pet.getIsDefault() == 1) {
            petProfileMapper.clearDefault(pet.getUserId());
        }

        // 检查该用户是否已有宠物，如果没有则自动设为默认
        int count = petProfileMapper.countByUserId(pet.getUserId());
        if (count == 0) {
            pet.setIsDefault(1);
        }

        return petProfileMapper.insert(pet) > 0;
    }

    @Override
    @Transactional
    public boolean updatePet(PetProfile pet, Integer currentUserId) {
        // 1. 查询原宠物信息
        PetProfile existingPet = petProfileMapper.getById(pet.getId(), null);
        if (existingPet == null) {
            throw new RuntimeException("宠物不存在");
        }

        // 2. 获取原宠物的 userId
        Integer userId = existingPet.getUserId();

        // 3. 如果要设为默认，先清除该用户的其他默认宠物
        if (pet.getIsDefault() != null && pet.getIsDefault() == 1) {
            petProfileMapper.clearDefault(userId);
        }

        // 4. 确保 userId 不被修改
        pet.setUserId(userId);

        // 5. 执行更新
        int result = petProfileMapper.update(pet);

        System.out.println("更新宠物结果: " + result + ", 宠物ID: " + pet.getId());

        return result > 0;
    }

    @Override
    @Transactional
    public boolean deletePet(Integer id, Integer userId) {
        return petProfileMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean setDefaultPet(Integer id, Integer userId) {
        petProfileMapper.clearDefault(userId);
        return petProfileMapper.setDefault(id, userId) > 0;
    }

    // ========== 疫苗记录 ==========

    @Override
    public List<PetVaccineRecord> getVaccineRecords(Integer petId) {
        return vaccineRecordMapper.getByPetId(petId);
    }

    @Override
    @Transactional
    public boolean addVaccineRecord(PetVaccineRecord record) {
        // 1. 检查库存
        if (record.getInventoryId() != null) {
            Inventory inventory = inventoryMapper.getById(record.getInventoryId());
            if (inventory == null) {
                throw new RuntimeException("选择的疫苗不存在");
            }
            if (inventory.getStock() <= 0) {
                throw new RuntimeException("疫苗【" + inventory.getItemName() + "】库存不足，请先入库");
            }
        }

        // 2. 设置疫苗名称（从库存中获取）
        if (record.getInventoryId() != null) {
            Inventory inventory = inventoryMapper.getById(record.getInventoryId());
            record.setVaccineName(inventory.getItemName());
        }

        // 3. 保存疫苗记录
        int result = vaccineRecordMapper.insert(record);

        if (result > 0) {
            // 4. 扣减库存
            if (record.getInventoryId() != null) {
                inventoryMapper.updateStock(record.getInventoryId(), -1);
            }
        }

        return result > 0;
    }
    @Override
    public boolean updateVaccineRecord(PetVaccineRecord record) {
        return vaccineRecordMapper.update(record) > 0;
    }

    @Override
    public boolean deleteVaccineRecord(Integer id) {
        return vaccineRecordMapper.deleteById(id) > 0;
    }

    @Override
    public List<PetVaccineRecord> getUpcomingVaccines(Integer userId) {
        return vaccineRecordMapper.getUpcoming(userId);
    }

    // ========== 体检记录 ==========

    @Override
    public List<PetHealthRecord> getHealthRecords(Integer petId) {
        return healthRecordMapper.getByPetId(petId);
    }

    @Override
    public boolean addHealthRecord(PetHealthRecord record) {
        return healthRecordMapper.insert(record) > 0;
    }

    @Override
    public boolean updateHealthRecord(PetHealthRecord record) {
        return healthRecordMapper.update(record) > 0;
    }

    @Override
    public boolean deleteHealthRecord(Integer id) {
        return healthRecordMapper.deleteById(id) > 0;
    }

    // ========== 管理员接口实现 ==========

    @Override
    public Map<String, Object> getAdminPetList(Integer page, Integer pageSize, String keyword, String type, Integer status) {
        int offset = (page - 1) * pageSize;
        List<PetProfile> list = petProfileMapper.getAdminPetList(offset, pageSize, keyword, type, status);
        int total = petProfileMapper.countAdminPet(keyword, type, status);

        // 计算年龄
        for (PetProfile pet : list) {
            if (pet.getBirthday() != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                int currentYear = cal.get(java.util.Calendar.YEAR);
                cal.setTime(pet.getBirthday());
                int birthYear = cal.get(java.util.Calendar.YEAR);
                pet.setAge(currentYear - birthYear);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public boolean adminDeletePet(Integer id) {
        return petProfileMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getPetStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总宠物数
        int totalPets = petProfileMapper.countAll();
        stats.put("totalPets", totalPets);

        // 各类型宠物数量
        List<Map<String, Object>> typeCountList = petProfileMapper.countByType();
        Map<String, Integer> typeCount = new HashMap<>();

        // 初始化各类型默认值
        typeCount.put("dog", 0);
        typeCount.put("cat", 0);
        typeCount.put("rabbit", 0);
        typeCount.put("bird", 0);
        typeCount.put("fish", 0);
        typeCount.put("other", 0);

        // 遍历结果填充数据
        for (Map<String, Object> item : typeCountList) {
            String type = (String) item.get("type");
            Long count = (Long) item.get("count");
            if (type != null) {
                typeCount.put(type, count.intValue());
            }
        }

        stats.put("typeCount", typeCount);
        stats.put("dogCount", typeCount.getOrDefault("dog", 0));
        stats.put("catCount", typeCount.getOrDefault("cat", 0));

        // 今日新增宠物数
        int todayNew = petProfileMapper.countTodayNew();
        stats.put("todayNew", todayNew);

        // 近7天新增趋势
        List<Map<String, Object>> trend = petProfileMapper.getWeeklyTrend();
        stats.put("weeklyTrend", trend);

        //  添加性别统计
        Map<String, Object> genderStats = petProfileMapper.countByGender();
        stats.put("genderStats", genderStats);

        return stats;
    }

    @Override
    public Map<String, Object> getAllVaccineRecords(Integer page, Integer pageSize, String keyword) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> list = vaccineRecordMapper.getAllVaccineRecords(offset, pageSize, keyword);
        int total = vaccineRecordMapper.countAllVaccineRecords(keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Map<String, Object> getAllHealthRecords(Integer page, Integer pageSize, String keyword) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> list = healthRecordMapper.getAllHealthRecords(offset, pageSize, keyword);
        int total = healthRecordMapper.countAllHealthRecords(keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    @Transactional
    public boolean batchDeleteVaccineRecords(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return vaccineRecordMapper.batchDeleteByIds(ids) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeleteHealthRecords(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return healthRecordMapper.batchDeleteByIds(ids) > 0;
    }

    @Override
    public int getUpcomingPetsCount() {
        return vaccineRecordMapper.countUpcomingPets();
    }

    @Override
    public int getDistinctPetsWithVaccineCount() {
        return vaccineRecordMapper.countDistinctPetsWithVaccine();
    }

    @Override
    public int getDistinctPetsWithHealthRecordCount() {
        return healthRecordMapper.countDistinctPetsWithHealthRecord();
    }

    @Override
    public BigDecimal getAvgWeight() {
        BigDecimal avgWeight = healthRecordMapper.getAvgWeightByLatestRecord();
        return avgWeight != null ? avgWeight : BigDecimal.ZERO;
    }
}