package com.pet.service;

import com.pet.entity.ServiceItem;
import java.util.List;
import java.util.Map;

public interface ServiceItemService {

    // 前台：获取服务列表
    Map<String, Object> getServiceList(Integer page, Integer pageSize,
                                       Integer categoryId, String keyword, String sort);

    // 获取服务详情
    ServiceItem getById(Integer id);

    // 获取热门服务
    List<Map<String, Object>> getHotServices(int limit);

    // 后台：获取服务列表
    Map<String, Object> getAdminItemList(Integer page, Integer pageSize,
                                         String keyword, Integer categoryId, Integer status);

    // 添加服务
    boolean addItem(ServiceItem item);

    // 更新服务
    boolean updateItem(ServiceItem item);

    // 更新状态
    boolean updateStatus(Integer id, Integer status);

    // 删除服务
    boolean deleteItem(Integer id);

    // 获取统计数据
    Map<String, Object> getStatistics(String keyword, Integer categoryId, Integer status);
}