package com.hngy.siae.auth.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis权限缓存服务接口
 * 
 * 负责管理用户权限和角色的Redis缓存，提供高效的权限查询和缓存管理功能
 * 
 * @author KEYKB
 */
public interface RedisPermissionCacheService {
    
    /**
     * 缓存用户权限到Redis
     * 
     * @param userId 用户ID
     * @param permissions 权限列表
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     */
    void cacheUserPermissions(Long userId, List<String> permissions, long expireTime, TimeUnit timeUnit);
    
    /**
     * 缓存用户角色到Redis
     * 
     * @param userId 用户ID
     * @param roles 角色列表
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     */
    void cacheUserRoles(Long userId, List<String> roles, long expireTime, TimeUnit timeUnit);
    
    /**
     * 从Redis获取用户权限
     * 
     * @param userId 用户ID
     * @return 权限列表，如果缓存不存在返回null
     */
    List<String> getUserPermissions(Long userId);
    
    /**
     * 从Redis获取用户角色
     * 
     * @param userId 用户ID
     * @return 角色列表，如果缓存不存在返回null
     */
    List<String> getUserRoles(Long userId);
    
    /**
     * 清除用户的所有权限缓存
     * 
     * @param userId 用户ID
     */
    void clearUserCache(Long userId);
    
    /**
     * 检查用户权限缓存是否存在
     * 
     * @param userId 用户ID
     * @return true如果缓存存在，false否则
     */
    boolean hasUserPermissionsCache(Long userId);
    
    /**
     * 检查用户角色缓存是否存在
     * 
     * @param userId 用户ID
     * @return true如果缓存存在，false否则
     */
    boolean hasUserRolesCache(Long userId);
    
    /**
     * 刷新用户权限缓存（重新设置过期时间）
     * 
     * @param userId 用户ID
     * @param expireTime 新的过期时间
     * @param timeUnit 时间单位
     */
    void refreshUserPermissionsCache(Long userId, long expireTime, TimeUnit timeUnit);
    
    /**
     * 刷新用户角色缓存（重新设置过期时间）
     * 
     * @param userId 用户ID
     * @param expireTime 新的过期时间
     * @param timeUnit 时间单位
     */
    void refreshUserRolesCache(Long userId, long expireTime, TimeUnit timeUnit);
}
