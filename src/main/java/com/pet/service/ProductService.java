// ProductService.java
package com.pet.service;

import com.pet.entity.Product;
import java.util.List;
import java.util.Map;

public interface ProductService {
    Map<String, Object> getProductList(Integer page, Integer pageSize, String keyword,
                                       Integer categoryId, Integer isHot, Integer isNew);
    Product getProductById(Integer id);
    List<Product> getHotProducts();
    List<Product> getNewProducts();
    boolean updateStock(Integer productId, Integer quantity);
}

