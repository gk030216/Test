package com.pet.mapper;

import com.pet.entity.ServiceFavorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ServiceFavoriteMapper {

    /**
     * 添加收藏
     */
    int insert(@Param("userId") Integer userId, @Param("serviceId") Integer serviceId);

    /**
     * 取消收藏
     */
    int delete(@Param("userId") Integer userId, @Param("serviceId") Integer serviceId);

    /**
     * 检查是否已收藏
     */
    int checkFavorite(@Param("userId") Integer userId, @Param("serviceId") Integer serviceId);

    /**
     * 获取用户收藏的服务列表
     */
    List<ServiceFavorite> getUserFavorites(@Param("userId") Integer userId,
                                           @Param("offset") Integer offset,
                                           @Param("limit") Integer limit);

    /**
     * 统计用户收藏数量
     */
    int countUserFavorites(@Param("userId") Integer userId);
}