package com.sunyongjie.blog.service.impl;

import com.sunyongjie.blog.common.BizException;
import com.sunyongjie.blog.common.CurrentUser;
import com.sunyongjie.blog.common.ResultCode;
import com.sunyongjie.blog.common.UserContext;
import com.sunyongjie.blog.dto.CommentRequest;
import com.sunyongjie.blog.dto.CommentVO;
import com.sunyongjie.blog.entity.Article;
import com.sunyongjie.blog.entity.Comment;
import com.sunyongjie.blog.mapper.ArticleMapper;
import com.sunyongjie.blog.mapper.CommentMapper;
import com.sunyongjie.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论业务实现。
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;

    @Override
    public List<CommentVO> listByArticle(Long articleId) {
        // 一次查出全部（含回复），再在内存里组装成两级，避免按父评论逐条查子评论的 N+1
        List<CommentVO> all = commentMapper.selectByArticleId(articleId);

        Map<Long, CommentVO> topLevel = new LinkedHashMap<>();
        for (CommentVO vo : all) {
            if (vo.getParentId() == null) {
                topLevel.put(vo.getId(), vo);
            }
        }
        for (CommentVO vo : all) {
            if (vo.getParentId() != null) {
                CommentVO parent = topLevel.get(vo.getParentId());
                if (parent != null) {
                    parent.getReplies().add(vo);
                }
            }
        }
        return new ArrayList<>(topLevel.values());
    }

    @Override
    @Transactional
    public Long create(CommentRequest request) {
        CurrentUser current = UserContext.require();

        Article article = articleMapper.selectById(request.getArticleId());
        if (article == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }

        Long parentId = request.getParentId();
        if (parentId != null) {
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null || !parent.getArticleId().equals(request.getArticleId())) {
                throw new BizException(ResultCode.BAD_REQUEST, "要回复的评论不存在");
            }
            // 只做两级：如果回复的是一条回复，就把它挂到那条回复的顶层父评论上，
            // 这样前端永远只需渲染两层，不用处理无限嵌套。
            if (parent.getParentId() != null) {
                parentId = parent.getParentId();
            }
        }

        Comment comment = new Comment();
        comment.setArticleId(request.getArticleId());
        comment.setUserId(current.id());
        comment.setContent(request.getContent());
        comment.setParentId(parentId);
        commentMapper.insert(comment);

        return comment.getId();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BizException(ResultCode.NOT_FOUND, "评论不存在");
        }

        // 评论作者本人，或者文章作者，都可以删
        CurrentUser current = UserContext.require();
        if (!current.isOwner(comment.getUserId()) && !current.isOwner(articleAuthorId(comment.getArticleId()))) {
            throw new BizException(ResultCode.FORBIDDEN, "无权删除该评论");
        }

        // 删顶层评论时连它的回复一起删，否则回复会挂在已删除的 parent_id 上变成孤儿
        commentMapper.deleteByParentId(id);
        commentMapper.deleteById(id);
    }

    private Long articleAuthorId(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        return article == null ? null : article.getAuthorId();
    }
}
