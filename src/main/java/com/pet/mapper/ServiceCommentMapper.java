package com.pet.mapper;

import com.pet.entity.ServiceComment;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface ServiceCommentMapper {
    int insert(ServiceComment comment);

    List<ServiceComment> getByServiceId(@Param("serviceId") Integer serviceId,
                                        @Param("offset") Integer offset,
                                        @Param("limit") Integer limit);

    int countByServiceId(@Param("serviceId") Integer serviceId);

    ServiceComment getByAppointmentId(@Param("appointmentId") Integer appointmentId);

    int updateReply(@Param("id") Integer id, @Param("reply") String reply);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    int deleteById(@Param("id") Integer id);

    Map<String, Object> getRatingStats(@Param("serviceId") Integer serviceId);

    List<ServiceComment> getAdminList(@Param("offset") Integer offset,
                                      @Param("limit") Integer limit,
                                      @Param("keyword") String keyword,
                                      @Param("rating") Integer rating,
                                      @Param("status") Integer status,
                                      @Param("replyStatus") Integer replyStatus);

    int countAdminList(@Param("keyword") String keyword,
                       @Param("rating") Integer rating,
                       @Param("status") Integer status,
                       @Param("replyStatus") Integer replyStatus);

    // 获取服务评分分布
    List<Map<String, Object>> getServiceRatingDistribution(@Param("serviceId") Integer serviceId);

    // 获取服务近7天评价趋势
    List<Map<String, Object>> getServiceCommentTrend(@Param("serviceId") Integer serviceId);

    // 获取服务已回复评价数量
    int getServiceRepliedCommentCount(@Param("serviceId") Integer serviceId);

    // 获取服务评价列表（支持评分筛选）
    List<ServiceComment> getServiceCommentListWithRating(@Param("serviceId") Integer serviceId,
                                                         @Param("offset") Integer offset,
                                                         @Param("limit") Integer limit,
                                                         @Param("rating") Integer rating);

    // 统计服务评价数量（支持评分筛选）
    int countServiceCommentWithRating(@Param("serviceId") Integer serviceId,
                                      @Param("rating") Integer rating);

    // 获取评价统计
    Map<String, Object> getStatistics();

    // 获取员工自己的评价统计
    Map<String, Object> getStatisticsByStaff(@Param("staffId") Integer staffId);

    /**
     * 获取用户的所有服务评价
     */
    List<ServiceComment> getUserServiceComments(@Param("userId") Integer userId,
                                                @Param("offset") Integer offset,
                                                @Param("limit") Integer limit);

    /**
     * 统计用户服务评价数量
     */
    int countUserServiceComments(@Param("userId") Integer userId);

}