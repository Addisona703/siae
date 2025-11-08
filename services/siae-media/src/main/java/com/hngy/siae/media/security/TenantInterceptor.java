package com.hngy.siae.media.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 租户拦截器
 * 从请求头中提取租户信息并设置到上下文
 * <p>
 * 职责：
 * - 提取租户ID和用户ID（来自网关或内部调用）
 * - 设置到 ThreadLocal 上下文供业务层使用
 * - 请求结束后清理上下文
 * <p>
 * 注意：
 * - 此拦截器在 ServiceAuthenticationFilter 之后执行
 * - 专注于业务层面的租户上下文管理，不涉及安全认证
 *
 * @author SIAE Team
 */
@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_ID_HEADER = "X-Tenant-Id";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader(TENANT_ID_HEADER);
        String userId = request.getHeader(USER_ID_HEADER);

        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
            log.debug("租户上下文已设置: tenantId={}", tenantId);
        } else {
            log.debug("请求头中未找到租户ID: {}", request.getRequestURI());
        }
        
        if (userId != null) {
            TenantContext.setUserId(userId);
            log.debug("用户上下文已设置: userId={}", userId);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        TenantContext.clear();
    }

}
