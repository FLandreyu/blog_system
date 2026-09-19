package com.sunyongjie.blog.mapper;

import com.sunyongjie.blog.entity.Tag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签表 + 文章标签关联表操作。
 */
public interface TagMapper {

    List<Tag> selectAll();

    /** 某篇文章的标签 */
    List<Tag> selectByArticleId(@Param("articleId") Long articleId);

    Tag selectByName(@Param("name") String name);

    int insert(Tag tag);

    /** 批量写 article_tag，用 <foreach> 拼成一条 INSERT */
    int insertArticleTags(@Param("articleId") Long articleId, @Param("tagIds") List<Long> tagIds);

    /** 编辑文章时先清空原有标签再重新写 */
    int deleteArticleTags(@Param("articleId") Long articleId);
}
