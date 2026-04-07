package com.pet.mapper;

import com.pet.entity.ServiceCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ServiceCategoryMapper {

    // 获取所有启用的分类（前台）
    List<ServiceCategory> getEnabledCategories();

    // 获取所有分类（后台）
    List<ServiceCategory> getAllCategories(@Param("keyword") String keyword);

    // 获取分类列表（分页）
    List<ServiceCategory> getCategoryList(@Param("offset") Integer offset,
                                          @Param("limit") Integer limit,
                                          @Param("keyword") String keyword,
                                          @Param("status") Integer status);

    // 统计分类数量
    int countCategoryList(@Param("keyword") String keyword,
                          @Param("status") Integer status);

    // 根据ID获取
    ServiceCategory getById(@Param("id") Integer id);

    // 检查是否有子分类
    int countChildren(@Param("parentId") Integer parentId);

    // 检查分类下是否有服务
    int countServices(@Param("categoryId") Integer categoryId);

    // 插入
    int insert(ServiceCategory category);

    // 更新
    int update(ServiceCategory category);

    // 删除
    int deleteById(@Param("id") Integer id);
}