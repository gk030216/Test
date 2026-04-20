package com.pet.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AIFaqMapper {

    /**
     * 获取启用的常见问题列表
     */
    List<Map<String, Object>> getEnabledList();

    /**
     * 获取所有常见问题列表（后台）
     */
    List<Map<String, Object>> getList(@Param("offset") Integer offset,
                                      @Param("limit") Integer limit,
                                      @Param("keyword") String keyword,
                                      @Param("category") String category,
                                      @Param("status") Integer status);

    /**
     * 获取总数
     */
    int countList(@Param("keyword") String keyword,
                  @Param("category") String category,
                  @Param("status") Integer status);

    /**
     * 插入常见问题
     */
    int insert(Map<String, Object> faq);

    /**
     * 更新常见问题
     */
    int update(Map<String, Object> faq);

    /**
     * 删除常见问题
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 根据问题查询常见问题（检查是否已存在）
     */
    Map<String, Object> findByQuestion(@Param("question") String question);
}