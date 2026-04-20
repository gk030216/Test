package com.pet.mapper;

import com.pet.entity.Inventory;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface InventoryMapper {

    List<Inventory> getInventoryList(@Param("offset") Integer offset,
                                     @Param("limit") Integer limit,
                                     @Param("keyword") String keyword,
                                     @Param("type") String type);

    int countInventoryList(@Param("keyword") String keyword,
                           @Param("type") String type);

    Inventory getById(@Param("id") Integer id);

    Inventory getByTypeAndItemId(@Param("type") String type, @Param("itemId") Integer itemId);

    int insert(Inventory inventory);

    int update(Inventory inventory);

    int updateStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    int deleteById(@Param("id") Integer id);

    Map<String, Object> getStatistics(@Param("type") String type);

    /**
     * 根据类型和关联ID更新库存
     */
    int updateStockByItemId(@Param("type") String type,
                            @Param("itemId") Integer itemId,
                            @Param("quantity") Integer quantity);

    // 获取疫苗库存列表（用于导出）
    List<Inventory> getVaccineListForExport(@Param("keyword") String keyword);

    // 获取商品库存列表（用于导出）
    List<Inventory> getProductInventoryListForExport(@Param("keyword") String keyword);

}