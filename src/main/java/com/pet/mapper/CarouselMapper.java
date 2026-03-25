package com.pet.mapper;

import com.pet.entity.Carousel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarouselMapper {

    /**
     * 获取所有启用的轮播图（按排序顺序）
     */
    List<Carousel> getEnabledCarousels();

    /**
     * 获取轮播图列表（分页）
     */
    List<Carousel> getCarouselList(@Param("offset") Integer offset,
                                   @Param("limit") Integer limit,
                                   @Param("keyword") String keyword,
                                   @Param("status") Integer status);

    /**
     * 获取轮播图总数
     */
    int countCarousel(@Param("keyword") String keyword, @Param("status") Integer status);

    /**
     * 根据ID获取轮播图
     */
    Carousel getById(@Param("id") Integer id);

    /**
     * 添加轮播图
     */
    int insert(Carousel carousel);

    /**
     * 更新轮播图
     */
    int update(Carousel carousel);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 批量更新状态
     */
    int batchUpdateStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status);

    /**
     * 删除轮播图
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 批量删除
     */
    int batchDelete(@Param("ids") List<Integer> ids);
}