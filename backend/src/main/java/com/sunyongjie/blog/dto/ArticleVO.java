package com.sunyongjie.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章列表项。
 *
 * <p>列表不返回正文（longtext），避免一次查询把大字段全捞出来。
 */
@Data
public class ArticleVO {

    private Long id;
    private String title;
    private String summary;
    private String cover;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;
}
