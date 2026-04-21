package com.pet.service.impl;

import com.pet.entity.Carousel;
import com.pet.mapper.CarouselMapper;
import com.pet.service.CarouselService;
import com.pet.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarouselServiceImpl implements CarouselService {

    @Autowired
    private CarouselMapper carouselMapper;

    @Override
    public List<Carousel> getHomeCarousels() {
        return carouselMapper.getEnabledCarousels();
    }

    @Override
    public Map<String, Object> getCarouselList(Integer page, Integer pageSize, String keyword, Integer status) {
        int offset = (page - 1) * pageSize;
        List<Carousel> list = carouselMapper.getCarouselList(offset, pageSize, keyword, status);
        int total = carouselMapper.countCarousel(keyword, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Carousel getById(Integer id) {
        return carouselMapper.getById(id);
    }

    @Override
    public boolean addCarousel(Carousel carousel) {
        // 检查轮播图数量限制
        int limit = FileUploadUtil.getCarouselLimit();
        int currentCount = carouselMapper.countCarousel(null, 1);
        if (currentCount >= limit) {
            throw new RuntimeException("轮播图数量已达上限（" + limit + "张），无法继续添加");
        }
        return carouselMapper.insert(carousel) > 0;
    }

    @Override
    public boolean updateCarousel(Carousel carousel) {
        return carouselMapper.update(carousel) > 0;
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        // 如果要启用轮播图，检查数量限制
        if (status == 1) {
            int limit = FileUploadUtil.getCarouselLimit();
            int currentCount = carouselMapper.countCarousel(null, 1);
            if (currentCount >= limit) {
                throw new RuntimeException("轮播图数量已达上限（" + limit + "张），无法启用");
            }
        }
        return carouselMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean batchUpdateStatus(List<Integer> ids, Integer status) {
        // 如果要批量启用，检查数量限制
        if (status == 1) {
            int limit = FileUploadUtil.getCarouselLimit();
            int currentCount = carouselMapper.countCarousel(null, 1);
            if (currentCount + ids.size() > limit) {
                throw new RuntimeException("启用后轮播图数量将超过上限（" + limit + "张）");
            }
        }
        return carouselMapper.batchUpdateStatus(ids, status) > 0;
    }

    @Override
    public boolean deleteCarousel(Integer id) {
        return carouselMapper.deleteById(id) > 0;
    }

    @Override
    public boolean batchDelete(List<Integer> ids) {
        return carouselMapper.batchDelete(ids) > 0;
    }

    @Override
    @Transactional
    public boolean batchUpdateSort(List<Map<String, Integer>> sortList) {
        if (sortList == null || sortList.isEmpty()) {
            return false;
        }

        int successCount = 0;
        for (Map<String, Integer> item : sortList) {
            Integer id = item.get("id");
            Integer sortOrder = item.get("sortOrder");
            if (id != null && sortOrder != null) {
                Carousel carousel = carouselMapper.getById(id);
                if (carousel != null) {
                    carousel.setSortOrder(sortOrder);
                    int result = carouselMapper.update(carousel);
                    if (result > 0) {
                        successCount++;
                    }
                }
            }
        }

        return successCount > 0;
    }
}