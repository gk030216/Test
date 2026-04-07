package com.pet.service.impl;

import com.pet.entity.ServiceCategory;
import com.pet.mapper.ServiceCategoryMapper;
import com.pet.service.ServiceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceCategoryServiceImpl implements ServiceCategoryService {

    @Autowired
    private ServiceCategoryMapper categoryMapper;

    @Override
    public List<ServiceCategory> getEnabledCategories() {
        List<ServiceCategory> list = categoryMapper.getEnabledCategories();
        return buildTree(list);
    }

    @Override
    public List<ServiceCategory> getAllCategories() {
        List<ServiceCategory> list = categoryMapper.getAllCategories(null);
        return list;
    }

    @Override
    public Map<String, Object> getCategoryList(Integer page, Integer pageSize, String keyword, Integer status) {
        int offset = (page - 1) * pageSize;
        List<ServiceCategory> list = categoryMapper.getCategoryList(offset, pageSize, keyword, status);
        int total = categoryMapper.countCategoryList(keyword, status);

        // 设置父分类名称
        for (ServiceCategory category : list) {
            if (category.getParentId() != null && category.getParentId() > 0) {
                ServiceCategory parent = categoryMapper.getById(category.getParentId());
                if (parent != null) {
                    category.setParentName(parent.getName());
                }
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
    public ServiceCategory getById(Integer id) {
        return categoryMapper.getById(id);
    }

    @Override
    public boolean addCategory(ServiceCategory category) {
        if (category.getParentId() == null) category.setParentId(0);
        if (category.getSortOrder() == null) category.setSortOrder(0);
        if (category.getStatus() == null) category.setStatus(1);
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
        // 检查是否有子分类
        int childrenCount = categoryMapper.countChildren(id);
        if (childrenCount > 0) {
            throw new RuntimeException("请先删除子分类");
        }
        // 检查分类下是否有服务
        int serviceCount = categoryMapper.countServices(id);
        if (serviceCount > 0) {
            throw new RuntimeException("请先删除该分类下的服务");
        }
        return categoryMapper.deleteById(id) > 0;
    }

    private List<ServiceCategory> buildTree(List<ServiceCategory> list) {
        List<ServiceCategory> tree = new ArrayList<>();
        for (ServiceCategory category : list) {
            if (category.getParentId() == 0) {
                tree.add(findChildren(category, list));
            }
        }
        return tree;
    }

    private ServiceCategory findChildren(ServiceCategory parent, List<ServiceCategory> all) {
        List<ServiceCategory> children = new ArrayList<>();
        for (ServiceCategory category : all) {
            if (category.getParentId() != null && category.getParentId().equals(parent.getId())) {
                children.add(findChildren(category, all));
            }
        }
        parent.setChildren(children);
        return parent;
    }
}