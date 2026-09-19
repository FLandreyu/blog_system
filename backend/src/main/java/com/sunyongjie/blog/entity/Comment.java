package com.sunyongjie.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论，对应表 comment。
 *
 * <p>{@code parentId} 为 NULL 表示顶层评论，否则表示它是某条评论的回复。
 * 只做两级，不做无限嵌套。
 */
@Data
public class Comment {

    private Long id;
    private Long articleId;
    private Long userId;
    private String content;
    /** 父评论 id，顶层为 null */
    private Long parentId;
    private LocalDateTime createTime;
}
