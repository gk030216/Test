package com.pet.service;

import com.pet.entity.Product;
import java.util.List;
import java.util.Map;

public interface ProductService {

    // 前台接口
    Map<String, Object> getProductList(Integer page, Integer pageSize, String keyword,
                                       Integer categoryId, Integer isHot, Integer isNew);
    Product getProductById(Integer id);
    List<Product> getHotProducts();
    List<Product> getNewProducts();
    boolean updateStock(Integer productId, Integer quantity);

    // 后台接口
    Map<String, Object> getAdminProductList(Integer page, Integer pageSize, String keyword,
                                            String categoryId, Integer status);
    boolean addProduct(Product product);
    boolean updateProduct(Product product);
    boolean updateProductStatus(Integer id, Integer status);
    boolean deleteProduct(Integer id);
}