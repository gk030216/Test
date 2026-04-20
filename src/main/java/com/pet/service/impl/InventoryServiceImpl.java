package com.pet.service.impl;

import com.pet.entity.Inventory;
import com.pet.mapper.InventoryMapper;
import com.pet.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    public Map<String, Object> getInventoryList(Integer page, Integer pageSize, String keyword, String type) {
        int offset = (page - 1) * pageSize;
        List<Inventory> list = inventoryMapper.getInventoryList(offset, pageSize, keyword, type);
        int total = inventoryMapper.countInventoryList(keyword, type);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Inventory getById(Integer id) {
        return inventoryMapper.getById(id);
    }

    @Override
    public boolean addInventory(Inventory inventory) {
        if (inventory.getStock() == null) inventory.setStock(0);
        if (inventory.getUnit() == null) inventory.setUnit("件");
        inventory.setStatus(1);
        return inventoryMapper.insert(inventory) > 0;
    }

    @Override
    public boolean updateInventory(Inventory inventory) {
        return inventoryMapper.update(inventory) > 0;
    }

    @Override
    public boolean addStock(Integer id, Integer quantity) {
        return inventoryMapper.updateStock(id, quantity) > 0;
    }

    @Override
    public boolean deductStock(Integer id, Integer quantity) {
        return inventoryMapper.updateStock(id, -quantity) > 0;
    }

    @Override
    public boolean deleteInventory(Integer id) {
        return inventoryMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getStatistics(String type) {
        return inventoryMapper.getStatistics(type);
    }


    @Override
    public List<Inventory> getVaccineListForExport(String keyword) {
        return inventoryMapper.getVaccineListForExport(keyword);
    }

    @Override
    public List<Inventory> getProductInventoryListForExport(String keyword) {
        return inventoryMapper.getProductInventoryListForExport(keyword);
    }

}