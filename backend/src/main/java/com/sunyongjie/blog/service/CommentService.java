package com.sunyongjie.blog.service;

import com.sunyongjie.blog.dto.CommentRequest;
import com.sunyongjie.blog.dto.CommentVO;

import java.util.List;

/**
 * 评论相关业务。
 */
public interface CommentService {

    /** 某篇文章的评论，两级结构 */
    List<CommentVO> listByArticle(Long articleId);

    Long create(CommentRequest request);

    void delete(Long id);
}
