package com.hngy.siae.security.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.hngy.siae.core.enums.RequestSource;
import com.hngy.siae.core.config.AuthProperties;
import com.hngy.siae.security.service.RedisPermissionService;
import com.hngy.siae.core.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 微服务认证过滤器
 * 职责：读取网关信息 + 权限查询 + 认证上下文填充
 *
 * @author KEYKB
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final RedisPermissionService redisPermissionService;
    private final AuthProperties authProperties;

    // 白名单路径（无需认证）
    private static final List<String> WHITELIST = Arrays.asList(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/notification/email/code/send",
        "/swagger-ui",
        "/v3/api-docs",
        "/actuator/health"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. 识别请求来源
            RequestSource requestSource = identifyRequestSource(request);
            log.debug("Request source identified: {} for path: {}", requestSource, request.getRequestURI());

            // 2. 根据来源采用不同的认证策略
            switch (requestSource) {
                case EXTERNAL_VIA_GATEWAY:
                    handleGatewayRequest(request);
                    break;

                case INTERNAL_SERVICE_CALL:
                    handleInternalServiceCall(request);
                    break;

                case DIRECT_EXTERNAL:
                    handleDirectExternalRequest(request);
                    break;
            }

            filterChain.doFilter(request, response);

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for {}: {}", request.getRequestURI(), e.getMessage());
            handleAuthenticationException(response, e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 识别请求来源（简化版本）
     */
    private RequestSource identifyRequestSource(HttpServletRequest request) {
        String gatewayAuth = request.getHeader("X-Gateway-Auth");
        String gatewaySecret = request.getHeader("X-Gateway-Secret");
        String internalServiceCall = request.getHeader("X-Internal-Service-Call");

        // 1. 检查是否来自网关
        if ("true".equals(gatewayAuth) && isValidGatewaySecret(gatewaySecret)) {
            return RequestSource.EXTERNAL_VIA_GATEWAY;
        }

        // 2. 检查是否为内部服务调用
        if (StrUtil.isNotBlank(internalServiceCall) &&
            authProperties.getInternalSecretKey().equals(internalServiceCall)) {
            return RequestSource.INTERNAL_SERVICE_CALL;
        }

        // 3. 其他情况 - 生产环境应该拒绝
        return RequestSource.DIRECT_EXTERNAL;
    }

    /**
     * 处理来自网关的请求 - 核心优化点
     * 不再进行JWT解析，直接使用网关传递的用户信息
     */
    private void handleGatewayRequest(HttpServletRequest request) throws AuthenticationException {
        String userIdHeader = request.getHeader("X-User-Id");
        String usernameHeader = request.getHeader("X-User-Name");

        // 白名单请求可能没有用户信息
        if (StrUtil.isBlank(userIdHeader)) {
            if (isInWhitelist(request.getRequestURI())) {
                log.debug("Gateway whitelist request: {}", request.getRequestURI());
                return;
            } else {
                throw new AuthenticationException("Missing user info from gateway");
            }
        }

        try {
            Long userId = Long.parseLong(userIdHeader);

            // 从Redis查询用户权限（这里是唯一的权限查询点）
            List<String> permissions = redisPermissionService.getAllUserAuthorities(userId);
            if (permissions == null) {
                log.warn("User permissions not found in cache for user: {}, loading from database", usernameHeader);
                // 这里可以添加从数据库加载权限的逻辑
                permissions = Collections.emptyList();
            }

            // 设置Spring Security上下文
            setSecurityContext(userId, usernameHeader, permissions);

            log.debug("Gateway request authenticated for user: {} with {} permissions",
                    usernameHeader, permissions.size());

        } catch (NumberFormatException e) {
            throw new AuthenticationException("Invalid user ID from gateway");
        }
    }

    /**
     * 处理内部服务调用
     * 为内部服务调用设置系统级或用户级认证上下文
     */
    private void handleInternalServiceCall(HttpServletRequest request) throws AuthenticationException {
        String callerService = request.getHeader("X-Caller-Service");
        String onBehalfOfUser = request.getHeader("X-On-Behalf-Of-User");

        // 如果是代表用户的调用，加载用户上下文
        if (StrUtil.isNotBlank(onBehalfOfUser)) {
            try {
                Long userId = Long.parseLong(onBehalfOfUser);
                List<String> permissions = redisPermissionService.getAllUserAuthorities(userId);

                setSecurityContext(userId, "service-call-user", permissions != null ? permissions : Collections.emptyList());
                log.debug("Internal service call authenticated from: {} on behalf of user: {}", callerService, userId);

            } catch (NumberFormatException e) {
                throw new AuthenticationException("Invalid user ID in internal service call");
            }
        } else {
            // 对于没有代表特定用户的内部服务调用，设置系统级认证
            // 这种情况通常发生在：登录、注册、健康检查等系统级操作
            setSystemServiceContext(callerService != null ? callerService : "internal-service");
            log.debug("Internal service call authenticated from: {} (system level)", callerService);
        }
    }

    /**
     * 处理直接外部访问（降级模式）
     * 生产环境建议禁用此模式
     */
    private void handleDirectExternalRequest(HttpServletRequest request) throws AuthenticationException {
        // 生产环境直接拒绝
        if (!authProperties.isEnableDirectAccess()) {
            throw new AuthenticationException("Direct access denied, please use gateway");
        }

        String path = request.getRequestURI();
        if (isInWhitelist(path)) {
            log.debug("Direct access whitelist: {}", path);
            return;
        }

        // 降级模式：完整JWT校验
        String token = extractToken(request);
        if (StrUtil.isBlank(token)) {
            throw new AuthenticationException("Missing token for direct access");
        }

        if (!jwtUtils.validateToken(token)) {
            throw new AuthenticationException("Invalid token for direct access");
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        String username = jwtUtils.getUsernameFromToken(token);
        List<String> permissions = redisPermissionService.getAllUserAuthorities(userId);

        setSecurityContext(userId, username, permissions != null ? permissions : Collections.emptyList());

        log.warn("Direct external access for user: {} (should use gateway in production)", username);
    }

    /**
     * 设置Spring Security上下文
     */
    private void setSecurityContext(Long userId, String username, List<String> permissions) {
        Collection<GrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(username, null, authorities);
        authToken.setDetails(userId); // 存储用户ID供后续使用

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /**
     * 为系统级服务调用设置认证上下文
     * 用于没有特定用户上下文的内部服务调用（如登录、注册、健康检查等）
     */
    private void setSystemServiceContext(String serviceName) {
        // 为系统级调用授予基础权限
        List<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_SYSTEM_SERVICE"),
            new SimpleGrantedAuthority("SYSTEM:INTERNAL:ACCESS")
        );

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(serviceName, null, authorities);
        authToken.setDetails("SYSTEM_SERVICE"); // 标识为系统服务

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /**
     * 验证网关密钥的有效性（防重放攻击）
     */
    private boolean isValidGatewaySecret(String secret) {
        if (StrUtil.isBlank(secret)) return false;

        long currentTime = System.currentTimeMillis() / 1000;
        for (int i = 0; i < authProperties.getGatewaySecretValidSeconds(); i++) {
            String expectedSecret = DigestUtil.md5Hex((currentTime - i) + ":" + authProperties.getGatewaySecretKey());
            if (secret.equals(expectedSecret)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 提取JWT Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
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
     * 处理认证异常
     */
    private void handleAuthenticationException(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}