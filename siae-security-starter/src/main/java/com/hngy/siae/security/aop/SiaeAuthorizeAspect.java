package com.hngy.siae.security.aop;

import cn.hutool.core.util.StrUtil;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.AuthResultCodeEnum;
import com.hngy.siae.core.result.CommonResultCodeEnum;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
            AssertUtils.fail(CommonResultCodeEnum.UNAUTHORIZED);
        }

        // 添加详细的权限调试日志
        log.warn("用户权限检查 - 用户: {}, 所有权限: {}", auth.getName(),
                auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toList()));

        // 管理员权限检查 - 根据数据库定义的角色
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ROOT".equals(a.getAuthority()) ||
                        "ROLE_ADMIN".equals(a.getAuthority()));

        if (isSuperAdmin) {
            log.warn("超级管理员访问，直接放行 - 用户: {}", auth.getName());
            return joinPoint.proceed();
        }

        // 创建SecurityExpressionRoot，注入当前认证信息,使用security的SecurityExpressionRoot方法
        SecurityExpressionRoot root = new SecurityExpressionRoot(auth) {};

        // 创建SpEL上下文，将root对象设置为rootObject
        StandardEvaluationContext context = new StandardEvaluationContext(root);

        String expr = siaeAuthorize.value();
        if (StrUtil.isBlank(expr)) {
            log.warn("权限表达式为空，拒绝访问");
            AssertUtils.fail(CommonResultCodeEnum.FORBIDDEN);
        }

        log.debug("评估权限表达式: {}", expr);

        Boolean allowed = parser.parseExpression(expr).getValue(context, Boolean.class);

        if (Boolean.TRUE.equals(allowed)) {
            log.debug("权限校验通过");
            return joinPoint.proceed();
        }

        log.warn("用户 {} 权限不足，表达式: {}", auth.getName(), expr);
        AssertUtils.fail(AuthResultCodeEnum.PERMISSION_DENIED);
        return null;
    }
}
