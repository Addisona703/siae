package com.hngy.siae.common.filter;

import com.hngy.siae.common.service.RedisPermissionService;
import com.hngy.siae.core.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 优化的JWT认证过滤器
 * 
 * 特性：
 * 1. 从JWT中提取基本用户信息（userId, username）
 * 2. 从Redis缓存中获取用户权限信息
 * 3. 构建Spring Security认证对象
 * 4. 优雅处理Redis连接失败的情况
 * 
 * 相比原版JwtAuthenticationFilter的优势：
 * - JWT token大小显著减少（不包含权限信息）
 * - 权限信息实时性更好（可以动态更新Redis缓存）
 * - 支持权限的集中管理和缓存
 * 
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
public class OptimizedJwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtils jwtUtils;
    private final RedisPermissionService redisPermissionService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // 从请求头中获取JWT
            String jwt = getJwtFromRequest(request);
            
            // 验证JWT是否有效
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                // 从JWT中提取基本用户信息
                Long userId = jwtUtils.getUserId(jwt);
                String username = jwtUtils.getUsername(jwt);
                
                if (userId != null && username != null) {
                    // 从Redis缓存中获取用户权限
                    List<String> authorities = getUserAuthoritiesFromCache(userId);
                    
                    // 转换为Spring Security权限对象
                    List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    
                    // 创建认证令牌并设置到上下文中
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, grantedAuthorities);
                    
                    // 设置用户ID到认证对象的details中，方便后续使用
                    authentication.setDetails(userId);
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("设置认证信息到SecurityContext成功, 用户: {}, 权限数量: {}", username, authorities.size());
                } else {
                    log.warn("JWT中缺少必要的用户信息: userId={}, username={}", userId, username);
                }
            }
        } catch (Exception ex) {
            log.error("设置用户认证失败", ex);
            // 清除可能的部分认证信息
            SecurityContextHolder.clearContext();
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从Redis缓存中获取用户权限
     * 
     * @param userId 用户ID
     * @return 权限列表，如果获取失败返回空列表
     */
    private List<String> getUserAuthoritiesFromCache(Long userId) {
        try {
            // 尝试从Redis获取用户的所有权限（包括角色）
            List<String> authorities = redisPermissionService.getAllUserAuthorities(userId);
            
            if (authorities.isEmpty()) {
                log.warn("用户权限缓存为空，用户ID: {}，可能需要重新登录", userId);
            }
            
            return authorities;
        } catch (Exception e) {
            log.error("从Redis获取用户权限失败，用户ID: {}，将使用空权限列表", userId, e);
            // Redis连接失败时，返回空权限列表而不是阻止用户访问
            // 这样可以保证系统的可用性，但可能需要根据业务需求调整策略
            return Collections.emptyList();
        }
    }
    
    /**
     * 从HTTP请求中提取JWT令牌
     * 
     * @param request HTTP请求
     * @return JWT令牌，如果不存在返回null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
