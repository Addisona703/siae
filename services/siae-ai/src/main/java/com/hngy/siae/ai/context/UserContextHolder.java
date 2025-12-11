package com.hngy.siae.ai.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用户上下文持有者
 * <p>
 * 用于在 AI 工具函数执行时传递用户认证信息
 * 解决 Spring AI 异步执行工具函数时 SecurityContext 丢失的问题
 * 
 * @author SIAE Team
 */
@Slf4j
public class UserContextHolder {
    
    private static final ThreadLocal<SecurityContext> contextHolder = new ThreadLocal<>();
    
    /**
     * 设置当前线程的用户上下文
     */
    public static void setContext(SecurityContext context) {
        contextHolder.set(context);
        // 同时设置到 SecurityContextHolder 中
        SecurityContextHolder.setContext(context);
        log.debug("User context set for thread: {}", Thread.currentThread().getName());
    }
    
    /**
     * 获取当前线程的用户上下文
     */
    public static SecurityContext getContext() {
        SecurityContext context = contextHolder.get();
        if (context == null) {
            context = SecurityContextHolder.getContext();
        }
        return context;
    }
    
    /**
     * 获取当前用户的认证信息
     */
    public static Authentication getAuthentication() {
        SecurityContext context = getContext();
        return context != null ? context.getAuthentication() : null;
    }
    
    /**
     * 清除当前线程的用户上下文
     */
    public static void clear() {
        contextHolder.remove();
        SecurityContextHolder.clearContext();
        log.debug("User context cleared for thread: {}", Thread.currentThread().getName());
    }
    
    /**
     * 在指定的上下文中执行操作
     */
    public static <T> T executeWithContext(SecurityContext context, java.util.function.Supplier<T> action) {
        SecurityContext originalContext = getContext();
        try {
            setContext(context);
            return action.get();
        } finally {
            if (originalContext != null) {
                setContext(originalContext);
            } else {
                clear();
            }
        }
    }
    
    /**
     * 在指定的上下文中执行操作（无返回值）
     */
    public static void executeWithContext(SecurityContext context, Runnable action) {
        executeWithContext(context, () -> {
            action.run();
            return null;
        });
    }
}