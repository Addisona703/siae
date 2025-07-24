package com.hngy.siae.common.configs;

import com.hngy.siae.common.filter.OptimizedJwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 通用安全配置类，适用于所有需要安全认证的子服务
 *
 * 使用优化的JWT认证过滤器，支持Redis权限缓存
 * 只有在Redis相关类存在时才会启用
 *
 * @author KEYKB
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
public class SecurityConfig {

    private final OptimizedJwtAuthenticationFilter optimizedJwtAuthenticationFilter;

    /**
     * 密码加密器
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链
     */
    @Bean
    public SecurityFilterChain commonSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable) // ✅ 禁用默认表单登录
                .httpBasic(AbstractHttpConfigurer::disable) // ✅ 禁用 httpBasic 登录
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/doc.html", "/webjars/**"
                        ).permitAll()
                        .requestMatchers(
                                "/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh-token"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(optimizedJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
