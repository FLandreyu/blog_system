package com.sunyongjie.blog.service;

import com.sunyongjie.blog.common.PageResult;
import com.sunyongjie.blog.dto.ArticleDetailVO;
import com.sunyongjie.blog.dto.ArticleQuery;
import com.sunyongjie.blog.dto.ArticleRequest;
import com.sunyongjie.blog.dto.ArticleVO;

/**
 * 文章相关业务。
 */
public interface ArticleService {

    /** 公开列表，只返回已发布文章 */
    PageResult<ArticleVO> page(ArticleQuery query);

    /** 我的文章，含草稿 */
    PageResult<ArticleVO> mine(ArticleQuery query);

    /** 详情，浏览量 +1；草稿仅作者本人可见 */
    ArticleDetailVO detail(Long id);

    Long create(ArticleRequest request);

    void update(Long id, ArticleRequest request);

    void delete(Long id);
}
