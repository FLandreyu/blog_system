package com.sunyongjie.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论列表项。
 *
 * <p>顶层评论的 {@code replies} 里挂着它的回复；回复自身的 {@code replies} 恒为空，
 * 因为只做两级。
 */
@Data
public class CommentVO {

    private Long id;
    private Long articleId;
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private Long parentId;
    private LocalDateTime createTime;

    /** 该评论下的回复；回复自身的这个字段恒为空列表 */
    private List<CommentVO> replies = new ArrayList<>();
}
