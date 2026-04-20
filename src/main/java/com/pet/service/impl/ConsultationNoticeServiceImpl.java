// ConsultationNoticeServiceImpl.java
package com.pet.service.impl;

import com.pet.entity.ConsultationNotice;
import com.pet.mapper.ConsultationNoticeMapper;
import com.pet.service.ConsultationNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsultationNoticeServiceImpl implements ConsultationNoticeService {

    @Autowired
    private ConsultationNoticeMapper noticeMapper;

    @Override
    public boolean addNotice(ConsultationNotice notice) {
        if (notice.getViewCount() == null) {
            notice.setViewCount(0);
        }
        if (notice.getIsTop() == null) {
            notice.setIsTop(0);
        }
        if (notice.getSortOrder() == null) {
            notice.setSortOrder(0);
        }
        return noticeMapper.insert(notice) > 0;
    }

    @Override
    public boolean deleteNotice(Integer id) {
        return noticeMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateNotice(ConsultationNotice notice) {
        return noticeMapper.updateById(notice) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsultationNotice getNoticeDetail(Integer id) {
        // 增加浏览次数
        noticeMapper.incrementViewCount(id);
        // 获取公告详情
        return noticeMapper.selectById(id);
    }

    @Override
    public List<ConsultationNotice> getAllNotices() {
        return noticeMapper.selectAll();
    }

    @Override
    public Map<String, Object> getNoticesByPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<ConsultationNotice> list = noticeMapper.selectPage(offset, pageSize);
        int total = noticeMapper.selectCount();

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public List<ConsultationNotice> getHomeNotices() {
        return noticeMapper.selectActiveNotices(5);
    }

    @Override
    public List<ConsultationNotice> getTopNotices() {
        return noticeMapper.selectTopNotices();
    }

    @Override
    public List<ConsultationNotice> getNoticesByType(Integer type) {
        return noticeMapper.selectByType(type);
    }

    @Override
    public Map<String, Object> getNoticesByCondition(int pageNum, int pageSize, Integer type, Integer status, String keyword) {
        int offset = (pageNum - 1) * pageSize;
        List<ConsultationNotice> list = noticeMapper.selectPageByCondition(offset, pageSize, type, status, keyword);
        int total = noticeMapper.selectCountByCondition(type, status, keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }
}