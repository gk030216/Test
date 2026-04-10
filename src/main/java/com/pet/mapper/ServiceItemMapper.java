package com.pet.mapper;

import com.pet.entity.ServiceItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ServiceItemMapper {

    // 前台：获取服务列表
    List<ServiceItem> getServiceList(@Param("offset") Integer offset,
                                     @Param("limit") Integer limit,
                                     @Param("categoryId") Integer categoryId,
                                     @Param("keyword") String keyword,
                                     @Param("sort") String sort);

    // 前台：统计数量
    int countService(@Param("categoryId") Integer categoryId,
                     @Param("keyword") String keyword);

    // 获取服务详情
    ServiceItem getById(@Param("id") Integer id);


    // 后台：获取服务列表
    List<ServiceItem> getAdminItemList(@Param("offset") Integer offset,
                                       @Param("limit") Integer limit,
                                       @Param("keyword") String keyword,
                                       @Param("categoryId") Integer categoryId,
                                       @Param("status") Integer status);

    // 后台：统计数量
    int countAdminItem(@Param("keyword") String keyword,
                       @Param("categoryId") Integer categoryId,
                       @Param("status") Integer status);

    // 插入
    int insert(ServiceItem item);

    // 更新
    int update(ServiceItem item);

    // 更新状态
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    // 删除
    int deleteById(@Param("id") Integer id);

    // 增加销量
    int incrementSales(@Param("id") Integer id, @Param("quantity") Integer quantity);

    // 统计热门服务数量
    int countHotItems(@Param("keyword") String keyword,
                      @Param("categoryId") Integer categoryId);

    List<Map<String, Object>> getHotServices(@Param("limit") int limit);
}