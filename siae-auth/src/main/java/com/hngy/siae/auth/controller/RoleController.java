package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.common.ApiResult;
import com.hngy.siae.auth.dto.RoleCreateRequest;
import com.hngy.siae.auth.dto.RolePermissionRequest;
import com.hngy.siae.auth.dto.RoleResponse;
import com.hngy.siae.auth.dto.RoleUpdateRequest;
import com.hngy.siae.auth.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 * 
 * @author KEYKB
 */
@Tag(name = "角色管理", description = "角色的增删改查")
@RestController
@RequestMapping("/api/v1/auth/roles")
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * 构造函数
     *
     * @param roleService 角色服务
     */
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    /**
     * 创建角色
     *
     * @param request 创建角色请求
     * @return 角色响应
     */
    @Operation(summary = "创建角色", description = "创建新的角色")
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public ApiResult<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest request) {
        RoleResponse roleResponse = roleService.createRole(request);
        return ApiResult.success(roleResponse);
    }
    
    /**
     * 更新角色
     *
     * @param roleId  角色ID
     * @param request 更新角色请求
     * @return 操作结果
     */
    @Operation(summary = "更新角色", description = "更新指定ID的角色")
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public ApiResult<Boolean> updateRole(@PathVariable Long roleId, @Valid @RequestBody RoleUpdateRequest request) {
        boolean result = roleService.updateRole(roleId, request);
        return ApiResult.success(result);
    }
    
    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 操作结果
     */
    @Operation(summary = "删除角色", description = "删除指定ID的角色")
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public ApiResult<Boolean> deleteRole(@PathVariable Long roleId) {
        boolean result = roleService.deleteRole(roleId);
        return ApiResult.success(result);
    }
    
    /**
     * 获取角色列表
     *
     * @return 角色列表
     */
    @Operation(summary = "获取角色列表", description = "获取所有角色的列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:role:query')")
    public ApiResult<List<RoleResponse>> getRoles() {
        List<RoleResponse> roles = roleService.getRoles();
        return ApiResult.success(roles);
    }
    
    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色响应
     */
    @Operation(summary = "获取角色详情", description = "获取指定ID的角色详细信息")
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('system:role:query')")
    public ApiResult<RoleResponse> getRole(@PathVariable Long roleId) {
        RoleResponse role = roleService.getRole(roleId);
        return ApiResult.success(role);
    }
    
    /**
     * 分配角色权限
     *
     * @param roleId  角色ID
     * @param request 权限分配请求
     * @return 操作结果
     */
    @Operation(summary = "分配角色权限", description = "为指定角色分配权限")
    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public ApiResult<Boolean> assignPermissions(@PathVariable Long roleId, @Valid @RequestBody RolePermissionRequest request) {
        boolean result = roleService.assignPermissions(roleId, request.getPermissionIds());
        return ApiResult.success(result);
    }
} 