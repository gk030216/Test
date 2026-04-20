package com.pet.service.impl;

import com.pet.entity.Inventory;
import com.pet.entity.Product;
import com.pet.mapper.InventoryMapper;
import com.pet.mapper.ProductMapper;
import com.pet.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

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
        Product product = productMapper.getById(id);
        if (product != null) {
            // 获取实时库存
            Inventory inventory = inventoryMapper.getByTypeAndItemId("product", id);
            if (inventory != null) {
                product.setStock(inventory.getStock());
            } else {
                product.setStock(0);
            }
        }
        return product;
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
                                                   Integer categoryId, Integer status) {
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
    @Transactional
    public boolean updateProductStatus(Integer id, Integer status) {
        // 如果是上架操作，检查库存
        if (status == 1) {
            // 检查库存表中是否有该商品且库存>0
            Inventory inventory = inventoryMapper.getByTypeAndItemId("product", id);
            if (inventory == null) {
                throw new RuntimeException("请先在库存管理中创建该商品库存");
            }
            if (inventory.getStock() <= 0) {
                throw new RuntimeException("库存不足，无法上架");
            }
        }
        return productMapper.updateStatus(id, status) > 0;
    }
    @Override
    public boolean deleteProduct(Integer id) {
        return productMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getProductStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 基础统计
        Map<String, Object> stats = productMapper.getProductStatistics();
        if (stats != null) {
            result.putAll(stats);
        } else {
            result.put("totalProducts", 0);
            result.put("totalSales", 0);
            result.put("totalRevenue", 0);
            result.put("avgRating", 0);
        }

        // 分类分布
        List<Map<String, Object>> categoryDistribution = productMapper.getCategoryDistribution();
        result.put("categoryDistribution", categoryDistribution != null ? categoryDistribution : new ArrayList<>());

        // 销量排行
        List<Map<String, Object>> salesRank = productMapper.getSalesRank(10);
        result.put("salesRank", salesRank != null ? salesRank : new ArrayList<>());

        // 价格区间分布
        List<Map<String, Object>> priceDistribution = productMapper.getPriceDistribution();
        result.put("priceDistribution", priceDistribution != null ? priceDistribution : new ArrayList<>());

        // 状态分布
        Map<String, Object> statusDistribution = productMapper.getStatusDistribution();
        List<Map<String, Object>> statusList = new ArrayList<>();
        if (statusDistribution != null) {
            Map<String, Object> onShelf = new HashMap<>();
            onShelf.put("name", "上架");
            onShelf.put("value", statusDistribution.get("onShelf"));
            statusList.add(onShelf);

            Map<String, Object> offShelf = new HashMap<>();
            offShelf.put("name", "下架");
            offShelf.put("value", statusDistribution.get("offShelf"));
            statusList.add(offShelf);
        }
        result.put("statusDistribution", statusList);

        return result;
    }

    @Override
    public List<Product> getHotProducts(Integer limit) {
        List<Map<String, Object>> rankList = productMapper.getSalesRank(limit);
        List<Product> hotProducts = new ArrayList<>();
        if (rankList != null) {
            for (Map<String, Object> item : rankList) {
                Product product = new Product();
                product.setId((Integer) item.get("id"));
                product.setName((String) item.get("name"));
                product.setSales((Integer) item.get("sales"));
                product.setPrice((BigDecimal) item.get("price"));
                product.setImage((String) item.get("image"));
                hotProducts.add(product);
            }
        }
        return hotProducts;
    }

    // 添加这个方法 ↓↓↓
    @Override
    public List<Product> exportAdminProductList(String keyword, Integer categoryId, Integer status) {
        return productMapper.exportAdminProductList(keyword, categoryId, status);
    }
}