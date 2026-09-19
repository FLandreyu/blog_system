package com.sunyongjie.blog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发布 / 编辑文章的请求体。
 */
@Data
public class ArticleRequest {

    @NotBlank(message = "不能为空")
    @Size(max = 200, message = "长度不能超过 200")
    private String title;

    @Size(max = 500, message = "长度不能超过 500")
    private String summary;

    @NotBlank(message = "不能为空")
    private String content;

    @Size(max = 255, message = "长度不能超过 255")
    private String cover;

    private Long categoryId;

    /** 标签 id 列表，可为空 */
    private List<Long> tagIds;

    /** 0=草稿 1=已发布，不传按草稿处理 */
    @Min(value = 0, message = "只能是 0（草稿）或 1（已发布）")
    @Max(value = 1, message = "只能是 0（草稿）或 1（已发布）")
    private Integer status;
}
