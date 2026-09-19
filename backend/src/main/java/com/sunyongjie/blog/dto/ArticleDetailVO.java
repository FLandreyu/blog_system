package com.sunyongjie.blog.dto;

import com.sunyongjie.blog.entity.Tag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章详情，比列表项多了正文、标签和评论数。
 */
@Data
public class ArticleDetailVO {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String cover;

    private Long categoryId;
    private String categoryName;

    private Long authorId;
    private String authorName;
    private String authorAvatar;

    private Integer status;
    private Integer viewCount;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 标签在 Service 层单独查一次再塞进来，避免写复杂的嵌套 resultMap */
    private List<Tag> tags;

    private Integer commentCount;
}
