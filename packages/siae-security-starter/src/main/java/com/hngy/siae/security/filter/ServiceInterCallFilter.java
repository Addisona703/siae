package com.hngy.siae.security.filter;

import cn.hutool.core.text.AntPathMatcher;
import com.hngy.siae.security.properties.SecurityProperties;
import com.hngy.siae.core.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 服务间调用过滤器
 *
 * ⚠️ 已废弃：该过滤器已被 ServiceAuthenticationFilter 替代
 * 新版本实现了JWT网关优化方案，统一处理所有认证场景
 *
 * 通用的服务间调用验证过滤器，用于验证SIAE系统内部服务间的调用。
 * 执行顺序：在JwtAuthenticationFilter之后执行，只处理已被识别为服务间调用的请求。
 *
 * 主要功能：
 * 1. 服务身份验证：验证调用方和被调用方都是SIAE系统中的合法服务
 * 2. 服务Token验证：验证服务间调用使用的JWT token
 * 3. 监控和审计：记录服务间调用的详细信息
 *
 * @author SIAE开发团队
 * @deprecated 使用 com.hngy.siae.security.filter.ServiceAuthenticationFilter 替代
 */
@Slf4j
// @Component // 注释掉，禁用此过滤器
@ConditionalOnProperty(prefix = "siae.security.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Deprecated
public class ServiceInterCallFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final SecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    /**
     * SIAE系统中的合法服务列表
     */
    private static final List<String> VALID_SERVICES = Arrays.asList(
            "siae-auth", "siae-user", "siae-content", "siae-gateway"
    );
    
    /**
     * 有效的服务间调用token标识
     */
    private static final List<String> VALID_SERVICE_TOKENS = Arrays.asList(
            "auth-to-user", "auth-to-content", "user-to-auth", "content-to-auth", "gateway-to-service"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 检测是否为服务间调用（只有JwtAuthenticationFilter放行的服务间调用才会到达这里）
        if (isServiceInterCall(request)) {
            log.debug("=== 服务间调用验证开始 ===");

            // 执行服务间调用验证
            boolean isValidServiceCall = validateServiceInterCall(request);
            
            if (isValidServiceCall) {
                log.debug("✅ 服务间调用验证通过");
            } else {
                log.warn("⚠️ 服务间调用验证失败，但不阻断请求（由标准认证流程处理）");
            }
            
            log.debug("=== 服务间调用验证结束 ===");
        }

        // 继续执行过滤器链
        log.info("服务间调用验证通过，继续执行过滤器链");
        filterChain.doFilter(request, response);
    }
    
    /**
     * 检测是否为服务间调用
     * 
     * @param request HTTP请求
     * @return true表示是服务间调用
     */
    private boolean isServiceInterCall(HttpServletRequest request) {
        String serviceCallHeader = request.getHeader("X-Service-Call");
        String serviceNameHeader = request.getHeader("X-Service-Name");
        String userAgent = request.getHeader("User-Agent");
        
        // 检查明确的服务间调用标识
        if ("true".equals(serviceCallHeader)) {
            return true;
        }
        
        // 检查服务名称标识
        if (serviceNameHeader != null && serviceNameHeader.startsWith("siae-")) {
            return true;
        }
        
        // 检查User-Agent中的服务标识
        if (userAgent != null && (userAgent.contains("siae-") || userAgent.contains("Feign"))) {
            return true;
        }

        return false;
    }
    
    /**
     * 验证服务间调用
     * 
     * @param request HTTP请求
     * @return true表示验证通过
     */
    private boolean validateServiceInterCall(HttpServletRequest request) {
        try {
            // 1. 验证服务身份
            if (!validateServiceIdentity(request)) {
                return false;
            }
            
            // 2. 验证服务Token
            if (!validateServiceToken(request)) {
                return false;
            }
            
            // 3. 记录服务间调用信息
            logServiceCallInfo(request);
            
            return true;
            
        } catch (Exception e) {
            log.error("服务间调用验证过程中发生异常", e);
            return false;
        }
    }
    
    /**
     * 验证服务身份
     * 
     * @param request HTTP请求
     * @return true表示服务身份合法
     */
    private boolean validateServiceIdentity(HttpServletRequest request) {
        String serviceNameHeader = request.getHeader("X-Service-Name");
        String userAgent = request.getHeader("User-Agent");
        
        // 验证调用方服务身份
        String callerService = extractCallerService(serviceNameHeader, userAgent);
        if (callerService == null || !VALID_SERVICES.contains(callerService)) {
            log.warn("无效的调用方服务: {}", callerService);
            return false;
        }
        
        log.debug("调用方服务验证通过: {}", callerService);
        return true;
    }
    
    /**
     * 提取调用方服务名称
     * 
     * @param serviceNameHeader X-Service-Name请求头
     * @param userAgent User-Agent请求头
     * @return 调用方服务名称
     */
    private String extractCallerService(String serviceNameHeader, String userAgent) {
        // 优先从X-Service-Name头获取
        if (serviceNameHeader != null && serviceNameHeader.startsWith("siae-")) {
            return serviceNameHeader;
        }
        
        // 从User-Agent中提取
        if (userAgent != null) {
            for (String service : VALID_SERVICES) {
                if (userAgent.contains(service)) {
                    return service;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 验证服务Token
     * 
     * @param request HTTP请求
     * @return true表示token验证通过
     */
    private boolean validateServiceToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("服务间调用缺少有效的Authorization头");
            return false;
        }
        
        String token = authHeader.substring(7);
        
        try {
            // 1. 验证token格式和签名
            if (!jwtUtils.validateToken(token)) {
                log.warn("服务间调用token格式或签名验证失败");
                return false;
            }
            
            // 2. 验证token中的服务标识
            var claims = jwtUtils.parseToken(token);
            String serviceIdentifier = claims.get("service", String.class);
            
            if (serviceIdentifier == null || !VALID_SERVICE_TOKENS.contains(serviceIdentifier)) {
                log.warn("无效的服务间调用token标识: {}", serviceIdentifier);
                return false;
            }
            
            log.debug("服务间调用token验证通过: {}", serviceIdentifier);
            return true;
            
        } catch (Exception e) {
            log.warn("服务间调用token验证异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 记录服务间调用信息
     * 
     * @param request HTTP请求
     */
    private void logServiceCallInfo(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String serviceCallHeader = request.getHeader("X-Service-Call");
        String serviceNameHeader = request.getHeader("X-Service-Name");
        String userAgent = request.getHeader("User-Agent");
        
        log.info("=== 服务间调用监控 ===");
        log.info("请求路径: {} {}", method, path);
        log.info("调用标识: X-Service-Call={}", serviceCallHeader);
        log.info("调用方服务: X-Service-Name={}", serviceNameHeader);
        log.info("User-Agent: {}", userAgent);
        log.info("验证状态: 通过");
        
        // 可以在这里添加更多的监控逻辑
        // 例如：调用次数统计、性能监控、安全审计等
    }
    
    /**
     * 判断当前请求是否应该被此过滤器处理
     * 
     * @param request HTTP请求
     * @return true表示应该处理
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getRequestURI();

        // 检查是否在白名单中，如果在白名单中则跳过此过滤器
        if (isWhitelistPath(requestPath)) {
            log.debug("白名单路径跳过服务间调用验证: {}", requestPath);
            return true;
        }

        // 只处理服务间调用，其他请求跳过
        return !isServiceInterCall(request);
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelistPath(String path) {
        return securityProperties.getWhitelistPaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
