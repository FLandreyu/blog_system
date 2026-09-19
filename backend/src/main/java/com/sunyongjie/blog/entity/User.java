package com.sunyongjie.blog.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户，对应表 user。
 */
@Data
public class User {

    private Long id;
    private String username;
    /** BCrypt 哈希，绝不返回给前端 */
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    /** ADMIN / USER */
    private String role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
