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
                                      @Param("status") Integer status);

    int countAdminList(@Param("keyword") String keyword,
                       @Param("rating") Integer rating,
                       @Param("status") Integer status);

}