package com.hngy.siae.gateway.config;

import com.hngy.siae.gateway.filter.GatewayAuthFilter;
// import com.hngy.siae.gateway.filter.JwtAuthFilter; // 旧版本，已废弃
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关安全配置（优化版本）
 *
 * ✅ 已更新：使用新的 GatewayAuthFilter（JWT网关优化方案）
 * ❌ 废弃：JwtAuthFilter（旧版本）
 *
 * 注意：GatewayAuthFilter 实现了 GlobalFilter，会自动被 Spring Cloud Gateway 识别执行
 * 此配置主要负责 Spring Security 的权限控制和异常处理
 *
 * @author SIAE开发团队
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    /**
     * 注入 GatewayAuthFilter 以确保其被正确初始化
     * 虽然 GlobalFilter 会自动执行，但注入可以确保配置正确性
     */
//    @Autowired
    private GatewayAuthFilter gatewayAuthFilter;

    // @Autowired
    // private JwtAuthFilter jwtAuthFilter; // 旧版本，已废弃

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("🔐 加载 Gateway 安全配置（WebFlux + 优化版本）");
        log.info("认证策略: 网关验签 + 用户信息传递");
        log.info("GatewayAuthFilter 已注入，将自动执行 GlobalFilter 逻辑");

        // 自定义认证异常处理器，未认证时触发
        ServerAuthenticationEntryPoint authenticationEntryPoint = (exchange, ex) -> {
            log.error("网关认证失败，未认证访问: {} - {}", exchange.getRequest().getURI().getPath(), ex.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");

            String errorBody = "{\"code\":401,\"message\":\"Unauthorized access\",\"data\":null}";
            var dataBuffer = exchange.getResponse().bufferFactory().wrap(errorBody.getBytes());
            return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        };

        // 自定义授权异常处理器，权限不足时触发
        ServerAccessDeniedHandler accessDeniedHandler = (exchange, denied) -> {
            log.error("网关授权失败，权限不足: {} - {}", exchange.getRequest().getURI().getPath(), denied.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");

            String errorBody = "{\"code\":403,\"message\":\"Access denied\",\"data\":null}";
            var dataBuffer = exchange.getResponse().bufferFactory().wrap(errorBody.getBytes());
            return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        };

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeExchange(exchanges -> exchanges
                        // 网关本身不需要认证，只负责路由和头信息传递
                        // 具体的认证逻辑由 GatewayAuthFilter 处理
                        .anyExchange().permitAll()
                )
                // 注意：GatewayAuthFilter 作为 GlobalFilter 会自动执行，无需手动添加
                // .addFilterBefore(gatewayAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
