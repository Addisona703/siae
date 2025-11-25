package com.hngy.siae.attendance.aspect;

import com.hngy.siae.attendance.annotation.RateLimit;
import com.hngy.siae.attendance.config.RateLimitConfig;
import com.hngy.siae.attendance.enums.AttendanceResultCodeEnum;
import com.hngy.siae.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * 限流切面
 * 基于 Redis + Lua 脚本实现分布式限流
 *
 * @author SIAE Team
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RateLimitConfig rateLimitConfig;

    /**
     * Lua 脚本：实现滑动窗口限流
     * 使用 Redis 的 ZSET 数据结构，score 为时间戳
     */
    private static final String RATE_LIMIT_SCRIPT =
            "local key = KEYS[1]\n" +
            "local now = tonumber(ARGV[1])\n" +
            "local window = tonumber(ARGV[2])\n" +
            "local limit = tonumber(ARGV[3])\n" +
            "local clearBefore = now - window\n" +
            "redis.call('ZREMRANGEBYSCORE', key, 0, clearBefore)\n" +
            "local amount = redis.call('ZCARD', key)\n" +
            "if amount < limit then\n" +
            "    redis.call('ZADD', key, now, now)\n" +
            "    redis.call('EXPIRE', key, window)\n" +
            "    return 1\n" +
            "else\n" +
            "    return 0\n" +
            "end";

    @Around("@annotation(com.hngy.siae.attendance.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 检查是否启用限流
        if (!rateLimitConfig.isEnabled()) {
            return joinPoint.proceed();
        }

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 获取限流参数
        int permits = rateLimit.permits();
        int window = rateLimit.window();

        // 如果注解中没有指定，使用配置文件中的默认值
        if (permits == -1 || window == -1) {
            RateLimitConfig.RateLimitRule defaultRule = getDefaultRule(method.getName());
            if (defaultRule != null) {
                if (permits == -1) {
                    permits = defaultRule.getPermits();
                }
                if (window == -1) {
                    window = defaultRule.getWindow();
                }
            } else {
                // 如果没有配置，使用默认值
                permits = permits == -1 ? 100 : permits;
                window = window == -1 ? 60 : window;
            }
        }

        // 构建限流 key
        String rateLimitKey = buildRateLimitKey(rateLimit);

        // 执行限流检查
        boolean allowed = checkRateLimit(rateLimitKey, permits, window);

        if (!allowed) {
            log.warn("请求被限流: key={}, permits={}, window={}s", rateLimitKey, permits, window);
            throw new BusinessException(AttendanceResultCodeEnum.RATE_LIMIT_EXCEEDED);
        }

        // 继续执行
        return joinPoint.proceed();
    }

    /**
     * 构建限流 key
     */
    private String buildRateLimitKey(RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder(rateLimit.key());

        switch (rateLimit.type()) {
            case USER:
                // 按用户限流：从请求中获取用户ID
                Long userId = getCurrentUserId();
                if (userId != null) {
                    keyBuilder.append(":user:").append(userId);
                } else {
                    // 如果无法获取用户ID，降级为IP限流
                    String ip = getCurrentIp();
                    keyBuilder.append(":ip:").append(ip);
                }
                break;

            case IP:
                // 按IP限流
                String ip = getCurrentIp();
                keyBuilder.append(":ip:").append(ip);
                break;

            case GLOBAL:
                // 全局限流
                keyBuilder.append(":global");
                break;
        }

        return keyBuilder.toString();
    }

    /**
     * 执行限流检查
     */
    private boolean checkRateLimit(String key, int permits, int window) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(RATE_LIMIT_SCRIPT);
            script.setResultType(Long.class);

            long now = System.currentTimeMillis();
            List<String> keys = Collections.singletonList(key);

            Long result = redisTemplate.execute(
                    script,
                    keys,
                    String.valueOf(now),
                    String.valueOf(window * 1000), // 转换为毫秒
                    String.valueOf(permits)
            );

            return result != null && result == 1;
        } catch (Exception e) {
            log.error("限流检查失败: key={}", key, e);
            // 限流检查失败时，允许请求通过（降级策略）
            return true;
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            // TODO: 从安全上下文或请求中获取用户ID
            // 这里需要根据实际的认证方式实现
            // 示例：
            // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            //     return ((UserDetails) authentication.getPrincipal()).getUserId();
            // }
            return null;
        } catch (Exception e) {
            log.warn("获取当前用户ID失败", e);
            return null;
        }
    }

    /**
     * 获取当前请求IP
     */
    private String getCurrentIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("获取当前请求IP失败", e);
        }
        return "unknown";
    }

    /**
     * 根据方法名获取默认限流规则
     */
    private RateLimitConfig.RateLimitRule getDefaultRule(String methodName) {
        if (methodName.contains("checkIn") || methodName.contains("checkin")) {
            return rateLimitConfig.getCheckIn();
        } else if (methodName.contains("checkOut") || methodName.contains("checkout")) {
            return rateLimitConfig.getCheckOut();
        } else if (methodName.contains("export")) {
            return rateLimitConfig.getExport();
        } else if (methodName.contains("query") || methodName.contains("list") || methodName.contains("get")) {
            return rateLimitConfig.getQuery();
        }
        return null;
    }
}
