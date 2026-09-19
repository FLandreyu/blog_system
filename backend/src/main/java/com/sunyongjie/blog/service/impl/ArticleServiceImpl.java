package com.sunyongjie.blog.service.impl;

import com.sunyongjie.blog.common.BizException;
import com.sunyongjie.blog.common.CurrentUser;
import com.sunyongjie.blog.common.PageResult;
import com.sunyongjie.blog.common.ResultCode;
import com.sunyongjie.blog.common.UserContext;
import com.sunyongjie.blog.dto.ArticleDetailVO;
import com.sunyongjie.blog.dto.ArticleQuery;
import com.sunyongjie.blog.dto.ArticleRequest;
import com.sunyongjie.blog.dto.ArticleVO;
import com.sunyongjie.blog.entity.Article;
import com.sunyongjie.blog.entity.Tag;
import com.sunyongjie.blog.mapper.ArticleMapper;
import com.sunyongjie.blog.mapper.CommentMapper;
import com.sunyongjie.blog.mapper.TagMapper;
import com.sunyongjie.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章业务实现。
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final TagMapper tagMapper;
    private final CommentMapper commentMapper;

    @Override
    public PageResult<ArticleVO> page(ArticleQuery query) {
        // 强制只看已发布：否则前端只要传 status=0 就能把别人的草稿捞出来
        query.setStatus(Article.STATUS_PUBLISHED);
        return doPage(query);
    }

    @Override
    public PageResult<ArticleVO> mine(ArticleQuery query) {
        query.setAuthorId(UserContext.requireUserId());
        // status 留空表示"全部（含草稿）"，由前端自己选
        return doPage(query);
    }

    @Override
    public ArticleDetailVO detail(Long id) {
        ArticleDetailVO detail = articleMapper.selectDetailById(id);
        if (detail == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }

        // 草稿只对作者本人可见；对其他人返回 404 而不是 403，
        // 避免"403 说明这篇草稿确实存在"这种信息泄露。
        if (detail.getStatus() != null && detail.getStatus() == Article.STATUS_DRAFT
                && !isCurrentAuthor(detail.getAuthorId())) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }

        // 自增走 SQL，避免"读出来 +1 再写回去"在并发下丢更新。
        // 这里同步把返回值 +1，让响应里的浏览量和刚落库的值一致。
        articleMapper.incrementViewCount(id);
        detail.setViewCount(detail.getViewCount() == null ? 1 : detail.getViewCount() + 1);
        detail.setTags(tagMapper.selectByArticleId(id));
        return detail;
    }

    @Override
    @Transactional
    public Long create(ArticleRequest request) {
        CurrentUser current = UserContext.require();

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setCover(request.getCover());
        article.setCategoryId(request.getCategoryId());
        article.setAuthorId(current.id());
        article.setStatus(request.getStatus() == null ? Article.STATUS_DRAFT : request.getStatus());

        articleMapper.insert(article);
        saveTags(article.getId(), request.getTagIds());
        return article.getId();
    }

    @Override
    @Transactional
    public void update(Long id, ArticleRequest request) {
        Article existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }
        if (!UserContext.require().isOwner(existing.getAuthorId())) {
            throw new BizException(ResultCode.FORBIDDEN, "只能修改自己发布的文章");
        }

        Article article = new Article();
        article.setId(id);
        article.setTitle(request.getTitle());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setCover(request.getCover());
        article.setCategoryId(request.getCategoryId());
        // 不传状态时保持原状态，避免"编辑一下草稿就被自动发布了"
        article.setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
        articleMapper.update(article);

        // 标签按"整组替换"处理：先清空再写。
        // 比逐条 diff 简单，且不会因为漏删而残留旧标签。
        tagMapper.deleteArticleTags(id);
        saveTags(id, request.getTagIds());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Article existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }
        if (!UserContext.require().isOwner(existing.getAuthorId())) {
            throw new BizException(ResultCode.FORBIDDEN, "只能删除自己发布的文章");
        }

        // 物理删除，并在同一个事务里清掉评论和标签关联，不留孤儿数据。
        // （选物理删除的理由见 docs/database.md 的说明）
        commentMapper.deleteByArticleId(id);
        tagMapper.deleteArticleTags(id);
        articleMapper.deleteById(id);
    }

    private PageResult<ArticleVO> doPage(ArticleQuery query) {
        long total = articleMapper.countByQuery(query);
        // 总数为 0 时没必要再查一次列表
        if (total == 0) {
            return PageResult.of(query, 0, List.of());
        }
        return PageResult.of(query, total, articleMapper.selectPage(query));
    }

    /** 校验标签存在后批量写入 article_tag */
    private void saveTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<Long> distinct = tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (distinct.isEmpty()) {
            return;
        }

        Set<Long> validIds = tagMapper.selectAll().stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());
        List<Long> invalid = distinct.stream()
                .filter(tagId -> !validIds.contains(tagId))
                .toList();
        if (!invalid.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "存在无效的标签：" + invalid);
        }

        tagMapper.insertArticleTags(articleId, distinct);
    }

    private boolean isCurrentAuthor(Long authorId) {
        CurrentUser current = UserContext.get();
        return current != null && current.isOwner(authorId);
    }
}
