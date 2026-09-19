package com.sunyongjie.blog.mapper;

import com.sunyongjie.blog.dto.CommentVO;
import com.sunyongjie.blog.entity.Comment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论表操作。
 */
public interface CommentMapper {

    /** 某篇文章的全部评论，按时间正序；两级结构在 Service 层组装 */
    List<CommentVO> selectByArticleId(@Param("articleId") Long articleId);

    Comment selectById(@Param("id") Long id);

    int insert(Comment comment);

    int deleteById(@Param("id") Long id);

    /** 删顶层评论时一并删掉它的回复，避免留下挂空的 parent_id */
    int deleteByParentId(@Param("parentId") Long parentId);

    /** 删文章时一并清掉它的评论 */
    int deleteByArticleId(@Param("articleId") Long articleId);

    int countByArticleId(@Param("articleId") Long articleId);
}
