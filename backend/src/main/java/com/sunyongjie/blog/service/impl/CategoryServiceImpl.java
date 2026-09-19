package com.sunyongjie.blog.service.impl;

import com.sunyongjie.blog.common.BizException;
import com.sunyongjie.blog.common.ResultCode;
import com.sunyongjie.blog.dto.CategoryRequest;
import com.sunyongjie.blog.dto.CategoryVO;
import com.sunyongjie.blog.entity.Category;
import com.sunyongjie.blog.mapper.ArticleMapper;
import com.sunyongjie.blog.mapper.CategoryMapper;
import com.sunyongjie.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类业务实现。
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    @Override
    public List<CategoryVO> list() {
        return categoryMapper.selectAllWithCount();
    }

    @Override
    @Transactional
    public CategoryVO create(CategoryRequest request) {
        if (categoryMapper.countByName(request.getName(), null) > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "分类名已存在");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        categoryMapper.insert(category);

        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSort(category.getSort());
        vo.setArticleCount(0);
        return vo;
    }

    @Override
    @Transactional
    public void update(Long id, CategoryRequest request) {
        if (categoryMapper.selectById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "分类不存在");
        }
        // 排除自己，否则"保持原名保存"会被自己判成重名
        if (categoryMapper.countByName(request.getName(), id) > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "分类名已存在");
        }

        Category category = new Category();
        category.setId(id);
        category.setName(request.getName());
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        categoryMapper.update(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (categoryMapper.selectById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "分类不存在");
        }

        // 被引用时直接拒绝，而不是把文章的 category_id 置 NULL：
        // 置 NULL 会让文章悄悄失去分类且作者毫无感知，删除动作应该在报错里被看见。
        int used = articleMapper.countByCategoryId(id);
        if (used > 0) {
            throw new BizException(ResultCode.BAD_REQUEST,
                    "该分类下还有 " + used + " 篇文章，请先移走或删除这些文章");
        }
        categoryMapper.deleteById(id);
    }
}
