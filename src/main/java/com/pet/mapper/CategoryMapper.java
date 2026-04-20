package com.pet.mapper;

import com.pet.entity.Category;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CategoryMapper {

    /**
     * 获取分类列表（分页）
     */
    List<Category> getCategoryList(@Param("offset") Integer offset,
                                   @Param("limit") Integer limit,
                                   @Param("keyword") String keyword,
                                   @Param("status") Integer status,
                                   @Param("parentId") Integer parentId);

    /**
     * 获取分类总数
     */
    int countCategory(@Param("keyword") String keyword,
                      @Param("status") Integer status,
                      @Param("parentId") Integer parentId);

    /**
     * 获取所有启用的分类
     */
    List<Category> getAllEnabledCategories();

    /**
     * 根据ID获取分类
     */
    Category getById(@Param("id") Integer id);

    /**
     * 检查子分类是否存在
     */
    int countChildren(@Param("parentId") Integer parentId);

    /**
     * 添加分类
     */
    int insert(Category category);

    /**
     * 更新分类
     */
    int update(Category category);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 更新排序
     */
    int updateSortOrder(@Param("id") Integer id, @Param("sortOrder") Integer sortOrder);

    /**
     * 删除分类
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 批量删除
     */
    int batchDelete(@Param("ids") List<Integer> ids);
}