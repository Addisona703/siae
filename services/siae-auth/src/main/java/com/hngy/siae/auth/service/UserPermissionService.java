package com.hngy.siae.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.auth.dto.request.UserPermissionDTO;
import com.hngy.siae.auth.entity.UserPermission;
import com.hngy.siae.auth.dto.response.UserPermissionVO;

import java.util.List;

/**
 * 用户权限关联服务接口
 */
public interface UserPermissionService extends IService<UserPermission> {
    
    /**
     * 通过用户ID查询用户权限关联列表
     *
     * @param userId 用户ID
     * @return 用户权限关联列表
     */
    List<UserPermissionVO> getUserPermissionsByUserId(Long userId);
    
    /**
     * 通过用户ID查询权限ID列表
     *
     * @param userId 用户ID
     * @return 权限ID列表
     */
    List<Long> getPermissionIdsByUserId(Long userId);
    
    /**
     * 给用户分配权限
     *
     * @param dto 用户权限DTO
     * @return 操作结果
     */
    Result<?> assignPermissionsToUser(UserPermissionDTO dto);
    
    /**
     * 移除用户所有权限
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<?> removeAllPermissionsFromUser(Long userId);
    
    /**
     * 移除用户指定权限
     *
     * @param dto 用户权限DTO
     * @return 操作结果
     */
    Result<?> removePermissionsFromUser(UserPermissionDTO dto);
    
    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param permissionId 权限ID
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, Long permissionId);
} 