package com.pet.mapper;

import com.pet.entity.StaffStatistics;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface StaffStatisticsMapper {

    /**
     * 获取所有员工统计（用于匹配度计算）
     */
    List<StaffStatistics> getAllStaffStatistics();

    /**
     * 根据员工ID获取统计
     */
    StaffStatistics getByStaffId(@Param("staffId") Integer staffId);

    /**
     * 初始化员工统计
     */
    int insert(StaffStatistics statistics);

    /**
     * 更新员工统计（服务完成后调用）
     */
    int updateStatistics(@Param("staffId") Integer staffId,
                         @Param("rating") Integer rating);

    /**
     * 重新计算所有员工的统计数据（定时任务或手动触发）
     */
    int recalculateAllStatistics();

    /**
     * ✅ 增加服务次数（没有评价时使用）
     */
    int incrementServiceCount(@Param("staffId") Integer staffId);
}