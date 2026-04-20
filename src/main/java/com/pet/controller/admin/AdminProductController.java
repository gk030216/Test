package com.pet.controller.admin;

import com.pet.entity.Inventory;
import com.pet.entity.Product;
import com.pet.mapper.InventoryMapper;
import com.pet.mapper.ProductMapper;
import com.pet.service.ProductService;
import com.pet.util.ExcelUtil;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/product")
@CrossOrigin(origins = "*")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    /**
     * 获取商品列表（后台）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,  // 改为 Integer
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = productService.getAdminProductList(page, pageSize, keyword, categoryId, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上架商品（从已有库存中选择）
     */
    @PostMapping("/add")
    @Transactional
    public Result<?> addProduct(@RequestBody Product product) {
        try {
            // 1. 检查库存是否存在
            if (product.getInventoryId() == null) {
                return Result.error("请选择要上架的商品");
            }

            Inventory inventory = inventoryMapper.getById(product.getInventoryId());
            if (inventory == null) {
                return Result.error("库存记录不存在");
            }

            // 2. 保存商品（不再创建新的库存记录）
            product.setSales(0);
            product.setStatus(1); // 上架状态
            // 商品表不再存储 stock，库存从 inventory 表获取

            int result = productMapper.insert(product);

            if (result > 0) {
                // 3. 更新库存表的 item_id，关联到商品（用于后续查询）
                inventory.setItemId(product.getId());
                inventoryMapper.update(inventory);

                return Result.success("上架成功");
            } else {
                return Result.error("上架失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/update")
    public Result<?> updateProduct(@RequestBody Product product) {
        try {
            boolean success = productService.updateProduct(product);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新商品状态（上架/下架）
     */
    @PutMapping("/status")
    public Result<?> updateProductStatus(@RequestParam Integer id, @RequestParam Integer status) {
        try {
            boolean success = productService.updateProductStatus(id, status);
            if (success) {
                return Result.success(status == 1 ? "上架成功" : "下架成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新商品状态
     */
    @PutMapping("/batch-status")
    public Result<?> batchUpdateProductStatus(@RequestParam String ids, @RequestParam Integer status) {
        try {
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            int successCount = 0;
            for (Integer id : idList) {
                if (productService.updateProductStatus(id, status)) {
                    successCount++;
                }
            }
            return Result.success("批量" + (status == 1 ? "上架" : "下架") + "成功，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteProduct(@PathVariable Integer id) {
        try {
            boolean success = productService.deleteProduct(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除商品
     */
    @DeleteMapping("/batch-delete")
    public Result<?> batchDeleteProducts(@RequestParam String ids) {
        try {
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            int successCount = 0;
            for (Integer id : idList) {
                if (productService.deleteProduct(id)) {
                    successCount++;
                }
            }
            return Result.success("批量删除成功，成功：" + successCount + "个");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 导出商品列表（Excel格式）
     */
    @GetMapping("/export")
    public void exportProductList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) {
        try {
            // 获取数据
            List<Product> list = productService.exportAdminProductList(keyword, categoryId, status);

            // 构建导出数据
            List<Map<String, Object>> exportList = new ArrayList<>();
            for (Product item : list) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", item.getId());
                row.put("name", item.getName());
                row.put("description", item.getDescription());
                row.put("price", item.getPrice());
                row.put("originalPrice", item.getOriginalPrice());
                row.put("categoryName", item.getCategoryName());
                row.put("sales", item.getSales());
                row.put("status", item.getStatus() == 1 ? "上架" : "下架");
                row.put("isHot", item.getIsHot() == 1 ? "是" : "否");
                row.put("isNew", item.getIsNew() == 1 ? "是" : "否");
                row.put("stock", item.getStock() != null ? item.getStock() : 0);
                row.put("createTime", formatDateTime(item.getCreateTime()));
                exportList.add(row);
            }

            // 定义表头
            String[] headers = {"ID", "商品名称", "描述", "价格", "原价", "分类", "销量", "状态", "热门", "新品", "库存", "创建时间"};
            String[] fieldNames = {"id", "name", "description", "price", "originalPrice", "categoryName", "sales", "status", "isHot", "isNew", "stock", "createTime"};

            String fileName = "商品列表_" + System.currentTimeMillis() + ".xlsx";
            ExcelUtil.exportExcel(response, exportList, headers, fieldNames, "商品列表", fileName);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("导出失败：" + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // 辅助方法：格式化日期时间
    private String formatDateTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 获取商品统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getProductStatistics() {
        try {
            Map<String, Object> statistics = productService.getProductStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取热销商品列表（后台管理用）
     */
    @GetMapping("/hot")
    public Result<List<Product>> getAdminHotProducts(@RequestParam(defaultValue = "8") Integer limit) {
        try {
            List<Product> hotProducts = productService.getHotProducts(limit);
            return Result.success(hotProducts);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}