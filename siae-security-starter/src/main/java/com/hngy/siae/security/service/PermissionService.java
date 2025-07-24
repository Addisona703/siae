package com.hngy.siae.security.service;

import java.util.List;

/**
 * 权限服务接口
 * 
 * 用于在各个服务中获取用户权限信息，支持多种实现方式
 * 
 * @author SIAE开发团队
 */
public interface PermissionService {
    
    /**
     * 获取用户权限列表
     * 
     * @param userId 用户ID
     * @return 权限列表，如果获取失败返回空列表
     */
    List<String> getUserPermissions(Long userId);
    
    /**
     * 获取用户角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表，如果获取失败返回空列表
     */
    List<String> getUserRoles(Long userId);
    
    /**
     * 获取用户的所有权限（包括角色权限）
     * 
     * @param userId 用户ID
     * @return 所有权限列表
     */
    List<String> getAllUserAuthorities(Long userId);
    
    /**
     * 检查用户是否拥有指定权限
     * 
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, String permission);
    
    /**
     * 检查用户是否拥有指定角色
     * 
     * @param userId 用户ID
     * @param role 角色标识
     * @return 是否拥有角色
     */
    boolean hasRole(Long userId, String role);
    
    /**
     * 检查用户是否拥有任意一个指定权限
     * 
     * @param userId 用户ID
     * @param permissions 权限列表
     * @return 是否拥有任意权限
     */
    boolean hasAnyPermission(Long userId, String... permissions);
    
    /**
     * 检查用户是否拥有所有指定权限
     * 
     * @param userId 用户ID
     * @param permissions 权限列表
     * @return 是否拥有所有权限
     */
    boolean hasAllPermissions(Long userId, String... permissions);
    
    /**
     * 刷新用户权限缓存
     * 
     * @param userId 用户ID
     */
    void refreshUserPermissions(Long userId);
    
    /**
     * 清除用户权限缓存
     * 
     * @param userId 用户ID
     */
    void clearUserPermissions(Long userId);
}
