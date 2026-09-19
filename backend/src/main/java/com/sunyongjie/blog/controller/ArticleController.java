package com.sunyongjie.blog.controller;

import com.sunyongjie.blog.common.PageResult;
import com.sunyongjie.blog.common.RequireLogin;
import com.sunyongjie.blog.common.Result;
import com.sunyongjie.blog.dto.ArticleDetailVO;
import com.sunyongjie.blog.dto.ArticleQuery;
import com.sunyongjie.blog.dto.ArticleRequest;
import com.sunyongjie.blog.dto.ArticleVO;
import com.sunyongjie.blog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章接口。Controller 只做参数绑定与转发，业务判断都在 Service。
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public Result<PageResult<ArticleVO>> page(@ModelAttribute ArticleQuery query) {
        return Result.ok(articleService.page(query));
    }

    /** 必须声明在 /{id} 之前，避免 "mine" 被当成路径变量 */
    @GetMapping("/mine")
    @RequireLogin
    public Result<PageResult<ArticleVO>> mine(@ModelAttribute ArticleQuery query) {
        return Result.ok(articleService.mine(query));
    }

    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return Result.ok(articleService.detail(id));
    }

    @PostMapping
    @RequireLogin
    public Result<Long> create(@Valid @RequestBody ArticleRequest request) {
        return Result.ok(articleService.create(request));
    }

    @PutMapping("/{id}")
    @RequireLogin
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        articleService.update(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.ok();
    }
}
