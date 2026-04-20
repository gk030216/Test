package com.pet.controller.admin;

import com.pet.entity.Category;
import com.pet.service.CategoryService;
import com.pet.util.ExcelUtil;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/category")
@CrossOrigin(origins = "*")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表（分页）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getCategoryList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer parentId) {  //  确保有 parentId 参数
        try {
            Map<String, Object> result = categoryService.getCategoryList(page, pageSize, keyword, status, parentId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    /**
     * 获取所有启用的分类（用于下拉选择）
     */
    @GetMapping("/all")
    public Result<List<Category>> getAllCategories() {
        try {
            List<Category> list = categoryService.getAllEnabledCategories();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Integer id) {
        try {
            Category category = categoryService.getById(id);
            if (category != null) {
                return Result.success(category);
            } else {
                return Result.error("分类不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加分类
     */
    @PostMapping("/add")
    public Result<?> addCategory(@RequestBody Category category) {
        try {
            boolean success = categoryService.addCategory(category);
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
     * 更新分类
     */
    @PutMapping("/update")
    public Result<?> updateCategory(@RequestBody Category category) {
        try {
            boolean success = categoryService.updateCategory(category);
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
     * 更新状态
     */
    @PutMapping("/status")
    public Result<?> updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        try {
            boolean success = categoryService.updateStatus(id, status);
            if (success) {
                return Result.success(status == 1 ? "启用成功" : "禁用成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteCategory(@PathVariable Integer id) {
        try {
            boolean success = categoryService.deleteCategory(id);
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
     * 批量删除
     */
    @DeleteMapping("/batch-delete")
    public Result<?> batchDelete(@RequestParam String ids) {
        try {
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            boolean success = categoryService.batchDelete(idList);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新分类排序
     */
    @PutMapping("/batch-sort")
    public Result<?> batchUpdateSort(@RequestBody List<Map<String, Integer>> sortList) {
        try {
            if (sortList == null || sortList.isEmpty()) {
                return Result.error("排序数据不能为空");
            }
            categoryService.batchUpdateSort(sortList);
            return Result.success("排序更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 导出分类列表
     */
    @GetMapping("/export")
    public void exportCategoryList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) {
        try {
            // ✅ 修复：添加第5个参数 parentId = null
            Map<String, Object> result = categoryService.getCategoryList(1, 9999, keyword, status, null);
            List<Category> list = (List<Category>) result.get("list");

            // 构建树形结构导出
            List<Map<String, Object>> exportList = new ArrayList<>();
            for (Category item : list) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", item.getId());
                row.put("name", item.getName());
                row.put("parentId", item.getParentId());
                row.put("parentName", getParentName(item.getParentId()));
                row.put("sortOrder", item.getSortOrder());
                row.put("status", item.getStatus() == 1 ? "启用" : "禁用");
                row.put("createTime", formatDate(item.getCreateTime()));
                exportList.add(row);
            }

            String[] headers = {"ID", "分类名称", "父分类ID", "父分类名称", "排序", "状态", "创建时间"};
            String[] fieldNames = {"id", "name", "parentId", "parentName", "sortOrder", "status", "createTime"};

            String fileName = "商品分类_" + System.currentTimeMillis() + ".xlsx";
            ExcelUtil.exportExcel(response, exportList, headers, fieldNames, "商品分类", fileName);

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

    private String getParentName(Integer parentId) {
        if (parentId == null || parentId == 0) return "顶级分类";
        // 这里需要注入 CategoryService 或使用其他方式获取父分类名称
        return String.valueOf(parentId);
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}