package com.pet.mapper;

import com.pet.entity.Product;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

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
                                      @Param("categoryId") String categoryId,
                                      @Param("status") Integer status);

    int countAdminProduct(@Param("keyword") String keyword,
                          @Param("categoryId") String categoryId,
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
}