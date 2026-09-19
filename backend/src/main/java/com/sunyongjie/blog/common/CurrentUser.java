package com.sunyongjie.blog.common;

/**
 * 从 JWT 解析出来的当前登录人，只放鉴权需要的字段，不含密码。
 */
public record CurrentUser(Long id, String username, String role) {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    /** 是否是某个资源的作者本人（越权判断用） */
    public boolean isOwner(Long ownerId) {
        return id != null && id.equals(ownerId);
    }
}
