package com.sunyongjie.blog.mapper;

import com.sunyongjie.blog.dto.CategoryVO;
import com.sunyongjie.blog.entity.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类表操作。
 */
public interface CategoryMapper {

    /** 带文章数的分类列表（后台管理用） */
    List<CategoryVO> selectAllWithCount();

    Category selectById(@Param("id") Long id);

    /** 同名校验；excludeId 用于"改名时排除自己" */
    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    int insert(Category category);

    int update(Category category);

    int deleteById(@Param("id") Long id);
}
