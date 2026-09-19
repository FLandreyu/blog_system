package com.sunyongjie.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 通用 Bean。
 */
@Configuration
public class AppConfig {

    /**
     * BCrypt 密码编码器。
     *
     * <p>BCrypt 每次加密都会随机生成 salt 并把它写进结果串里，
     * 所以同一个明文两次加密得到的哈希并不相同 —— 校验只能用
     * {@code matches(raw, encoded)}，不能拿哈希去比相等。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
