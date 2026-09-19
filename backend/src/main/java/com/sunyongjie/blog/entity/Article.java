package com.sunyongjie.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章，对应表 article。
 */
@Data
public class Article {

    /** 草稿 */
    public static final int STATUS_DRAFT = 0;
    /** 已发布 */
    public static final int STATUS_PUBLISHED = 1;

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String cover;
    private Long categoryId;
    private Long authorId;
    /** 0=草稿 1=已发布 */
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
