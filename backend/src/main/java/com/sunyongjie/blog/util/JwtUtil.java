package com.sunyongjie.blog.util;

import com.sunyongjie.blog.common.CurrentUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * JWT 签发与解析。
 *
 * <p>token 由三部分组成：{@code header.payload.signature}。
 * payload 里只放 userId(sub)、username、role —— <b>不放密码，也不放敏感信息</b>，
 * 因为 JWT 只是签名不是加密，任何人都能解出 payload。
 *
 * <p>签名算法由密钥长度决定，这里用 HS256（要求密钥 ≥ 256 位，即 32 字节）。
 */
@Component
public class JwtUtil {

    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLE = "role";

    /** HS256 要求的最小密钥长度（字节） */
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final Duration expire;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire-hours:24}") long expireHours) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            // 早点报错，好过启动后每个请求都签名失败
            throw new IllegalStateException(
                    "jwt.secret 太短：HS256 要求至少 " + MIN_SECRET_BYTES + " 字节，当前 "
                            + secretBytes.length + " 字节。请在 application-local.yml 里换一个更长的随机串。");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expire = Duration.ofHours(expireHours);
    }

    /** 登录成功后签发 token */
    public String generate(Long userId, String username, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expire.toMillis());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * 校验签名与有效期并还原登录人。
     *
     * @throws io.jsonwebtoken.JwtException 签名不对、已过期、格式错误都会抛它
     */
    public CurrentUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new CurrentUser(
                Long.valueOf(claims.getSubject()),
                claims.get(CLAIM_USERNAME, String.class),
                claims.get(CLAIM_ROLE, String.class));
    }
}
