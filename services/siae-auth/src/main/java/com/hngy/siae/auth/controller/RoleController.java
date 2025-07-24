package com.hngy.siae.auth.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.auth.dto.request.RoleCreateDTO;
import com.hngy.siae.auth.dto.request.RolePermissionDTO;
import com.hngy.siae.auth.dto.response.RoleVO;
import com.hngy.siae.auth.dto.request.RoleUpdateDTO;
import com.hngy.siae.auth.service.RoleService;
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
 * 角色控制器
 * 
 * @author KEYKB
 */
@Tag(name = "角色管理", description = "角色相关API")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * 创建角色
     *
     * @param request 角色创建请求DTO
     * @return 创建的角色
     */
    @Operation(summary = "创建角色", description = "创建新的系统角色")
    @PostMapping
    @PreAuthorize("hasAuthority('" + AUTH_ROLE_ADD + "')")
    public Result<RoleVO> createRole(@Valid @RequestBody RoleCreateDTO request) {
        RoleVO roleVO = roleService.createRole(request);
        return Result.success(roleVO);
    }

    /**
     * 更新角色
     *
     * @param roleId  角色ID
     * @param request 角色更新请求DTO
     * @return 更新结果
     */
    @Operation(summary = "更新角色", description = "更新指定角色信息")
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('" + AUTH_ROLE_EDIT + "')")
    public Result<Boolean> updateRole(
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @Valid @RequestBody RoleUpdateDTO request) {
        boolean result = roleService.updateRole(roleId, request);
        return Result.success(result);
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 删除结果
     */
    @Operation(summary = "删除角色", description = "删除指定的系统角色")
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('" + AUTH_ROLE_DELETE + "')")
    public Result<Boolean> deleteRole(
            @Parameter(description = "角色ID") @PathVariable Long roleId) {
        boolean result = roleService.deleteRole(roleId);
        return Result.success(result);
    }

    /**
     * 获取所有角色
     *
     * @return 角色列表
     */
    @Operation(summary = "获取所有角色", description = "获取系统中所有角色")
    @GetMapping
    @PreAuthorize("hasAuthority('" + AUTH_ROLE_QUERY + "')")
    public Result<List<RoleVO>> getRoles() {
        List<RoleVO> roles = roleService.getRoles();
        return Result.success(roles);
    }

    /**
     * 获取指定角色
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    @Operation(summary = "获取指定角色", description = "根据ID获取角色详情")
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('" + AUTH_ROLE_QUERY + "')")
    public Result<RoleVO> getRole(
            @Parameter(description = "角色ID") @PathVariable Long roleId) {
        RoleVO role = roleService.getRole(roleId);
        return Result.success(role);
    }

    /**
     * 分配权限
     *
     * @param roleId  角色ID
     * @param request 角色权限请求DTO
     * @return 分配结果
     */
    @Operation(summary = "分配权限", description = "为指定角色分配权限")
    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('" + AUTH_ROLE_EDIT + "')")
    public Result<Boolean> assignPermissions(
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @Valid @RequestBody RolePermissionDTO request) {
        boolean result = roleService.assignPermissions(roleId, request.getPermissionIds());
        return Result.success(result);
    }
} 