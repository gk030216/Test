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

    @Autowired
    private ServiceItemMapper serviceItemMapper;

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
    public List<Map<String, Object>> getHotServices(int limit) {
        return serviceItemMapper.getHotServices(limit);
    }

    @Override
    public List<ServiceItem> getHotServiceItems(int limit) {
        return serviceItemMapper.getHotServiceItems(limit);
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

    @Override
    public Map<String, Object> getStatistics(String keyword, Integer categoryId, Integer status) {
        Map<String, Object> stats = new HashMap<>();

        // 总服务数
        int total = itemMapper.countAdminItem(keyword, categoryId, null);
        stats.put("total", total);

        // 上架数量
        int onSale = itemMapper.countAdminItem(keyword, categoryId, 1);
        stats.put("onSale", onSale);

        // 下架数量
        int offSale = itemMapper.countAdminItem(keyword, categoryId, 0);
        stats.put("offSale", offSale);

        // 热门服务数量
        int hotCount = itemMapper.countHotItems(keyword, categoryId);
        stats.put("hotCount", hotCount);

        return stats;
    }
}