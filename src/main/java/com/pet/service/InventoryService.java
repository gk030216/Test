package com.pet.service;

import com.pet.entity.Inventory;

import java.util.List;
import java.util.Map;

public interface InventoryService {

    Map<String, Object> getInventoryList(Integer page, Integer pageSize, String keyword, String type);

    Inventory getById(Integer id);

    boolean addInventory(Inventory inventory);

    boolean updateInventory(Inventory inventory);

    boolean addStock(Integer id, Integer quantity);

    boolean deductStock(Integer id, Integer quantity);

    boolean deleteInventory(Integer id);

    Map<String, Object> getStatistics(String type);

    // 获取疫苗库存列表（用于导出）
    List<Inventory> getVaccineListForExport(String keyword);

    // 获取商品库存列表（用于导出）
    List<Inventory> getProductInventoryListForExport(String keyword);
}