package com.pet.service.impl;

import com.pet.entity.Category;
import com.pet.mapper.CategoryMapper;
import com.pet.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Map<String, Object> getCategoryList(Integer page, Integer pageSize, String keyword, Integer status, Integer parentId) {
        int offset = (page - 1) * pageSize;
        List<Category> list = categoryMapper.getCategoryList(offset, pageSize, keyword, status, parentId);
        int total = categoryMapper.countCategory(keyword, status, parentId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public List<Category> getAllEnabledCategories() {
        return categoryMapper.getAllEnabledCategories();
    }

    @Override
    public Category getById(Integer id) {
        return categoryMapper.getById(id);
    }

    @Override
    @Transactional
    public boolean addCategory(Category category) {
        if (category.getParentId() == null) {
            category.setParentId(0);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        return categoryMapper.insert(category) > 0;
    }

    @Override
    @Transactional
    public boolean updateCategory(Category category) {
        return categoryMapper.update(category) > 0;
    }

    @Override
    @Transactional
    public boolean updateStatus(Integer id, Integer status) {
        return categoryMapper.updateStatus(id, status) > 0;
    }

    @Override
    @Transactional
    public boolean deleteCategory(Integer id) {
        int childrenCount = categoryMapper.countChildren(id);
        if (childrenCount > 0) {
            throw new RuntimeException("请先删除该分类下的子分类");
        }
        return categoryMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean batchDelete(List<Integer> ids) {
        for (Integer id : ids) {
            int childrenCount = categoryMapper.countChildren(id);
            if (childrenCount > 0) {
                throw new RuntimeException("分类ID " + id + " 下存在子分类，请先删除子分类");
            }
        }
        return categoryMapper.batchDelete(ids) > 0;
    }

    @Override
    @Transactional
    public void batchUpdateSort(List<Map<String, Integer>> sortList) {
        if (sortList == null || sortList.isEmpty()) {
            return;
        }
        for (Map<String, Integer> item : sortList) {
            categoryMapper.updateSortOrder(item.get("id"), item.get("sortOrder"));
        }
    }
}