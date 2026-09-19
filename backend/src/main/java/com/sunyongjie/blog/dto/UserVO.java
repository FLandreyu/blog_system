package com.sunyongjie.blog.dto;

import com.sunyongjie.blog.entity.User;
import lombok.Data;

/**
 * 对外暴露的用户信息。
 *
 * <p>单独建一个 VO 而不是直接返回 {@link User}，
 * 是为了从源头上保证密码哈希不会被序列化出去。
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String role;

    public static UserVO from(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return vo;
    }
}
