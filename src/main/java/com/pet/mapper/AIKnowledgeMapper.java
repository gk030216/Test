package com.pet.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AIKnowledgeMapper {

    Map<String, Object> searchBestMatch(@Param("question") String question);

    List<Map<String, Object>> searchByKeyword(@Param("keyword") String keyword, @Param("limit") Integer limit);

    List<Map<String, Object>> getList(@Param("offset") Integer offset,
                                      @Param("limit") Integer limit,
                                      @Param("keyword") String keyword,
                                      @Param("category") String category,
                                      @Param("source") Integer source,
                                      @Param("status") Integer status);

    int countList(@Param("keyword") String keyword,
                  @Param("category") String category,
                  @Param("source") Integer source,
                  @Param("status") Integer status);

    int insert(Map<String, Object> knowledge);

    int update(Map<String, Object> knowledge);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    int deleteById(@Param("id") Integer id);

    int incrementUseCount(@Param("id") Integer id);

    Map<String, Object> getById(@Param("id") Integer id);
}