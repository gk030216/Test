// ConsultationNoticeMapper.java
package com.pet.mapper;

import com.pet.entity.ConsultationNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ConsultationNoticeMapper {

    /**
     * 插入公告
     */
    int insert(ConsultationNotice notice);

    /**
     * 根据ID删除公告
     */
    int deleteById(Integer id);

    /**
     * 更新公告
     */
    int updateById(ConsultationNotice notice);

    /**
     * 根据ID查询公告
     */
    ConsultationNotice selectById(Integer id);

    /**
     * 查询所有公告
     */
    List<ConsultationNotice> selectAll();

    /**
     * 分页查询公告
     */
    List<ConsultationNotice> selectPage(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询公告总数
     */
    int selectCount();

    /**
     * 获取启用的公告（按置顶、排序、时间排序）
     */
    List<ConsultationNotice> selectActiveNotices(@Param("limit") int limit);

    /**
     * 获取置顶公告
     */
    List<ConsultationNotice> selectTopNotices();

    /**
     * 增加浏览次数
     */
    int incrementViewCount(Integer id);

    /**
     * 根据类型获取公告
     */
    List<ConsultationNotice> selectByType(@Param("type") Integer type);

    /**
     * 条件分页查询
     */
    List<ConsultationNotice> selectPageByCondition(@Param("offset") int offset,
                                                   @Param("limit") int limit,
                                                   @Param("type") Integer type,
                                                   @Param("status") Integer status,
                                                   @Param("keyword") String keyword);

    /**
     * 条件查询总数
     */
    int selectCountByCondition(@Param("type") Integer type,
                               @Param("status") Integer status,
                               @Param("keyword") String keyword);

}