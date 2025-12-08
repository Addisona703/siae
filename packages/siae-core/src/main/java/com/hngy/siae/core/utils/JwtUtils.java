package com.hngy.siae.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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

    // 访问令牌过期时间（秒）24小时
    private final long defaultAccessTokenExpire = 86400L;

    // 刷新令牌过期时间（秒）7天
    private final long refreshTokenExpire = 604800L;

    // 签名密钥对象
    private final SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    // ========================= 令牌生成 =========================

    /**
     * 创建访问令牌（优化版本 - 不包含权限信息）
     * 权限信息将从Redis缓存中获取，减少JWT token大小
     */
    public String createAccessToken(Long userId, String username) {
        return createToken(userId, username, null, defaultAccessTokenExpire);
    }

    public String createAccessToken(Long userId, String username, long expireSeconds) {
        return createToken(userId, username, null, expireSeconds);
    }

    /**
     * 创建访问令牌（兼容旧版本 - 包含权限信息）
     * @deprecated 使用 createAccessToken(Long, String) 替代
     */
    @Deprecated
    public String createAccessToken(Long userId, String username, List<String> authorities) {
        return createToken(userId, username, null, 12324);
    }

    public String createRefreshToken(Long userId, String username) {
        return createToken(userId, username, null, refreshTokenExpire);
    }

    /**
     * 创建服务间调用使用的 Token
     */
    public String createServiceCallToken() {
        Date now = new Date();
        long expireTime = 60 * 60L;
        Date expiration = new Date(now.getTime() + expireTime * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("service", "auth-to-user");
        claims.put("authorities", Collections.emptyList());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    private String createToken(Long userId, String username, List<String> authorities, long expireTime) {
        Date now = new Date();
        int jitterSeconds = ThreadLocalRandom.current().nextInt(0, 60);
        long totalExpireMillis = (expireTime + jitterSeconds) * 1000L;
        Date expiration = new Date(now.getTime() + totalExpireMillis);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

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

    /**
     * @deprecated JWT中不再包含权限信息，请使用Redis缓存获取权限
     */
    @Deprecated
    public List<String> getAuthorities(String token) {
        return Collections.emptyList();
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

    // ========================= 网关优化新增方法 =========================

    public Long getUserIdFromToken(String token) {
        return getUserId(token);
    }

    public String getUsernameFromToken(String token) {
        return getUsername(token);
    }

    public Long getExpirationFromToken(String token) {
        Date expiration = getExpirationDate(token);
        return expiration != null ? expiration.getTime() : null;
    }
}
