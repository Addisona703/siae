package com.hngy.siae.common.service;

import java.util.List;

/**
 * Redis权限服务接口
 * 
 * 用于在各个服务中获取用户权限信息，支持从Redis缓存中读取
 * 
 * @author KEYKB
 */
public interface RedisPermissionService {
    
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
}
