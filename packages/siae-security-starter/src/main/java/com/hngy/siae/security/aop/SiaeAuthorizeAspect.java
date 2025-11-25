package com.hngy.siae.security.aop;

import cn.hutool.core.util.StrUtil;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.AuthResultCodeEnum;
import com.hngy.siae.core.result.CommonResultCodeEnum;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import com.hngy.siae.security.expression.SiaeSecurityExpressionRoot;
import com.hngy.siae.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "siae.security", 
    name = "enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
public class SiaeAuthorizeAspect {

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final SecurityUtil securityUtil;

    @Around("@annotation(siaeAuthorize)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, SiaeAuthorize siaeAuthorize) throws Throwable {
        Authentication auth = securityUtil.getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            log.warn("用户未认证，拒绝访问");
            AssertUtils.fail(CommonResultCodeEnum.UNAUTHORIZED);
        }

        // 添加详细的权限日志，便于排查
        log.info("用户权限检查开始 - 用户: {}, 所有权限: {}", auth.getName(),
                auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toList()));

        // 超级管理员权限检查 - 使用 SecurityUtil 统一判断
        boolean isSuperAdmin = securityUtil.isSuperAdmin();
        log.info("超级管理员检查结果: {}, 用户: {}", isSuperAdmin, auth.getName());
        
        if (isSuperAdmin) {
            log.info("超级管理员(ROLE_ROOT)访问，直接放行 - 用户: {}", auth.getName());
            return joinPoint.proceed();
        }

        // 创建自定义的SecurityExpressionRoot，注入当前认证信息和SecurityUtil
        // SecurityUtil提供实际的权限检查逻辑，ExpressionRoot只是SpEL适配器
        SiaeSecurityExpressionRoot root = new SiaeSecurityExpressionRoot(auth, securityUtil);

        // 创建SpEL上下文，将root对象设置为rootObject
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        
        // 方式2: 可以在上下文中注册额外的变量或Bean
        // 这样就可以在SpEL表达式中使用 #beanName.method() 的方式调用
        // 例如: @SiaeAuthorize("#securityHelper.canAccessRecord(#recordId)")
        // context.setVariable("securityHelper", securityHelperBean);

        String expr = siaeAuthorize.value();
        if (StrUtil.isBlank(expr)) {
            log.warn("权限表达式为空，拒绝访问");
            AssertUtils.fail(CommonResultCodeEnum.FORBIDDEN);
        }

        log.info("评估权限表达式: {}，用户: {}", expr, auth.getName());

        Boolean allowed = parser.parseExpression(expr).getValue(context, Boolean.class);

        if (Boolean.TRUE.equals(allowed)) {
            log.info("权限校验通过 - 用户: {}, 表达式: {}", auth.getName(), expr);
            return joinPoint.proceed();
        }

        log.warn("用户 {} 权限不足，表达式: {}", auth.getName(), expr);
        AssertUtils.fail(AuthResultCodeEnum.PERMISSION_DENIED);
        return null;
    }
}
