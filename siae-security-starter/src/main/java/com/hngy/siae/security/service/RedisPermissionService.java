package com.hngy.siae.security.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis权限服务接口
 *
 * 统一的Redis权限服务接口，整合了权限查询和缓存管理功能
 * 支持用户权限和角色的Redis缓存操作，提供高效的权限查询和缓存管理
 *
 * @author SIAE开发团队
 */
public interface RedisPermissionService {

    // ==================== 权限查询方法 ====================

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限列表，如果缓存不存在或获取失败返回空列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色列表，如果缓存不存在或获取失败返回空列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 获取用户的所有权限（包括角色权限）
     *
     * @param userId 用户ID
     * @return 所有权限列表
     */
    List<String> getAllUserAuthorities(Long userId);

    // ==================== 缓存管理方法 ====================

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
     * 清除用户的所有权限缓存
     *
     * @param userId 用户ID
     */
    void clearUserCache(Long userId);

    /**
     * 清除用户权限缓存
     *
     * @param userId 用户ID
     */
    void clearUserPermissions(Long userId);

    /**
     * 清除用户角色缓存
     *
     * @param userId 用户ID
     */
    void clearUserRoles(Long userId);

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
