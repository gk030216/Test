// ConsultationNoticeController.java
package com.pet.controller;

import com.pet.entity.ConsultationNotice;
import com.pet.service.ConsultationNoticeService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultation-notice")
public class ConsultationNoticeController {

    @Autowired
    private ConsultationNoticeService noticeService;

    /**
     * 获取首页展示的公告（最多5条）
     */
    @GetMapping("/home")
    public Result getHomeNotices() {
        try {
            List<ConsultationNotice> notices = noticeService.getHomeNotices();
            return Result.success(notices);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取公告失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有启用的公告列表
     */
    @GetMapping("/list")
    public Result getNoticeList() {
        try {
            List<ConsultationNotice> notices = noticeService.getAllNotices();
            return Result.success(notices);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取公告列表失败：" + e.getMessage());
        }
    }

    /**
     * 分页获取公告列表
     */
    @GetMapping("/page")
    public Result getNoticePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        try {
            Map<String, Object> result = noticeService.getNoticesByCondition(pageNum, pageSize, type, status, keyword);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取公告列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取置顶公告
     */
    @GetMapping("/top")
    public Result getTopNotices() {
        try {
            List<ConsultationNotice> notices = noticeService.getTopNotices();
            return Result.success(notices);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取置顶公告失败：" + e.getMessage());
        }
    }

    /**
     * 根据类型获取公告
     */
    @GetMapping("/type/{type}")
    public Result getNoticesByType(@PathVariable Integer type) {
        try {
            List<ConsultationNotice> notices = noticeService.getNoticesByType(type);
            return Result.success(notices);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取公告失败：" + e.getMessage());
        }
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
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

    /**
     * 添加公告（管理员）
     */
    @PostMapping("/add")
    public Result addNotice(@RequestBody ConsultationNotice notice) {
        try {
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
}