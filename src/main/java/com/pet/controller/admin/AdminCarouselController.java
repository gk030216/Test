package com.pet.controller.admin;

import com.pet.entity.Carousel;
import com.pet.service.CarouselService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/carousel")
public class AdminCarouselController {

    @Autowired
    private CarouselService carouselService;

    /**
     * 获取轮播图列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = carouselService.getCarouselList(page, pageSize, keyword, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取轮播图详情
     */
    @GetMapping("/{id}")
    public Result<Carousel> getById(@PathVariable Integer id) {
        try {
            Carousel carousel = carouselService.getById(id);
            if (carousel != null) {
                return Result.success(carousel);
            } else {
                return Result.error("轮播图不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加轮播图
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Carousel carousel) {
        try {
            boolean success = carouselService.addCarousel(carousel);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("添加失败");
        }
    }

    /**
     * 更新轮播图
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Carousel carousel) {
        try {
            boolean success = carouselService.updateCarousel(carousel);
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
            boolean success = carouselService.updateStatus(id, status);
            if (success) {
                return Result.success(status == 1 ? "启用成功" : "禁用成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("操作失败");
        }
    }

    @PutMapping("/batch-status")
    public Result<?> batchUpdateStatus(@RequestParam String ids, @RequestParam Integer status) {
        try {
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            boolean success = carouselService.batchUpdateStatus(idList, status);
            if (success) {
                return Result.success("批量" + (status == 1 ? "启用" : "禁用") + "成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("操作失败");
        }
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        try {
            boolean success = carouselService.deleteCarousel(id);
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
            boolean success = carouselService.batchDelete(idList);
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
     * 批量更新轮播图排序
     */
    @PutMapping("/batch-sort")
    public Result<?> batchUpdateSort(@RequestBody List<Map<String, Integer>> sortList) {
        try {
            System.out.println("========== 收到排序更新请求 ==========");
            System.out.println("接收到的数据: " + sortList);

            if (sortList == null || sortList.isEmpty()) {
                return Result.error("排序数据不能为空");
            }

            boolean success = carouselService.batchUpdateSort(sortList);
            if (success) {
                return Result.success("排序更新成功");
            } else {
                return Result.error("排序更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}