package com.hngy.siae.auth.service;

import com.hngy.siae.auth.dto.request.PermissionCreateDTO;
import com.hngy.siae.auth.dto.response.PermissionVO;

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
    PermissionVO createPermission(PermissionCreateDTO request);
    
    /**
     * 获取权限列表
     *
     * @return 权限列表
     */
    List<PermissionVO> getPermissions();
    
    /**
     * 获取权限详情
     *
     * @param permissionId 权限ID
     * @return 权限响应
     */
    PermissionVO getPermission(Long permissionId);
}