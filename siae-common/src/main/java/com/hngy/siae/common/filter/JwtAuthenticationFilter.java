package com.hngy.siae.common.filter;

import com.hngy.siae.core.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器
 * 
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
//@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtils jwtUtils;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // 从请求头中获取JWT
            String jwt = getJwtFromRequest(request);
            
            // 验证JWT是否有效
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                // 获取用户ID和用户名
                Long userId = jwtUtils.getUserId(jwt);
                String username = jwtUtils.getUsername(jwt);
                
                // 获取权限列表
                List<String> authorities = jwtUtils.getAuthorities(jwt);
                List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                
                // 创建认证令牌并设置到上下文中
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, grantedAuthorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("设置认证信息到SecurityContext, 用户: {}", username);
            }
        } catch (Exception ex) {
            log.error("无法设置用户认证", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求头中获取JWT
     *
     * @param request HTTP请求
     * @return JWT
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 