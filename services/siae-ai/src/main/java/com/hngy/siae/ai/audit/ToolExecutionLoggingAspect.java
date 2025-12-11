package com.hngy.siae.ai.audit;

import com.hngy.siae.ai.security.PermissionChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具执行日志切面
 * <p>
 * 使用AOP拦截工具方法调用，记录执行时间和结果。
 * 自动记录所有标注为@Tool的方法执行情况。
 * <p>
 * Requirements: 5.3
 *
 * @author SIAE Team
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ToolExecutionLoggingAspect {

    private final AiAuditService auditService;
    private final PermissionChecker permissionChecker;

    /**
     * 拦截所有Spring AI工具函数的执行
     * <p>
     * 记录工具名称、参数、执行时间和结果
     */
    @Around("execution(java.util.function.Function *(..)) && " +
            "(@within(org.springframework.context.annotation.Configuration) || " +
            "@within(org.springframework.stereotype.Component)) && " +
            "bean(*Tool)")
    public Object logToolExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String toolName = extractToolName(joinPoint);
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 获取用户信息
            Long userId = permissionChecker.getCurrentUserId();
            String username = permissionChecker.getCurrentUsername();
            
            // 提取参数
            Map<String, Object> parameters = extractParameters(joinPoint);
            
            // 记录执行
            auditService.logToolExecution(toolName, parameters, result, executionTime, userId, username);
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 获取用户信息
            Long userId = permissionChecker.getCurrentUserId();
            String username = permissionChecker.getCurrentUsername();
            
            // 提取参数
            Map<String, Object> parameters = extractParameters(joinPoint);
            
            // 记录错误
            auditService.logToolExecutionError(toolName, parameters, e.getMessage(), 
                    executionTime, userId, username);
            
            throw e;
        }
    }

    /**
     * 从JoinPoint提取工具名称
     */
    private String extractToolName(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        // 工具方法通常返回Function，方法名就是工具名
        return methodName;
    }

    /**
     * 从JoinPoint提取参数
     */
    private Map<String, Object> extractParameters(ProceedingJoinPoint joinPoint) {
        Map<String, Object> parameters = new HashMap<>();
        Object[] args = joinPoint.getArgs();
        
        if (args.length > 0) {
            // 通常第一个参数是请求对象
            Object request = args[0];
            if (request != null) {
                parameters.put("request", request.toString());
            }
        }
        
        return parameters;
    }
}
