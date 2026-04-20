package com.pet.controller.admin;

import com.pet.entity.ConsultationNotice;
import com.pet.service.ConsultationNoticeService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/consultation-notice")
public class AdminConsultationNoticeController {

    @Autowired
    private ConsultationNoticeService noticeService;

    /**
     * 分页查询公告（管理员）
     */
    @GetMapping("/page")
    public Result getNoticePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = noticeService.getNoticesByCondition(pageNum, pageSize, type, status, title);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取公告列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有公告列表（管理员）
     */
    @GetMapping("/list")
    public Result getAllNotices() {
        try {
            List<ConsultationNotice> list = noticeService.getAllNotices();
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取公告列表失败：" + e.getMessage());
        }
    }

    /**
     * 添加公告（管理员）
     */
    @PostMapping("/add")
    public Result addNotice(@RequestBody ConsultationNotice notice) {
        try {
            // 设置默认值
            if (notice.getViewCount() == null) {
                notice.setViewCount(0);
            }
            if (notice.getIsTop() == null) {
                notice.setIsTop(0);
            }
            if (notice.getSortOrder() == null) {
                notice.setSortOrder(0);
            }
            if (notice.getStatus() == null) {
                notice.setStatus(1);
            }

            boolean result = noticeService.addNotice(notice);
            if (result) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新公告（管理员）
     */
    @PutMapping("/update")
    public Result updateNotice(@RequestBody ConsultationNotice notice) {
        try {
            boolean result = noticeService.updateNotice(notice);
            if (result) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除公告（管理员）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteNotice(@PathVariable Integer id) {
        try {
            boolean result = noticeService.deleteNotice(id);
            if (result) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除公告（管理员）
     */
    @DeleteMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Integer> ids) {
        try {
            int successCount = 0;
            for (Integer id : ids) {
                if (noticeService.deleteNotice(id)) {
                    successCount++;
                }
            }
            return Result.success("成功删除 " + successCount + " 条公告");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 切换置顶状态（管理员）
     */
    @PutMapping("/toggleTop/{id}")
    public Result toggleTop(@PathVariable Integer id, @RequestParam Integer isTop) {
        try {
            ConsultationNotice notice = new ConsultationNotice();
            notice.setId(id);
            notice.setIsTop(isTop);
            boolean result = noticeService.updateNotice(notice);
            if (result) {
                return Result.success(isTop == 1 ? "已置顶" : "已取消置顶");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 切换状态（管理员）
     */
    @PutMapping("/toggleStatus/{id}")
    public Result toggleStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            ConsultationNotice notice = new ConsultationNotice();
            notice.setId(id);
            notice.setStatus(status);
            boolean result = noticeService.updateNotice(notice);
            if (result) {
                return Result.success(status == 1 ? "已启用" : "已禁用");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 更新排序（管理员）
     */
    @PutMapping("/updateSort/{id}")
    public Result updateSort(@PathVariable Integer id, @RequestParam Integer sortOrder) {
        try {
            ConsultationNotice notice = new ConsultationNotice();
            notice.setId(id);
            notice.setSortOrder(sortOrder);
            boolean result = noticeService.updateNotice(notice);
            if (result) {
                return Result.success("排序已更新");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 获取公告详情（管理员）
     */
    @GetMapping("/detail/{id}")
    public Result getNoticeDetail(@PathVariable Integer id) {
        try {
            ConsultationNotice notice = noticeService.getNoticeDetail(id);
            if (notice == null) {
                return Result.error("公告不存在");
            }
            return Result.success(notice);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取公告详情失败：" + e.getMessage());
        }
    }
}