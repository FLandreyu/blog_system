package com.sunyongjie.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类，对应表 category。
 */
@Data
public class Category {

    private Long id;
    private String name;
    /** 排序值，越小越靠前 */
    private Integer sort;
    private LocalDateTime createTime;
}
