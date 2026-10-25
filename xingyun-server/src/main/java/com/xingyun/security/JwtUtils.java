package com.xingyun.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：负责生成与解析 Token（登录后下发，后续请求在 Authorization 头携带）。
 *
 * @author xingyun
 */
@Component
public class JwtUtils {

    @Value("${xingyun.jwt.secret}")
    private String secret;

    @Value("${xingyun.jwt.expire}")
    private long expire;

    /** 启动即校验密钥强度，避免弱密钥在生产环境静默生效 */
    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT 密钥长度不足 32 字节，请通过 XINGYUN_JWT_SECRET 配置强密钥");
        }
    }

    private SecretKey getKey() {
        // HS256 要求密钥至少 256bit(32字节)
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token
     *
     * @param userId   用户ID
     * @param username 账号
     * @return JWT 字符串
     */
    public String createToken(Long userId, String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expire);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 token，失败会抛异常（由调用方捕获）
     */
    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** 从 token 取用户ID */
    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }
}
