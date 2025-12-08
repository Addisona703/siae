package com.hngy.siae.media.security;

import com.hngy.siae.media.domain.enums.ActorType;
import com.hngy.siae.web.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * 审计日志切面
 * 自动记录带有@AuditLog注解的方法调用
 *
 * @author SIAE Team
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final com.hngy.siae.media.service.IAuditService auditService;

    @AfterReturning(pointcut = "@annotation(com.hngy.siae.media.security.AuditLog)", returning = "result")
    public void logAudit(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            com.hngy.siae.media.security.AuditLog annotation = 
                    signature.getMethod().getAnnotation(com.hngy.siae.media.security.AuditLog.class);

            String tenantId = TenantContext.getTenantId();
            String userId = TenantContext.getUserId();

            if (tenantId == null || userId == null) {
                log.warn("Tenant or user context not set, skip audit log");
                return;
            }

            // 获取请求信息
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String ip = null;
            String userAgent = null;
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ip = WebUtils.getClientIp(request);
                userAgent = request.getHeader("User-Agent");
            }

            // 构建元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("method", signature.getMethod().getName());
            metadata.put("description", annotation.description());

            // 记录审计日志
            auditService.log(null, tenantId, ActorType.USER, userId, 
                    annotation.action(), ip, userAgent, metadata);

        } catch (Exception e) {
            log.error("Failed to log audit", e);
        }
    }

}
