package com.pet.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ChatHistoryMapper {

    // 用户端方法
    int insert(Map<String, Object> history);
    List<Map<String, Object>> getByUserId(@Param("userId") Integer userId,
                                          @Param("offset") Integer offset,
                                          @Param("limit") Integer limit);
    int countByUserId(@Param("userId") Integer userId);
    int updateRating(@Param("id") Integer id, @Param("rating") Integer rating);
    int deleteByUserId(@Param("userId") Integer userId);

    // 后台管理方法
    List<Map<String, Object>> getAdminList(@Param("offset") Integer offset,
                                           @Param("limit") Integer limit,
                                           @Param("keyword") String keyword,
                                           @Param("answerSource") Integer answerSource,
                                           @Param("rating") Integer rating);
    int countAdminList(@Param("keyword") String keyword,
                       @Param("answerSource") Integer answerSource,
                       @Param("rating") Integer rating);
    int deleteById(@Param("id") Integer id);
}