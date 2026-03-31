package com.pet.service.impl;

import com.pet.entity.Product;
import com.pet.mapper.ProductMapper;
import com.pet.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    // ============= 前台接口 =============

    @Override
    public Map<String, Object> getProductList(Integer page, Integer pageSize, String keyword,
                                              Integer categoryId, Integer isHot, Integer isNew) {
        int offset = (page - 1) * pageSize;
        List<Product> list = productMapper.getProductList(offset, pageSize, keyword, categoryId, isHot, isNew);
        int total = productMapper.countProduct(keyword, categoryId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Product getProductById(Integer id) {
        return productMapper.getById(id);
    }

    @Override
    public List<Product> getHotProducts() {
        return productMapper.getProductList(0, 6, null, null, 1, null);
    }

    @Override
    public List<Product> getNewProducts() {
        return productMapper.getProductList(0, 6, null, null, null, 1);
    }

    @Override
    public boolean updateStock(Integer productId, Integer quantity) {
        return productMapper.updateStock(productId, quantity) > 0;
    }

    // ============= 后台接口 =============

    @Override
    public Map<String, Object> getAdminProductList(Integer page, Integer pageSize, String keyword,
                                                   String categoryId, Integer status) {
        int offset = (page - 1) * pageSize;
        List<Product> list = productMapper.getAdminProductList(offset, pageSize, keyword, categoryId, status);
        int total = productMapper.countAdminProduct(keyword, categoryId, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public boolean addProduct(Product product) {
        product.setSales(0);
        product.setStatus(product.getStatus() == null ? 1 : product.getStatus());
        product.setIsHot(product.getIsHot() == null ? 0 : product.getIsHot());
        product.setIsNew(product.getIsNew() == null ? 0 : product.getIsNew());
        return productMapper.insert(product) > 0;
    }

    @Override
    public boolean updateProduct(Product product) {
        return productMapper.update(product) > 0;
    }

    @Override
    public boolean updateProductStatus(Integer id, Integer status) {
        return productMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean deleteProduct(Integer id) {
        return productMapper.deleteById(id) > 0;
    }
}