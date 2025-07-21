package com.hngy.siae.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JWT 工具类，包含生成、解析、校验方法
 * 写死版，适合开发测试环境
 *
 * @author KEYKB
 */
@Slf4j
@Component
public class JwtUtils {

    // ========================= 配置区 =========================

    // JWT签名密钥（必须大于等于256位）
    private final String secret = "nD7I9zd0bReTmUiVpV9tlVMlNJTuwNTtcg351xRSxfy3DftE6nEhVnWZk0XiPFd";

    // 访问令牌过期时间（秒）1小时
    private final long accessTokenExpire = 3600L;

    // 刷新令牌过期时间（秒）7天
    private final long refreshTokenExpire = 604800L;

    // 签名密钥对象
    private final SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    // ========================= 令牌生成 =========================

    public String createAccessToken(Long userId, String username, List<String> authorities) {
        return createToken(userId, username, authorities, accessTokenExpire);
    }

    public String createRefreshToken(Long userId, String username) {
        return createToken(userId, username, null, refreshTokenExpire);
    }

    /**
     * 创建服务间调用使用的 Token
     * 你可以只放入固定的服务标识，没有权限列表，过期时间也可以设置得短一些
     */
    public String createServiceCallToken() {
        Date now = new Date();
        long expireTime = 60 * 60L; // 1小时有效期，视需求调整
        Date expiration = new Date(now.getTime() + expireTime * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("service", "auth-to-user"); // 标识是哪个服务调用哪个服务
        claims.put("authorities", Collections.emptyList()); // 在 createServiceCallToken() 里给这个 token 加个空权限列表

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    private String createToken(Long userId, String username, List<String> authorities, long expireTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireTime * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        if (authorities != null) {
            claims.put("authorities", authorities);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // ========================= Token解析 =========================

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getAuthorities(String token) {
        return parseToken(token).get("authorities", List.class);
    }

    // ========================= Token校验 =========================

    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    public Date getExpirationDate(String token) {
        return parseToken(token).getExpiration();
    }
}
