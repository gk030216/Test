package com.pet.mapper;

import com.pet.entity.PetProfile;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PetProfileMapper {

    // 获取用户的宠物列表
    List<PetProfile> getByUserId(@Param("userId") Integer userId);

    // 获取默认宠物
    PetProfile getDefaultByUserId(@Param("userId") Integer userId);

    // 根据ID获取宠物详情
    PetProfile getById(@Param("id") Integer id, @Param("userId") Integer userId);

    // 添加宠物
    int insert(PetProfile pet);

    // 更新宠物
    int update(PetProfile pet);

    // 删除宠物（软删除）
    int deleteById(@Param("id") Integer id);

    // 清除默认宠物
    int clearDefault(@Param("userId") Integer userId);

    // 设置默认宠物
    int setDefault(@Param("id") Integer id, @Param("userId") Integer userId);

    // 获取宠物数量
    int countByUserId(@Param("userId") Integer userId);

    // ========== 新增方法 ==========

    /**
     * 根据宠物ID获取宠物名称
     */
    String getPetNameById(@Param("id") Integer id);

    // ========== 管理员接口 ==========

    // 获取所有宠物列表（分页）
    List<PetProfile> getAdminPetList(@Param("offset") Integer offset,
                                     @Param("limit") Integer limit,
                                     @Param("keyword") String keyword,
                                     @Param("type") String type,
                                     @Param("status") Integer status);

    // 统计宠物总数
    int countAdminPet(@Param("keyword") String keyword,
                      @Param("type") String type,
                      @Param("status") Integer status);

    // 统计所有宠物数
    int countAll();

    // 按类型统计
    List<Map<String, Object>> countByType();

    // 今日新增
    int countTodayNew();

    // 近7天趋势
    List<Map<String, Object>> getWeeklyTrend();

    /**
     * 按性别统计宠物数量
     */
    Map<String, Object> countByGender();
}