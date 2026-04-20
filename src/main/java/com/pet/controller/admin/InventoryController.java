package com.pet.controller.admin;

import com.pet.entity.Inventory;
import com.pet.service.InventoryService;
import com.pet.util.ExcelUtil;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/admin/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    private Integer getUserRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getInventoryList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam String type,
            HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            Map<String, Object> result = inventoryService.getInventoryList(page, pageSize, keyword, type);
            Map<String, Object> stats = inventoryService.getStatistics(type);
            result.put("statistics", stats);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<?> addInventory(@RequestBody Inventory inventory, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = inventoryService.addInventory(inventory);
            return success ? Result.success("添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<?> updateInventory(@RequestBody Inventory inventory, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = inventoryService.updateInventory(inventory);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/add-stock/{id}")
    public Result<?> addStock(@PathVariable Integer id, @RequestBody Map<String, Integer> params, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            Integer quantity = params.get("quantity");
            if (quantity == null || quantity <= 0) {
                return Result.error("数量必须大于0");
            }
            boolean success = inventoryService.addStock(id, quantity);
            return success ? Result.success("入库成功") : Result.error("入库失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteInventory(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer role = getUserRole(request);
            if (role != 2 && role != 3) {
                return Result.error(403, "无权限访问");
            }
            boolean success = inventoryService.deleteInventory(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 导出疫苗库存列表
     */
    @GetMapping("/vaccine/export")
    public void exportVaccineList(
            @RequestParam(required = false) String keyword,
            HttpServletResponse response) {
        try {
            // 获取所有疫苗库存数据（不分页）
            List<Inventory> list = inventoryService.getVaccineListForExport(keyword);

            // 构建导出数据
            List<Map<String, Object>> exportList = new ArrayList<>();
            for (Inventory item : list) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", item.getId());
                row.put("itemName", item.getItemName());
                row.put("stock", item.getStock());
                row.put("unit", item.getUnit());
                row.put("status", item.getStatus() != null && item.getStatus() == 1 ? "正常" : "禁用");
                row.put("createTime", formatDateTime(item.getCreateTime()));
                row.put("updateTime", formatDateTime(item.getUpdateTime()));
                exportList.add(row);
            }

            // 定义表头和字段名
            String[] headers = {"ID", "疫苗名称", "库存数量", "单位", "状态", "创建时间", "更新时间"};
            String[] fieldNames = {"id", "itemName", "stock", "unit", "status", "createTime", "updateTime"};

            String fileName = "疫苗库存列表_" + System.currentTimeMillis() + ".xlsx";
            ExcelUtil.exportExcel(response, exportList, headers, fieldNames, "疫苗库存", fileName);

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


    /**
     * 导出商品库存列表
     */
    @GetMapping("/product/export")
    public void exportProductInventoryList(
            @RequestParam(required = false) String keyword,
            HttpServletResponse response) {
        try {
            // 获取所有商品库存数据（不分页）
            List<Inventory> list = inventoryService.getProductInventoryListForExport(keyword);

            // 构建导出数据
            List<Map<String, Object>> exportList = new ArrayList<>();
            for (Inventory item : list) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", item.getId());
                row.put("itemName", item.getItemName());
                row.put("stock", item.getStock());
                row.put("unit", item.getUnit());
                row.put("status", item.getStatus() != null && item.getStatus() == 1 ? "正常" : "禁用");
                row.put("createTime", formatDateTime(item.getCreateTime()));
                row.put("updateTime", formatDateTime(item.getUpdateTime()));
                exportList.add(row);
            }

            // 定义表头和字段名
            String[] headers = {"ID", "商品名称", "库存数量", "单位", "状态", "创建时间", "更新时间"};
            String[] fieldNames = {"id", "itemName", "stock", "unit", "status", "createTime", "updateTime"};

            String fileName = "商品库存列表_" + System.currentTimeMillis() + ".xlsx";
            ExcelUtil.exportExcel(response, exportList, headers, fieldNames, "商品库存", fileName);

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

    /**
     * 格式化日期时间
     */
    private String formatDateTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}