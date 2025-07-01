package com.hngy.siae.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT工具类
 * 
 * @author KEYKB
 */
@Component
public class JwtUtils {
    
    /**
     * 密钥
     */
    private final SecretKey secretKey;
    
    /**
     * 访问令牌过期时间(秒)
     */
    private final long accessTokenExpire;
    
    /**
     * 刷新令牌过期时间(秒)
     */
    private final long refreshTokenExpire;
    
    /**
     * 构造函数
     *
     * @param secretKey         密钥
     * @param accessTokenExpire 访问令牌过期时间(秒)
     * @param refreshTokenExpire 刷新令牌过期时间(秒)
     */
    public JwtUtils(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-token-expire}") long accessTokenExpire,
            @Value("${jwt.refresh-token-expire}") long refreshTokenExpire) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpire = accessTokenExpire;
        this.refreshTokenExpire = refreshTokenExpire;
    }
    
    /**
     * 创建访问令牌
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param authorities 权限列表
     * @return 访问令牌
     */
    public String createAccessToken(Long userId, String username, List<String> authorities) {
        return createToken(userId, username, authorities, accessTokenExpire);
    }
    
    /**
     * 创建刷新令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 刷新令牌
     */
    public String createRefreshToken(Long userId, String username) {
        return createToken(userId, username, null, refreshTokenExpire);
    }
    
    /**
     * 创建令牌
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param authorities 权限列表
     * @param expireTime 过期时间(秒)
     * @return 令牌
     */
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
    
    /**
     * 解析令牌
     *
     * @param token 令牌
     * @return 声明
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 获取用户ID
     *
     * @param token 令牌
     * @return 用户ID
     */
    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }
    
    /**
     * 获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }
    
    /**
     * 获取权限列表
     *
     * @param token 令牌
     * @return 权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getAuthorities(String token) {
        return parseToken(token).get("authorities", List.class);
    }
    
    /**
     * 验证令牌是否有效
     *
     * @param token 令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取过期时间
     *
     * @param token 令牌
     * @return 过期时间
     */
    public Date getExpirationDate(String token) {
        return parseToken(token).getExpiration();
    }
} 