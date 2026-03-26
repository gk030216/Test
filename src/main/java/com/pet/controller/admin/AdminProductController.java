package com.pet.controller.admin;

import com.pet.entity.Product;
import com.pet.service.ProductService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/product")
@CrossOrigin(origins = "*")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取商品列表（后台）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = productService.getAdminProductList(page, pageSize, keyword, categoryId, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加商品
     */
    @PostMapping("/add")
    public Result<?> addProduct(@RequestBody Product product) {
        try {
            boolean success = productService.addProduct(product);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
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
     * 更新商品状态
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
}