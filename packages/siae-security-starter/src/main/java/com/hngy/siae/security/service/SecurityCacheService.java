package com.hngy.siae.security.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 安全缓存服务接口
 * 
 * 统一管理用户权限、角色、Token的Redis缓存操作
 * 
 * @author SIAE开发团队
 */
public interface SecurityCacheService {

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

    // ==================== 权限检查方法 ====================

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

    // ==================== 权限缓存管理方法 ====================

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
     * 清除用户的所有缓存（权限+角色）
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
     * 刷新用户权限缓存（重新加载）
     *
     * @param userId 用户ID
     */
    void refreshUserPermissions(Long userId);

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
     * 刷新用户权限缓存过期时间
     *
     * @param userId 用户ID
     * @param expireTime 新的过期时间
     * @param timeUnit 时间单位
     */
    void refreshUserPermissionsCache(Long userId, long expireTime, TimeUnit timeUnit);

    /**
     * 刷新用户角色缓存过期时间
     *
     * @param userId 用户ID
     * @param expireTime 新的过期时间
     * @param timeUnit 时间单位
     */
    void refreshUserRolesCache(Long userId, long expireTime, TimeUnit timeUnit);

    // ==================== Token管理方法 ====================

    /**
     * 验证token是否有效（JWT格式+Redis存在）
     *
     * @param token JWT token
     * @return true表示token有效，false表示token无效或已过期
     */
    boolean validateToken(String token);

    /**
     * 存储token到Redis
     *
     * @param token JWT token
     * @param userInfo 用户信息
     * @param expireSeconds 过期时间（秒）
     */
    void storeToken(String token, Object userInfo, long expireSeconds);

    /**
     * 从Redis中删除token（登出时使用）
     *
     * @param token JWT token
     */
    void removeToken(String token);

    // ==================== 单点登录（同端互斥）方法 ====================

    /**
     * 存储用户设备token映射（用于同端互斥）
     *
     * @param userId 用户ID
     * @param deviceType 设备类型（web、mobile、desktop）
     * @param token JWT token
     * @param expireSeconds 过期时间（秒）
     */
    void storeUserDeviceToken(Long userId, String deviceType, String token, long expireSeconds);

    /**
     * 获取用户指定设备类型的token
     *
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @return token，如果不存在返回null
     */
    String getUserDeviceToken(Long userId, String deviceType);

    /**
     * 删除用户指定设备类型的token（踢掉该设备）
     *
     * @param userId 用户ID
     * @param deviceType 设备类型
     */
    void removeUserDeviceToken(Long userId, String deviceType);

    /**
     * 踢掉用户同类型设备的旧登录，存储新token
     *
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @param newToken 新token
     * @param expireSeconds 过期时间（秒）
     */
    void kickSameDeviceAndStoreNewToken(Long userId, String deviceType, String newToken, long expireSeconds);
}
