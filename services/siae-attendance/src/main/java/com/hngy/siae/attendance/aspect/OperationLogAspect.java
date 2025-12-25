package com.hngy.siae.attendance.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.attendance.annotation.OperationLog;
import com.hngy.siae.attendance.service.IOperationLogService;
import com.hngy.siae.security.utils.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 
 * <p>拦截带有@OperationLog注解的方法，自动记录操作日志</p>
 * 
 * @author SIAE Team
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final IOperationLogService operationLogService;
    private final ObjectMapper objectMapper;
    private final SecurityUtil securityUtil;
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 环绕通知：拦截带有@OperationLog注解的方法
     * 
     * @param joinPoint 切点
     * @param operationLog 操作日志注解
     * @return 方法执行结果
     * @throws Throwable 执行异常
     */
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 创建操作日志对象
        com.hngy.siae.attendance.entity.OperationLog logEntity = new com.hngy.siae.attendance.entity.OperationLog();
        
        // 获取当前用户ID，如果未认证则跳过日志记录
        Long userId = securityUtil.getCurrentUserIdOrNull();
        if (userId == null) {
            log.warn("未获取到用户ID，跳过操作日志记录");
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw e;
            }
        }
        logEntity.setUserId(userId);
        
        // 设置操作类型和模块
        logEntity.setOperationType(operationLog.type());
        logEntity.setOperationModule(operationLog.module());
        
        // 解析操作描述（支持SpEL表达式）
        String description = parseDescription(operationLog.description(), joinPoint);
        logEntity.setOperationDesc(description);
        
        // 获取HTTP请求信息
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            logEntity.setRequestMethod(request.getMethod());
            logEntity.setRequestUrl(request.getRequestURI());
            logEntity.setIpAddress(getIpAddress(request));
            logEntity.setUserAgent(request.getHeader("User-Agent"));
            
            // 记录请求参数
            if (operationLog.recordParams()) {
                String params = getRequestParams(joinPoint);
                logEntity.setRequestParams(params);
            }
        }
        
        // 执行目标方法
        Object result = null;
        boolean success = true;
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
            
            // 记录响应结果
            if (operationLog.recordResult() && result != null) {
                try {
                    String resultJson = objectMapper.writeValueAsString(result);
                    // 限制响应结果长度，避免存储过大的数据
                    if (resultJson.length() > 5000) {
                        resultJson = resultJson.substring(0, 5000) + "... (truncated)";
                    }
                    logEntity.setResponseResult(resultJson);
                } catch (Exception e) {
                    log.warn("序列化响应结果失败", e);
                }
            }
            
        } catch (Throwable e) {
            success = false;
            errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.length() > 1000) {
                errorMessage = errorMessage.substring(0, 1000) + "... (truncated)";
            }
            logEntity.setErrorMessage(errorMessage);
            throw e;
        } finally {
            // 计算执行时长
            long executionTime = System.currentTimeMillis() - startTime;
            logEntity.setExecutionTime((int) executionTime);
            logEntity.setStatus(success);
            logEntity.setCreatedAt(LocalDateTime.now());
            
            // 异步保存日志
            operationLogService.saveAsync(logEntity);
            
            log.info("操作日志记录: type={}, module={}, userId={}, success={}, time={}ms", 
                    operationLog.type(), operationLog.module(), userId, success, executionTime);
        }
        
        return result;
    }

    /**
     * 解析操作描述（支持SpEL表达式）
     * 
     * <p>只有当描述包含SpEL表达式标记（#变量引用）时才进行解析，
     * 否则直接返回原字符串，避免普通文本被误解析</p>
     * 
     * @param description 描述模板
     * @param joinPoint 切点
     * @return 解析后的描述
     */
    private String parseDescription(String description, ProceedingJoinPoint joinPoint) {
        if (description == null || description.isEmpty()) {
            return "";
        }
        
        // 如果不包含SpEL表达式标记，直接返回原字符串
        if (!containsSpelExpression(description)) {
            return description;
        }
        
        try {
            // 创建SpEL上下文
            EvaluationContext context = new StandardEvaluationContext();
            
            // 获取方法参数
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            // 将参数添加到上下文
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            
            // 解析表达式
            return parser.parseExpression(description).getValue(context, String.class);
        } catch (Exception e) {
            log.warn("解析操作描述失败: {}", description, e);
            return description;
        }
    }
    
    /**
     * 检查字符串是否包含SpEL表达式标记
     * 
     * @param text 待检查的字符串
     * @return 是否包含SpEL表达式
     */
    private boolean containsSpelExpression(String text) {
        // 检查是否包含 #变量名 或 #{} 表达式
        return text.contains("#") || text.contains("${");
    }

    /**
     * 获取请求参数
     * 
     * @param joinPoint 切点
     * @return 请求参数JSON字符串
     */
    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }
            
            // 过滤掉HttpServletRequest、HttpServletResponse等对象
            Object[] filteredArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof HttpServletRequest || 
                    args[i] instanceof jakarta.servlet.http.HttpServletResponse) {
                    filteredArgs[i] = null;
                } else {
                    filteredArgs[i] = args[i];
                }
            }
            
            String paramsJson = objectMapper.writeValueAsString(filteredArgs);
            
            // 限制参数长度
            if (paramsJson.length() > 5000) {
                paramsJson = paramsJson.substring(0, 5000) + "... (truncated)";
            }
            
            return paramsJson;
        } catch (Exception e) {
            log.warn("序列化请求参数失败", e);
            return null;
        }
    }

    /**
     * 获取HttpServletRequest对象
     * 
     * @return HttpServletRequest对象，如果不在Web环境中则返回null
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 对于多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
