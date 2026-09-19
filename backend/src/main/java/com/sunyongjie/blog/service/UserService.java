package com.sunyongjie.blog.service;

import com.sunyongjie.blog.dto.LoginRequest;
import com.sunyongjie.blog.dto.LoginResponse;
import com.sunyongjie.blog.dto.RegisterRequest;
import com.sunyongjie.blog.dto.UpdateProfileRequest;
import com.sunyongjie.blog.dto.UserVO;

/**
 * 用户相关业务。
 */
public interface UserService {

    UserVO register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserVO getById(Long id);

    UserVO updateProfile(Long userId, UpdateProfileRequest request);
}
