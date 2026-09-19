package com.sunyongjie.blog.controller;

import com.sunyongjie.blog.common.RequireLogin;
import com.sunyongjie.blog.common.Result;
import com.sunyongjie.blog.common.UserContext;
import com.sunyongjie.blog.dto.LoginRequest;
import com.sunyongjie.blog.dto.LoginResponse;
import com.sunyongjie.blog.dto.RegisterRequest;
import com.sunyongjie.blog.dto.UpdateProfileRequest;
import com.sunyongjie.blog.dto.UserVO;
import com.sunyongjie.blog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(userService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(userService.login(request));
    }

    @GetMapping("/me")
    @RequireLogin
    public Result<UserVO> me() {
        return Result.ok(userService.getById(UserContext.requireUserId()));
    }

    @PutMapping("/me")
    @RequireLogin
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.ok(userService.updateProfile(UserContext.requireUserId(), request));
    }
}
