package com.pet.service;

import com.pet.entity.Category;
import java.util.List;
import java.util.Map;

public interface CategoryService {

    /**
     * 获取分类列表（分页）
     */
    Map<String, Object> getCategoryList(Integer page, Integer pageSize, String keyword, Integer status);

    /**
     * 获取所有启用的分类
     */
    List<Category> getAllEnabledCategories();

    /**
     * 根据ID获取分类
     */
    Category getById(Integer id);

    /**
     * 添加分类
     */
    boolean addCategory(Category category);

    /**
     * 更新分类
     */
    boolean updateCategory(Category category);

    /**
     * 更新状态
     */
    boolean updateStatus(Integer id, Integer status);

    /**
     * 删除分类
     */
    boolean deleteCategory(Integer id);

    /**
     * 批量删除
     */
    boolean batchDelete(List<Integer> ids);
}