package com.sunyongjie.blog.dto;

import lombok.Data;

/**
 * 分类列表项。
 *
 * <p>带上 {@code articleCount}，后台管理页可以直接显示"该分类下有 3 篇文章"，
 * 删除被引用的分类时也能给出明确提示。
 */
@Data
public class CategoryVO {

    private Long id;
    private String name;
    private Integer sort;
    private Integer articleCount;
}
