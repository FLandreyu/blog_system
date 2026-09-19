package com.sunyongjie.blog.controller;

import com.sunyongjie.blog.common.RequireLogin;
import com.sunyongjie.blog.common.Result;
import com.sunyongjie.blog.dto.CommentRequest;
import com.sunyongjie.blog.dto.CommentVO;
import com.sunyongjie.blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评论接口。看是公开的，发和删要登录。
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public Result<List<CommentVO>> list(@RequestParam Long articleId) {
        return Result.ok(commentService.listByArticle(articleId));
    }

    @PostMapping
    @RequireLogin
    public Result<Long> create(@Valid @RequestBody CommentRequest request) {
        return Result.ok(commentService.create(request));
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.ok();
    }
}
