package com.pet.service.impl;

import com.pet.entity.ServiceItem;
import com.pet.mapper.ServiceItemMapper;
import com.pet.service.ServiceItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceItemServiceImpl implements ServiceItemService {

    @Autowired
    private ServiceItemMapper itemMapper;

    @Override
    public Map<String, Object> getServiceList(Integer page, Integer pageSize,
                                              Integer categoryId, String keyword, String sort) {
        int offset = (page - 1) * pageSize;
        List<ServiceItem> list = itemMapper.getServiceList(offset, pageSize, categoryId, keyword, sort);
        int total = itemMapper.countService(categoryId, keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public ServiceItem getById(Integer id) {
        return itemMapper.getById(id);
    }

    @Override
    public List<ServiceItem> getHotServices() {
        return itemMapper.getHotServices();
    }

    @Override
    public Map<String, Object> getAdminItemList(Integer page, Integer pageSize,
                                                String keyword, Integer categoryId, Integer status) {
        int offset = (page - 1) * pageSize;
        List<ServiceItem> list = itemMapper.getAdminItemList(offset, pageSize, keyword, categoryId, status);
        int total = itemMapper.countAdminItem(keyword, categoryId, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public boolean addItem(ServiceItem item) {
        if (item.getStatus() == null) item.setStatus(1);
        if (item.getIsHot() == null) item.setIsHot(0);
        if (item.getSales() == null) item.setSales(0);
        return itemMapper.insert(item) > 0;
    }

    @Override
    public boolean updateItem(ServiceItem item) {
        return itemMapper.update(item) > 0;
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        return itemMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean deleteItem(Integer id) {
        return itemMapper.deleteById(id) > 0;
    }
}