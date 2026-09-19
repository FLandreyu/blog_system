package com.sunyongjie.blog.dto;

import com.sunyongjie.blog.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章列表查询条件，对应 {@code GET /api/articles} 的 query string。
 *
 * <p>每个字段都可能为 null，Mapper 里用 {@code <if>} 判断后再决定是否拼进 WHERE。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleQuery extends PageQuery {

    /** 分类筛选，精确匹配 */
    private Long categoryId;

    /** 标题关键词，模糊匹配 */
    private String keyword;

    /** 作者筛选，"我的文章" 用 */
    private Long authorId;

    /** 状态筛选：公开列表强制为 1，后台可为 null（看全部） */
    private Integer status;
}
