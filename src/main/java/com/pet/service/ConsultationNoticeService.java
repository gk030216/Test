// ConsultationNoticeService.java
package com.pet.service;

import com.pet.entity.ConsultationNotice;
import java.util.List;
import java.util.Map;

public interface ConsultationNoticeService {

    /**
     * 添加公告
     */
    boolean addNotice(ConsultationNotice notice);

    /**
     * 删除公告
     */
    boolean deleteNotice(Integer id);

    /**
     * 更新公告
     */
    boolean updateNotice(ConsultationNotice notice);

    /**
     * 获取公告详情
     */
    ConsultationNotice getNoticeDetail(Integer id);

    /**
     * 获取所有公告
     */
    List<ConsultationNotice> getAllNotices();

    /**
     * 分页获取公告
     */
    Map<String, Object> getNoticesByPage(int pageNum, int pageSize);

    /**
     * 获取首页展示的公告（最多5条）
     */
    List<ConsultationNotice> getHomeNotices();

    /**
     * 获取置顶公告
     */
    List<ConsultationNotice> getTopNotices();

    /**
     * 根据类型获取公告
     */
    List<ConsultationNotice> getNoticesByType(Integer type);

    /**
     * 条件分页查询
     */
    Map<String, Object> getNoticesByCondition(int pageNum, int pageSize, Integer type, Integer status, String keyword);
}