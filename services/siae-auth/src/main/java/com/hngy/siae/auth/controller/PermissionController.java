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

import static com.hngy.siae.core.permissions.AuthPermissions.*;

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
    @PreAuthorize("hasAuthority('" + AUTH_PERMISSION_ADD + "')")
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
    @PreAuthorize("hasAuthority('" + AUTH_PERMISSION_QUERY + "')")
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
    @PreAuthorize("hasAuthority('" + AUTH_PERMISSION_QUERY + "')")
    public Result<PermissionVO> getPermission(
            @Parameter(description = "权限ID") @PathVariable Long permissionId) {
        PermissionVO permission = permissionService.getPermission(permissionId);
        return Result.success(permission);
    }
} 