package com.pet.service;

import com.pet.entity.ServiceCategory;
import java.util.List;
import java.util.Map;

public interface ServiceCategoryService {

    // 前台：获取启用的分类（树形）
    List<ServiceCategory> getEnabledCategories();

    // 后台：获取所有分类（用于父分类下拉）
    List<ServiceCategory> getAllCategories();

    // 后台：获取分类列表（分页）
    Map<String, Object> getCategoryList(Integer page, Integer pageSize, String keyword, Integer status);

    // 根据ID获取
    ServiceCategory getById(Integer id);

    // 添加
    boolean addCategory(ServiceCategory category);

    // 更新
    boolean updateCategory(ServiceCategory category);

    // 更新状态
    boolean updateStatus(Integer id, Integer status);

    // 删除
    boolean deleteCategory(Integer id);
}