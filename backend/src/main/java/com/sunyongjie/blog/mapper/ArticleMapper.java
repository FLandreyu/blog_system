package com.sunyongjie.blog.mapper;

import com.sunyongjie.blog.dto.ArticleDetailVO;
import com.sunyongjie.blog.dto.ArticleQuery;
import com.sunyongjie.blog.dto.ArticleVO;
import com.sunyongjie.blog.entity.Article;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章表操作。
 *
 * <p>列表查询把整个 {@link ArticleQuery} 作为参数传进 XML，
 * 由 {@code <where>} + {@code <if>} 决定拼哪些条件。
 */
public interface ArticleMapper {

    List<ArticleVO> selectPage(ArticleQuery query);

    long countByQuery(ArticleQuery query);

    Article selectById(@Param("id") Long id);

    ArticleDetailVO selectDetailById(@Param("id") Long id);

    int insert(Article article);

    int update(Article article);

    int deleteById(@Param("id") Long id);

    /** 该分类下还有多少篇文章，删除分类前校验用 */
    int countByCategoryId(@Param("categoryId") Long categoryId);

    /** 浏览量自增，交给数据库做，避免"读出来 +1 再写回去"的丢更新 */
    int incrementViewCount(@Param("id") Long id);
}
