package com.pet.mapper;

import com.pet.entity.PetHealthRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PetHealthRecordMapper {

    // 获取宠物体检记录
    List<PetHealthRecord> getByPetId(@Param("petId") Integer petId);

    // 添加体检记录
    int insert(PetHealthRecord record);

    // 更新体检记录
    int update(PetHealthRecord record);

    // 删除体检记录
    int deleteById(@Param("id") Integer id);

    // ========== 以下方法需要添加 ==========

    // 获取所有体检记录（管理员用）
    List<Map<String, Object>> getAllHealthRecords(@Param("offset") Integer offset,
                                                  @Param("limit") Integer limit,
                                                  @Param("keyword") String keyword);

    // 统计所有体检记录
    int countAllHealthRecords(@Param("keyword") String keyword);
}