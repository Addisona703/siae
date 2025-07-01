package com.hngy.siae.auth.service;

import com.hngy.siae.auth.dto.RoleCreateRequest;
import com.hngy.siae.auth.dto.RoleResponse;
import com.hngy.siae.auth.dto.RoleUpdateRequest;

import java.util.List;

/**
 * 角色服务接口
 * 
 * @author KEYKB
 */
public interface RoleService {
    
    /**
     * 创建角色
     *
     * @param request 创建角色请求
     * @return 角色响应
     */
    RoleResponse createRole(RoleCreateRequest request);
    
    /**
     * 更新角色
     *
     * @param roleId  角色ID
     * @param request 更新角色请求
     * @return 是否成功
     */
    boolean updateRole(Long roleId, RoleUpdateRequest request);
    
    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long roleId);
    
    /**
     * 获取角色列表
     *
     * @return 角色列表
     */
    List<RoleResponse> getRoles();
    
    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色响应
     */
    RoleResponse getRole(Long roleId);
    
    /**
     * 分配角色权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds);
    
    /**
     * 分配用户角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean assignUserRoles(Long userId, List<Long> roleIds);
} 