package com.hngy.siae.auth.service;

import com.hngy.siae.auth.dto.PermissionCreateRequest;
import com.hngy.siae.auth.dto.PermissionResponse;

import java.util.List;

/**
 * 权限服务接口
 * 
 * @author KEYKB
 */
public interface PermissionService {
    
    /**
     * 创建权限
     *
     * @param request 创建权限请求
     * @return 权限响应
     */
    PermissionResponse createPermission(PermissionCreateRequest request);
    
    /**
     * 获取权限列表
     *
     * @return 权限列表
     */
    List<PermissionResponse> getPermissions();
    
    /**
     * 获取权限详情
     *
     * @param permissionId 权限ID
     * @return 权限响应
     */
    PermissionResponse getPermission(Long permissionId);
    
    /**
     * 根据角色ID获取权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<PermissionResponse> getPermissionsByRoleId(Long roleId);
    
    /**
     * 根据用户ID获取权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<PermissionResponse> getPermissionsByUserId(Long userId);
} 