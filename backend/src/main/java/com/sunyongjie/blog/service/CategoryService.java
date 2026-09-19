package com.sunyongjie.blog.service;

import com.sunyongjie.blog.dto.CategoryRequest;
import com.sunyongjie.blog.dto.CategoryVO;

import java.util.List;

/**
 * 分类相关业务。
 */
public interface CategoryService {

    List<CategoryVO> list();

    CategoryVO create(CategoryRequest request);

    void update(Long id, CategoryRequest request);

    void delete(Long id);
}
