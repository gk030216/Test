package com.pet.service;

import com.pet.entity.Carousel;

import java.util.List;
import java.util.Map;

public interface CarouselService {

    /**
     * 获取首页轮播图（启用的）
     */
    List<Carousel> getHomeCarousels();

    /**
     * 获取轮播图列表（分页）
     */
    Map<String, Object> getCarouselList(Integer page, Integer pageSize, String keyword, Integer status);

    /**
     * 根据ID获取
     */
    Carousel getById(Integer id);

    /**
     * 添加轮播图
     */
    boolean addCarousel(Carousel carousel);

    /**
     * 更新轮播图
     */
    boolean updateCarousel(Carousel carousel);

    /**
     * 更新状态
     */
    boolean updateStatus(Integer id, Integer status);

    /**
     * 批量更新状态
     */
    boolean batchUpdateStatus(List<Integer> ids, Integer status);

    /**
     * 删除
     */
    boolean deleteCarousel(Integer id);

    /**
     * 批量删除
     */
    boolean batchDelete(List<Integer> ids);

    /**
     * 批量更新排序
     */
    boolean batchUpdateSort(List<Map<String, Integer>> sortList);
}