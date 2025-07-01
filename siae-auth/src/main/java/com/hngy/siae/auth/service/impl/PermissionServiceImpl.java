package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.auth.common.BusinessException;
import com.hngy.siae.auth.dto.PermissionCreateRequest;
import com.hngy.siae.auth.dto.PermissionResponse;
import com.hngy.siae.auth.entity.Permission;
import com.hngy.siae.auth.mapper.PermissionMapper;
import com.hngy.siae.auth.service.PermissionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 * 
 * @author KEYKB
 */
@Service
public class PermissionServiceImpl implements PermissionService {
    
    private final PermissionMapper permissionMapper;
    
    /**
     * 构造函数
     *
     * @param permissionMapper 权限Mapper
     */
    public PermissionServiceImpl(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }
    
    @Override
    public PermissionResponse createPermission(PermissionCreateRequest request) {
        // 检查权限编码是否已存在
        Permission existingPermission = permissionMapper.selectOne(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getCode, request.getCode())
        );
        
        if (existingPermission != null) {
            throw new BusinessException("权限编码已存在");
        }
        
        // 创建权限
        Permission permission = new Permission();
        permission.setName(request.getName());
        permission.setCode(request.getCode());
        permission.setType(request.getType());
        permission.setParentId(request.getParentId());
        permission.setPath(request.getPath());
        permission.setComponent(request.getComponent());
        permission.setIcon(request.getIcon());
        permission.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        permission.setStatus(1);  // 默认启用
        permission.setCreatedAt(LocalDateTime.now());
        permissionMapper.insert(permission);
        
        // 构建响应
        return convertToPermissionResponse(permission);
    }
    
    @Override
    public List<PermissionResponse> getPermissions() {
        List<Permission> permissions = permissionMapper.selectList(null);
        return permissions.stream()
                .map(this::convertToPermissionResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public PermissionResponse getPermission(Long permissionId) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        return convertToPermissionResponse(permission);
    }
    
    @Override
    public List<PermissionResponse> getPermissionsByRoleId(Long roleId) {
        List<Permission> permissions = permissionMapper.selectByRoleId(roleId);
        return permissions.stream()
                .map(this::convertToPermissionResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PermissionResponse> getPermissionsByUserId(Long userId) {
        List<Permission> permissions = permissionMapper.selectByUserId(userId);
        return permissions.stream()
                .map(this::convertToPermissionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 将权限实体转换为权限响应
     *
     * @param permission 权限实体
     * @return 权限响应
     */
    private PermissionResponse convertToPermissionResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setName(permission.getName());
        response.setCode(permission.getCode());
        response.setType(permission.getType());
        response.setParentId(permission.getParentId());
        response.setPath(permission.getPath());
        response.setComponent(permission.getComponent());
        response.setIcon(permission.getIcon());
        response.setSortOrder(permission.getSortOrder());
        response.setStatus(permission.getStatus());
        return response;
    }
} 