package com.hngy.siae.ai.security;

import com.hngy.siae.ai.exception.AiException;
import com.hngy.siae.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 权限检查器
 * <p>
 * 为AI工具函数提供权限检查功能
 * 确保用户只能访问其有权限的数据
 * 基于 siae-security-starter 的 SecurityUtil 实现
 * <p>
 * Requirements: 7.1, 7.2, 7.3
 * 
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionChecker {
    
    private final SecurityUtil securityUtil;
    
    /**
     * 检查用户是否已认证
     * 如果未认证，抛出异常
     */
    public void requireAuthenticated() {
        Authentication auth = securityUtil.getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user attempted to access protected resource");
            throw AiException.authenticationRequired();
        }
    }
    
    /**
     * 检查用户是否拥有指定权限
     * 如果没有权限，抛出异常
     * 
     * @param permission 所需权限
     */
    public void requirePermission(String permission) {
        requireAuthenticated();
        
        if (!securityUtil.hasPermission(permission)) {
            log.warn("User {} lacks required permission: {}", 
                    getCurrentUsername(), permission);
            throw AiException.permissionDenied();
        }
    }
    
    /**
     * 检查用户是否拥有任意一个权限
     * 如果没有任何权限，抛出异常
     * 
     * @param permissions 所需权限列表
     */
    public void requireAnyPermission(String... permissions) {
        requireAuthenticated();
        
        if (!securityUtil.hasAnyPermission(permissions)) {
            log.warn("User {} lacks any of required permissions: {}", 
                    getCurrentUsername(), String.join(", ", permissions));
            throw AiException.permissionDenied();
        }
    }
    
    /**
     * 检查用户是否拥有指定角色
     * 如果没有角色，抛出异常
     * 
     * @param role 所需角色
     */
    public void requireRole(String role) {
        requireAuthenticated();
        
        if (!securityUtil.hasAnyRole(role)) {
            log.warn("User {} lacks required role: {}", 
                    getCurrentUsername(), role);
            throw AiException.permissionDenied();
        }
    }
    
    /**
     * 检查用户是否可以查询成员信息
     * 规则：所有已认证用户都可以查询成员信息
     */
    public void checkMemberQueryPermission() {
        requireAuthenticated();
        log.debug("User {} authorized to query member information", 
                getCurrentUsername());
    }
    
    /**
     * 检查用户是否可以查询获奖信息
     * 规则：所有已认证用户都可以查询获奖信息
     */
    public void checkAwardQueryPermission() {
        requireAuthenticated();
        log.debug("User {} authorized to query award information", 
                getCurrentUsername());
    }
    
    /**
     * 检查用户是否可以查询统计信息
     * 规则：需要管理员权限或统计查看权限
     */
    public void checkStatisticsQueryPermission() {
        requireAuthenticated();
        
        // 管理员或拥有统计查看权限的用户可以查询
        if (securityUtil.isAdmin() || 
            securityUtil.isSuperAdmin() ||
            securityUtil.hasPermission("STATISTICS:VIEW")) {
            log.debug("User {} authorized to query statistics", getCurrentUsername());
            return;
        }
        
        log.warn("User {} lacks permission to query statistics", getCurrentUsername());
        throw AiException.permissionDenied();
    }
    
    /**
     * 获取当前用户ID
     * 用于记录审计日志
     */
    public Long getCurrentUserId() {
        return securityUtil.getCurrentUserIdOrNull();
    }
    
    /**
     * 获取当前用户名
     * 用于记录审计日志
     */
    public String getCurrentUsername() {
        Authentication auth = securityUtil.getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "ANONYMOUS";
    }
}
