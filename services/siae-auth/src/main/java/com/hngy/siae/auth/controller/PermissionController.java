package com.hngy.siae.auth.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.auth.dto.request.PermissionCreateDTO;
import com.hngy.siae.auth.dto.response.PermissionVO;
import com.hngy.siae.auth.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {
    
    private final PermissionService permissionService;

    /**
     * 创建权限
     *
     * @param request 创建权限请求
     * @return 权限响应
     */
    @Operation(summary = "创建权限", description = "创建新的权限")
    @PostMapping
    @PreAuthorize("hasAuthority('system:permission:add')")
    public Result<PermissionVO> createPermission(@Valid @RequestBody PermissionCreateDTO request) {
        PermissionVO permissionVO = permissionService.createPermission(request);
        return Result.success(permissionVO);
    }
    
    /**
     * 获取权限列表
     *
     * @return 权限列表
     */
    @Operation(summary = "获取权限列表", description = "获取所有权限的列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:permission:query')")
    public Result<List<PermissionVO>> getPermissions() {
        List<PermissionVO> permissions = permissionService.getPermissions();
        return Result.success(permissions);
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
    public Result<PermissionVO> getPermission(
            @Parameter(description = "权限ID") @PathVariable Long permissionId) {
        PermissionVO permission = permissionService.getPermission(permissionId);
        return Result.success(permission);
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
    public Result<List<PermissionVO>> getPermissionsByRoleId(
            @Parameter(description = "角色ID") @PathVariable Long roleId) {
        List<PermissionVO> permissions = permissionService.getPermissionsByRoleId(roleId);
        return Result.success(permissions);
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
    public Result<List<PermissionVO>> getPermissionsByUserId(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<PermissionVO> permissions = permissionService.getPermissionsByUserId(userId);
        return Result.success(permissions);
    }
} 