package com.pet.service.impl;

import com.pet.entity.ServiceCategory;
import com.pet.mapper.ServiceCategoryMapper;
import com.pet.service.ServiceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceCategoryServiceImpl implements ServiceCategoryService {

    @Autowired
    private ServiceCategoryMapper categoryMapper;

    @Override
    public List<ServiceCategory> getEnabledCategories() {
        // 一级分类，直接返回列表，不需要构建树
        return categoryMapper.getEnabledCategories();
    }

    @Override
    public List<ServiceCategory> getAllCategories() {
        return categoryMapper.getAllCategories(null);
    }

    @Override
    public Map<String, Object> getCategoryList(Integer page, Integer pageSize, String keyword, Integer status) {
        int offset = (page - 1) * pageSize;
        List<ServiceCategory> list = categoryMapper.getCategoryList(offset, pageSize, keyword, status);
        int total = categoryMapper.countCategoryList(keyword, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public ServiceCategory getById(Integer id) {
        return categoryMapper.getById(id);
    }

    @Override
    public boolean addCategory(ServiceCategory category) {
        // 一级分类，不需要设置 parentId
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        return categoryMapper.insert(category) > 0;
    }

    @Override
    public boolean updateCategory(ServiceCategory category) {
        return categoryMapper.update(category) > 0;
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        ServiceCategory category = new ServiceCategory();
        category.setId(id);
        category.setStatus(status);
        return categoryMapper.update(category) > 0;
    }

    @Override
    public boolean deleteCategory(Integer id) {
        // 一级分类，只需要检查分类下是否有服务
        int serviceCount = categoryMapper.countServices(id);
        if (serviceCount > 0) {
            throw new RuntimeException("请先删除该分类下的服务");
        }
        return categoryMapper.deleteById(id) > 0;
    }
}