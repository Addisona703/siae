package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.common.ApiResult;
import com.hngy.siae.auth.dto.PermissionCreateRequest;
import com.hngy.siae.auth.dto.PermissionResponse;
import com.hngy.siae.auth.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限控制器
 * 
 * @author KEYKB
 */
@Tag(name = "权限管理", description = "权限的增删改查")
@RestController
@RequestMapping("/api/v1/auth/permissions")
public class PermissionController {
    
    private final PermissionService permissionService;
    
    /**
     * 构造函数
     *
     * @param permissionService 权限服务
     */
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    
    /**
     * 创建权限
     *
     * @param request 创建权限请求
     * @return 权限响应
     */
    @Operation(summary = "创建权限", description = "创建新的权限")
    @PostMapping
    @PreAuthorize("hasAuthority('system:permission:add')")
    public ApiResult<PermissionResponse> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        PermissionResponse permissionResponse = permissionService.createPermission(request);
        return ApiResult.success(permissionResponse);
    }
    
    /**
     * 获取权限列表
     *
     * @return 权限列表
     */
    @Operation(summary = "获取权限列表", description = "获取所有权限的列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:permission:query')")
    public ApiResult<List<PermissionResponse>> getPermissions() {
        List<PermissionResponse> permissions = permissionService.getPermissions();
        return ApiResult.success(permissions);
    }
    
    /**
     * 获取权限详情
     *
     * @param permissionId 权限ID
     * @return 权限响应
     */
    @Operation(summary = "获取权限详情", description = "获取指定ID的权限详细信息")
    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('system:permission:query')")
    public ApiResult<PermissionResponse> getPermission(@PathVariable Long permissionId) {
        PermissionResponse permission = permissionService.getPermission(permissionId);
        return ApiResult.success(permission);
    }
    
    /**
     * 获取角色权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Operation(summary = "获取角色权限列表", description = "获取指定角色的权限列表")
    @GetMapping("/by-role/{roleId}")
    @PreAuthorize("hasAuthority('system:permission:query')")
    public ApiResult<List<PermissionResponse>> getPermissionsByRoleId(@PathVariable Long roleId) {
        List<PermissionResponse> permissions = permissionService.getPermissionsByRoleId(roleId);
        return ApiResult.success(permissions);
    }
    
    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Operation(summary = "获取用户权限列表", description = "获取指定用户的权限列表")
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAuthority('system:permission:query')")
    public ApiResult<List<PermissionResponse>> getPermissionsByUserId(@PathVariable Long userId) {
        List<PermissionResponse> permissions = permissionService.getPermissionsByUserId(userId);
        return ApiResult.success(permissions);
    }
} 