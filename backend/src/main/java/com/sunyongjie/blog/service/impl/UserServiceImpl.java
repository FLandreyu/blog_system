package com.sunyongjie.blog.service.impl;

import com.sunyongjie.blog.common.BizException;
import com.sunyongjie.blog.common.CurrentUser;
import com.sunyongjie.blog.common.ResultCode;
import com.sunyongjie.blog.dto.LoginRequest;
import com.sunyongjie.blog.dto.LoginResponse;
import com.sunyongjie.blog.dto.RegisterRequest;
import com.sunyongjie.blog.dto.UpdateProfileRequest;
import com.sunyongjie.blog.dto.UserVO;
import com.sunyongjie.blog.entity.User;
import com.sunyongjie.blog.mapper.UserMapper;
import com.sunyongjie.blog.service.UserService;
import com.sunyongjie.blog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户业务实现。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserVO register(RegisterRequest request) {
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户名已被占用");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // 只存 BCrypt 哈希，明文密码连日志都不留
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null);
        user.setRole(CurrentUser.ROLE_USER);

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 上面的 countByUsername 存在并发窗口：两个请求可能同时通过校验，
            // 真正兜底的是 user 表的唯一索引 uk_username。
            throw new BizException(ResultCode.BAD_REQUEST, "用户名已被占用");
        }
        return UserVO.from(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());

        // 用户不存在和密码错误返回同一句提示：
        // 否则攻击者可以靠错误信息的差异批量枚举出哪些用户名真实存在。
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }

        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, UserVO.from(user));
    }

    @Override
    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return UserVO.from(user);
    }

    @Override
    @Transactional
    public UserVO updateProfile(Long userId, UpdateProfileRequest request) {
        if (userMapper.selectById(userId) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }

        User update = new User();
        update.setId(userId);
        update.setNickname(request.getNickname());
        update.setAvatar(StringUtils.hasText(request.getAvatar()) ? request.getAvatar() : null);
        userMapper.updateProfile(update);

        return UserVO.from(userMapper.selectById(userId));
    }
}
