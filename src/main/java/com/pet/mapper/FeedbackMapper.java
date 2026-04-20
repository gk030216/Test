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

    // 员工获取反馈列表（待处理 + 自己处理的）
    List<Feedback> getStaffList(@Param("offset") Integer offset,
                                @Param("limit") Integer limit,
                                @Param("keyword") String keyword,
                                @Param("type") String type,
                                @Param("status") Integer status,
                                @Param("handlerId") Integer handlerId);

    // 员工获取反馈总数
    int countStaffList(@Param("keyword") String keyword,
                       @Param("type") String type,
                       @Param("status") Integer status,
                       @Param("handlerId") Integer handlerId);

    // 根据状态和处理人统计
    int countByStatusAndHandler(@Param("status") Integer status, @Param("handlerId") Integer handlerId);
}