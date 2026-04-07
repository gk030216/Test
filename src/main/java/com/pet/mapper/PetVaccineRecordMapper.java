package com.pet.mapper;

import com.pet.entity.PetVaccineRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PetVaccineRecordMapper {

    // 获取宠物的疫苗记录
    List<PetVaccineRecord> getByPetId(@Param("petId") Integer petId);

    // 添加疫苗记录
    int insert(PetVaccineRecord record);

    // 更新疫苗记录
    int update(PetVaccineRecord record);

    // 删除疫苗记录
    int deleteById(@Param("id") Integer id);

    // 获取即将到期的疫苗（7天内）
    List<PetVaccineRecord> getUpcoming(@Param("userId") Integer userId);

    // ========== 以下方法需要添加 ==========

    // 获取所有疫苗记录（管理员用）
    List<Map<String, Object>> getAllVaccineRecords(@Param("offset") Integer offset,
                                                   @Param("limit") Integer limit,
                                                   @Param("keyword") String keyword);

    // 统计所有疫苗记录
    int countAllVaccineRecords(@Param("keyword") String keyword);
}