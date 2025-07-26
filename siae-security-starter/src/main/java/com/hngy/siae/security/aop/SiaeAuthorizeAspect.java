package com.hngy.siae.security.aop;

import com.hngy.siae.security.annotation.SiaeAuthorize;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class SiaeAuthorizeAspect {

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(siaeAuthorize)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, SiaeAuthorize siaeAuthorize) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            log.warn("用户未认证，拒绝访问");
            throw new AccessDeniedException("用户未认证");
        }

        // 超级管理员直接放行
        boolean isRoot = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ROOT".equals(a.getAuthority()));
        if (isRoot) {
            log.debug("超级管理员访问，直接放行");
            return joinPoint.proceed();
        }

        // 创建SecurityExpressionRoot，注入当前认证信息,使用security的SecurityExpressionRoot方法
        SecurityExpressionRoot root = new SecurityExpressionRoot(auth) {};

        // 创建SpEL上下文，将root对象设置为rootObject
        StandardEvaluationContext context = new StandardEvaluationContext(root);

        String expr = siaeAuthorize.value();
        if (expr == null || expr.trim().isEmpty()) {
            log.warn("权限表达式为空，拒绝访问");
            throw new AccessDeniedException("未授权访问");
        }

        log.debug("评估权限表达式: {}", expr);

        Boolean allowed = parser.parseExpression(expr).getValue(context, Boolean.class);

        if (Boolean.TRUE.equals(allowed)) {
            log.debug("权限校验通过");
            return joinPoint.proceed();
        }

        log.warn("用户 {} 权限不足，表达式: {}", auth.getName(), expr);
        throw new AccessDeniedException("权限不足");
    }
}
