package com.hngy.siae.gateway.filter;

import com.hngy.siae.core.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT全局认证过滤器
 *
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher pathMatcher =  new AntPathMatcher();
    private final JwtUtils jwtUtils;

    /**
     * 白名单路径，不需要token验证
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 检查请求路径是否在白名单中
        if (isWhiteListPath(path)) {
            return chain.filter(exchange);
        }

        // 获取token
        String token = getTokenFromRequest(request);
        
        // 验证token
        if (!StringUtils.hasText(token) || !validateToken(token)) {
            return unauthorizedResponse(exchange);
        }
        
        // Token有效，转发请求
        return chain.filter(exchange);
    }

    /**
     * 判断请求路径是否在白名单中
     *
     * @param path 请求路径
     * @return 是否在白名单中
     */
    private boolean isWhiteListPath(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 从请求头中获取Token
     *
     * @param request 请求对象
     * @return Token字符串
     */
    private String getTokenFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 验证Token
     *
     * @param token JWT Token字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            return jwtUtils.validateToken(token);
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
        }
        return false;
    }


    /**
     * 返回未授权响应
     *
     * @param exchange ServerWebExchange对象
     * @return Mono<Void>
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String body = "{\"code\":401,\"message\":\"未授权：请先登录\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 确保在其他过滤器之前执行
    }
} 