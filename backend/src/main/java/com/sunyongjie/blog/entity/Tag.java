package com.sunyongjie.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签，对应表 tag。
 *
 * <p>文章与标签是多对多，关联关系放在 article_tag 表。
 */
@Data
public class Tag {

    private Long id;
    private String name;
    private LocalDateTime createTime;
}
