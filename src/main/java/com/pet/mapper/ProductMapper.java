package com.pet.mapper;

import com.pet.entity.Product;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductMapper {

    // 前台查询（只查上架商品）- 使用 Integer categoryId 支持多级分类
    List<Product> getProductList(@Param("offset") Integer offset,
                                 @Param("limit") Integer limit,
                                 @Param("keyword") String keyword,
                                 @Param("categoryId") Integer categoryId,
                                 @Param("isHot") Integer isHot,
                                 @Param("isNew") Integer isNew);

    int countProduct(@Param("keyword") String keyword,
                     @Param("categoryId") Integer categoryId);

// 后台查询（查所有商品）
    List<Product> getAdminProductList(@Param("offset") Integer offset,
                                      @Param("limit") Integer limit,
                                      @Param("keyword") String keyword,
                                      @Param("categoryId") Integer categoryId,  // 改为 Integer
                                      @Param("status") Integer status);

    int countAdminProduct(@Param("keyword") String keyword,
                          @Param("categoryId") Integer categoryId,  // 改为 Integer
                          @Param("status") Integer status);

    Product getById(@Param("id") Integer id);

    int insert(Product product);

    int update(Product product);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    int updateStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    int deleteById(@Param("id") Integer id);

    int updateRating(@Param("id") Integer id,
                     @Param("avgRating") BigDecimal avgRating,
                     @Param("commentCount") Integer commentCount);

    // 导出所有商品（根据筛选条件）
    List<Product> exportAdminProductList(@Param("keyword") String keyword,
                                         @Param("categoryId") Integer categoryId,
                                         @Param("status") Integer status);

    // 获取商品统计数据
    Map<String, Object> getProductStatistics();

    // 获取分类商品数量分布
    List<Map<String, Object>> getCategoryDistribution();

    // 获取销量排行Top N
    List<Map<String, Object>> getSalesRank(@Param("limit") Integer limit);

    // 获取价格区间分布
    List<Map<String, Object>> getPriceDistribution();

    // 获取商品状态分布（上架/下架数量）
    Map<String, Object> getStatusDistribution();

    // 更新商品销量
    int updateSales(@Param("id") Integer id, @Param("quantity") Integer quantity);
}