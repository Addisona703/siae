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
    private final long accessTokenExpire = 86400L;

    // 刷新令牌过期时间（秒）7天
    private final long refreshTokenExpire = 604800L;

    // 签名密钥对象
    private final SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    // ========================= 令牌生成 =========================

    /**
     * 创建访问令牌（优化版本 - 不包含权限信息）
     * 权限信息将从Redis缓存中获取，减少JWT token大小
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT访问令牌
     */
    public String createAccessToken(Long userId, String username) {
        return createToken(userId, username, null, accessTokenExpire);
    }

    /**
     * 创建访问令牌（兼容旧版本 - 包含权限信息）
     *
     * @deprecated 使用 createAccessToken(Long, String) 替代
     * @param userId 用户ID
     * @param username 用户名
     * @param authorities 权限列表（将被忽略）
     * @return JWT访问令牌
     */
    @Deprecated
    public String createAccessToken(Long userId, String username, List<String> authorities) {
        // 忽略authorities参数，不再将权限信息存储在JWT中
        return createToken(userId, username, null, accessTokenExpire);
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

    /**
     * 创建JWT令牌（优化版本 - 仅包含基本信息）
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param authorities 权限列表（已废弃，将被忽略）
     * @param expireTime 过期时间（秒）
     * @return JWT令牌
     */
    private String createToken(Long userId, String username, List<String> authorities, long expireTime) {
        Date now = new Date();
        // 防止雪崩：增加 0~60 秒随机偏移
        int jitterSeconds = ThreadLocalRandom.current().nextInt(0, 60);
        long totalExpireMillis = (expireTime + jitterSeconds) * 1000L;

        Date expiration = new Date(now.getTime() + totalExpireMillis);

        // 优化后的JWT只包含基本信息：userId, username, exp
        // 权限信息将从Redis缓存中获取，大大减少token大小
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        // 不再包含authorities，减少token大小

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
     * 获取权限列表（已废弃）
     *
     * @deprecated JWT中不再包含权限信息，请使用Redis缓存获取权限
     * @param token JWT令牌
     * @return 空列表（权限信息已移至Redis缓存）
     */
    @Deprecated
    public List<String> getAuthorities(String token) {
        // JWT中不再包含权限信息，返回空列表
        // 权限信息应该从Redis缓存中获取
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

    /**
     * 从Token获取用户ID（网关层使用）
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        return getUserId(token);
    }

    /**
     * 从Token获取用户名（网关层使用）
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getUsername(token);
    }

    /**
     * 从Token获取过期时间戳（网关层使用）
     *
     * @param token JWT令牌
     * @return 过期时间戳（毫秒）
     */
    public Long getExpirationFromToken(String token) {
        Date expiration = getExpirationDate(token);
        return expiration != null ? expiration.getTime() : null;
    }
}
