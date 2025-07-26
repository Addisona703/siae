package com.hngy.siae.security.filter;

import com.hngy.siae.core.utils.JwtUtils;
import com.hngy.siae.security.properties.SecurityProperties;
import com.hngy.siae.security.service.RedisPermissionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器
 * 
 * 功能特性：
 * 1. 支持配置化的JWT认证开关
 * 2. 支持白名单路径跳过认证
 * 3. 从JWT中提取用户信息
 * 4. 从Redis缓存中获取用户权限（优化性能）
 * 5. 构建Spring Security认证对象
 * 6. 优雅处理各种异常情况
 * 
 * @author SIAE开发团队
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "siae.security.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtils jwtUtils;
    private final RedisPermissionService redisPermissionService;
    private final SecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // 检查是否为白名单路径
            String requestPath = request.getRequestURI();
            if (isWhitelistPath(requestPath)) {
                if (securityProperties.getPermission().isLogEnabled()) {
                    log.debug("白名单路径跳过JWT认证: {}", requestPath);
                }
                filterChain.doFilter(request, response);
                return;
            }
            
            // 从请求头中获取JWT
            String jwt = getJwtFromRequest(request);
            
            // 验证JWT是否有效
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                // 从JWT中提取基本用户信息
                Long userId = jwtUtils.getUserId(jwt);
                String username = jwtUtils.getUsername(jwt);

                log.info("JWT验证成功，提取用户信息: userId={}, username={}, 路径: {}", userId, username, requestPath);

                if (userId != null && username != null) {
                    // 从权限服务中获取用户权限
                    List<String> authorities = getUserAuthorities(userId);

                    log.info("获取用户权限: userId={}, 权限列表: {}", userId, authorities);

                    // 转换为Spring Security权限对象
                    List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    log.info("转换为Spring Security权限: userId={}, 权限对象: {}", userId, grantedAuthorities);

                    // 创建认证令牌并设置到上下文中
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, grantedAuthorities);

                    // 设置用户ID到认证对象的details中，方便后续使用
                    authentication.setDetails(userId);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("JWT认证成功并设置安全上下文, 用户: {}, 权限数量: {}, 路径: {}", username, authorities.size(), requestPath);

                    if (securityProperties.getPermission().isLogEnabled()) {
                        log.debug("JWT认证成功, 用户: {}, 权限数量: {}", username, authorities.size());
                    }
                } else {
                    log.warn("JWT中缺少必要的用户信息: userId={}, username={}", userId, username);
                }
            } else if (jwt != null) {
                log.warn("无效的JWT令牌，路径: {}", requestPath);
            } else {
                log.info("请求中没有JWT令牌，路径: {}", requestPath);
            }
        } catch (Exception ex) {
            log.error("JWT认证过程中发生异常，路径: {}", request.getRequestURI(), ex);
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
    private List<String> getUserAuthorities(Long userId) {
        log.info("开始获取用户权限，用户ID: {}", userId);

        try {
            // 从Redis缓存获取用户的所有权限（包括角色）
            List<String> authorities = redisPermissionService.getAllUserAuthorities(userId);

            log.info("Redis权限服务返回结果，用户ID: {}, 权限列表: {}", userId, authorities);

            if (authorities.isEmpty()) {
                log.warn("用户权限为空，用户ID: {}，可能需要重新登录或分配权限", userId);
            }

            if (securityProperties.getPermission().isLogEnabled()) {
                log.debug("从Redis缓存获取用户权限成功，用户ID: {}, 权限数量: {}", userId, authorities.size());
            }

            return authorities;
        } catch (Exception e) {
            log.error("从Redis缓存获取用户权限失败，用户ID: {}，将使用空权限列表", userId, e);

            // 根据配置决定是否抛出异常
            if (securityProperties.getPermission().isThrowExceptionOnFailure()) {
                throw new RuntimeException("从Redis缓存获取用户权限失败", e);
            }

            // Redis缓存失败时，返回空权限列表
            // 注意：这里可以考虑添加数据库回退逻辑，但为了性能优化，暂时返回空列表
            return Collections.emptyList();
        }
    }
    
    /**
     * 检查路径是否在白名单中
     * 
     * @param path 请求路径
     * @return 是否在白名单中
     */
    private boolean isWhitelistPath(String path) {
        return securityProperties.getWhitelistPaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    
    /**
     * 从HTTP请求中提取JWT令牌
     * 
     * @param request HTTP请求
     * @return JWT令牌，如果不存在返回null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String headerName = securityProperties.getJwt().getHeaderName();
        String tokenPrefix = securityProperties.getJwt().getTokenPrefix();
        
        String bearerToken = request.getHeader(headerName);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix)) {
            return bearerToken.substring(tokenPrefix.length());
        }
        return null;
    }
}
