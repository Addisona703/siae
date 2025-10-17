package com.hngy.siae.gateway.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.hngy.siae.core.dto.GatewayUserInfo;
import com.hngy.siae.core.config.AuthProperties;
import com.hngy.siae.core.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 简化的网关认证过滤器
 * 职责：JWT验签 + 基础用户信息传递
 *
 * @author KEYKB
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GatewayAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final AuthProperties authProperties;

    // 无需认证的路径白名单
    private static final List<String> WHITELIST = Arrays.asList(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/notification/email/code/send",
        "/swagger-ui",
        "/v3/api-docs",
        "/actuator/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 跳过白名单路径
        if (isInWhitelist(path)) {
            return addGatewayHeaders(exchange, chain, null);
        }

        // 获取JWT Token
        String token = extractToken(exchange.getRequest());
        if (StrUtil.isBlank(token)) {
            return unauthorized(exchange, "Missing authentication token");
        }

        try {
            // 1. 校验JWT Token有效性（这里只做一次JWT解析）
            if (!jwtUtils.validateToken(token)) {
                return unauthorized(exchange, "Invalid or expired token");
            }

            // 2. 提取基础用户信息（不查询权限，减少网关处理时间）
            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);
            Long expireTime = jwtUtils.getExpirationFromToken(token);

            if (userId == null || StrUtil.isBlank(username)) {
                return unauthorized(exchange, "Invalid user information in token");
            }

            // 3. 构建简化的用户信息对象
            GatewayUserInfo userInfo = GatewayUserInfo.builder()
                .userId(userId)
                .username(username)
                .jwtExpireTime(expireTime)
                .gatewayTimestamp(System.currentTimeMillis())
                .build();

            // 4. 传递用户信息到微服务
            return addGatewayHeaders(exchange, chain, userInfo);

        } catch (Exception e) {
            log.error("Gateway JWT validation failed for path: {}", path, e);
            return unauthorized(exchange, "JWT validation failed");
        }
    }

    /**
     * 添加网关认证头信息
     */
    private Mono<Void> addGatewayHeaders(ServerWebExchange exchange, GatewayFilterChain chain,
                                        GatewayUserInfo userInfo) {
        ServerWebExchange.Builder exchangeBuilder = exchange.mutate()
            .request(requestBuilder -> {
                // 标识请求来自网关
                requestBuilder.header("X-Gateway-Auth", "true");
                requestBuilder.header("X-Gateway-Secret", generateGatewaySecret());

                // 传递基础用户信息（如果有）
                if (userInfo != null) {
                    requestBuilder.header("X-User-Id", userInfo.getUserId().toString());
                    requestBuilder.header("X-User-Name", userInfo.getUsername());
                    requestBuilder.header("X-JWT-Expire-Time", userInfo.getJwtExpireTime().toString());
                    requestBuilder.header("X-Gateway-Timestamp", userInfo.getGatewayTimestamp().toString());

                    log.debug("Gateway authentication success for user: {}", userInfo.getUsername());
                }
            });

        return chain.filter(exchangeBuilder.build());
    }

    /**
     * 生成网关动态密钥（防重放攻击）
     */
    private String generateGatewaySecret() {
        long timestamp = System.currentTimeMillis() / 1000;
        return DigestUtil.md5Hex(timestamp + ":" + authProperties.getGatewaySecretKey());
    }

    /**
     * 提取JWT Token
     */
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 检查是否在白名单中
     */
    private boolean isInWhitelist(String path) {
        return WHITELIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String result = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级，确保在路由过滤器之前执行
    }
}