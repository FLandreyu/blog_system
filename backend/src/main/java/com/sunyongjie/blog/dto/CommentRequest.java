package com.sunyongjie.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论 / 回复。
 */
@Data
public class CommentRequest {

    @NotNull(message = "不能为空")
    private Long articleId;

    @NotBlank(message = "不能为空")
    @Size(max = 1000, message = "长度不能超过 1000")
    private String content;

    /** 回复某条评论时传父评论 id；顶层评论留空 */
    private Long parentId;
}
