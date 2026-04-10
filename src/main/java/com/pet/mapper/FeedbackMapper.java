package com.pet.mapper;

import com.pet.entity.Feedback;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface FeedbackMapper {
    int insert(Feedback feedback);
    int update(Feedback feedback);
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status,
                     @Param("handlerId") Integer handlerId, @Param("handlerName") String handlerName);
    int resolve(@Param("id") Integer id, @Param("processResult") String processResult);
    Feedback getById(@Param("id") Integer id);
    List<Feedback> getList(@Param("offset") Integer offset, @Param("limit") Integer limit,
                           @Param("keyword") String keyword, @Param("type") String type,
                           @Param("status") Integer status);
    int countList(@Param("keyword") String keyword, @Param("type") String type,
                  @Param("status") Integer status);
    int countToday();
    int countByStatus(@Param("status") Integer status);
    List<Feedback> getUserFeedbacks(@Param("userId") Integer userId, @Param("offset") Integer offset,
                                    @Param("limit") Integer limit);
}