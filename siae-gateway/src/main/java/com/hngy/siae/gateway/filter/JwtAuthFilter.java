package com.hngy.siae.gateway.filter;

import com.hngy.siae.core.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Spring WebFlux 环境下的 JWT 鉴权过滤器
 *
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    private final JwtUtils jwtUtils;

    private static final List<ServerWebExchangeMatcher> WHITE_LIST = List.of(
            ServerWebExchangeMatchers.pathMatchers("/api/v1/auth/login"),
            ServerWebExchangeMatchers.pathMatchers("/api/v1/auth/register"),
            ServerWebExchangeMatchers.pathMatchers("/api/v1/auth/refresh-token"),
            ServerWebExchangeMatchers.pathMatchers("/api/v1/auth/logout"),
            ServerWebExchangeMatchers.pathMatchers("/api/v1/message/email/code/send"),
            ServerWebExchangeMatchers.pathMatchers("/v3/api-docs/**"),
            ServerWebExchangeMatchers.pathMatchers("/swagger-ui/**"),
            ServerWebExchangeMatchers.pathMatchers("/swagger-resources/**"),
            ServerWebExchangeMatchers.pathMatchers("/webjars/**")
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return isWhiteListed(exchange).flatMap(isWhite -> {
            if (isWhite) {
                return chain.filter(exchange);
            }

            String token = extractToken(exchange);
            log.info("开始校验 JWT：{}，方法：{}，路径：{}", token, exchange.getRequest().getMethod(), exchange.getRequest().getURI().getPath());

            if (!StringUtils.hasText(token) || !validateToken(token)) {
                log.warn("JWT 校验失败");

                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                byte[] bytes = "{\"code\":401,\"message\":\"未授权：请先登录\"}"
                        .getBytes(StandardCharsets.UTF_8);
                return exchange.getResponse()
                        .writeWith(Mono.just(exchange.getResponse()
                                .bufferFactory()
                                .wrap(bytes)));
            }

            log.info("JWT 校验通过");

            Long userId = jwtUtils.getUserId(token);
            Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

        });
    }

    private Mono<Boolean> isWhiteListed(ServerWebExchange exchange) {
        return Flux.fromIterable(WHITE_LIST)
                .concatMap(matcher -> matcher.matches(exchange))
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .hasElements();
    }

    private String extractToken(ServerWebExchange exchange) {
        String bearer = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            return jwtUtils.validateToken(token);
        } catch (Exception e) {
            log.error("Token 验证异常: {}", e.getMessage());
            return false;
        }
    }
}
