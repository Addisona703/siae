package com.hngy.siae.security.service.impl;

import com.hngy.siae.security.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 权限服务的降级实现
 * 
 * 当Redis不可用或被禁用时使用此实现，提供基础的权限检查功能
 * 可以保证系统在Redis不可用时仍能正常运行
 * 
 * @author KEYKB
 */
@Slf4j
@Service
@ConditionalOnMissingBean(RedisPermissionServiceImpl.class)
@ConditionalOnProperty(prefix = "siae.security.permission", name = "fallback-enabled", havingValue = "true", matchIfMissing = true)
public class FallbackPermissionServiceImpl implements PermissionService {
    
    @Override
    public List<String> getUserPermissions(Long userId) {
        log.warn("使用降级权限服务，返回空权限列表，用户ID: {}", userId);
        return Collections.emptyList();
    }
    
    @Override
    public List<String> getUserRoles(Long userId) {
        log.warn("使用降级权限服务，返回空角色列表，用户ID: {}", userId);
        return Collections.emptyList();
    }
    
    @Override
    public List<String> getAllUserAuthorities(Long userId) {
        log.warn("使用降级权限服务，返回空权限列表，用户ID: {}", userId);
        return Collections.emptyList();
    }
    
    @Override
    public boolean hasPermission(Long userId, String permission) {
        log.warn("使用降级权限服务，权限检查返回false，用户ID: {}, 权限: {}", userId, permission);
        return false;
    }
    
    @Override
    public boolean hasRole(Long userId, String role) {
        log.warn("使用降级权限服务，角色检查返回false，用户ID: {}, 角色: {}", userId, role);
        return false;
    }
    
    @Override
    public boolean hasAnyPermission(Long userId, String... permissions) {
        log.warn("使用降级权限服务，权限检查返回false，用户ID: {}, 权限: {}", userId, String.join(",", permissions));
        return false;
    }
    
    @Override
    public boolean hasAllPermissions(Long userId, String... permissions) {
        log.warn("使用降级权限服务，权限检查返回false，用户ID: {}, 权限: {}", userId, String.join(",", permissions));
        return false;
    }
    
    @Override
    public void refreshUserPermissions(Long userId) {
        log.warn("使用降级权限服务，无法刷新权限缓存，用户ID: {}", userId);
    }
    
    @Override
    public void clearUserPermissions(Long userId) {
        log.warn("使用降级权限服务，无法清除权限缓存，用户ID: {}", userId);
    }
}
