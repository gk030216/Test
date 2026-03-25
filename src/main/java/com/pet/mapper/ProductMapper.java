package com.pet.mapper;

import com.pet.entity.Product;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ProductMapper {
    List<Product> getProductList(@Param("offset") Integer offset,
                                 @Param("limit") Integer limit,
                                 @Param("keyword") String keyword,
                                 @Param("categoryId") Integer categoryId,
                                 @Param("isHot") Integer isHot,
                                 @Param("isNew") Integer isNew);
    int countProduct(@Param("keyword") String keyword, @Param("categoryId") Integer categoryId);
    Product getById(@Param("id") Integer id);
    int updateStock(@Param("id") Integer id, @Param("quantity") Integer quantity);
}