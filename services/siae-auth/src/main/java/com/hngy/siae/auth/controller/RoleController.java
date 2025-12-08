package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.dto.request.RoleCreateDTO;
import com.hngy.siae.auth.dto.request.RolePermissionDTO;
import com.hngy.siae.auth.dto.request.RolePermissionUpdateDTO;
import com.hngy.siae.auth.dto.request.RoleQueryDTO;
import com.hngy.siae.auth.dto.request.RoleUpdateDTO;
import com.hngy.siae.auth.dto.response.PermissionVO;
import com.hngy.siae.auth.dto.response.RoleVO;
import com.hngy.siae.auth.service.RoleService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hngy.siae.auth.permissions.AuthPermissions.*;

/**
 * 角色控制器
 * 
 * @author KEYKB
 */
@Tag(name = "角色管理", description = "角色相关API")
@Validated
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * 创建角色
     *
     * @param roleCreateDTO 角色创建请求DTO
     * @return 创建的角色
     */
    @Operation(summary = "创建角色", description = "创建新的系统角色")
    @PostMapping
    @SiaeAuthorize("hasAuthority('" + AUTH_ROLE_ADD + "')")
    public Result<RoleVO> createRole(@Valid @RequestBody RoleCreateDTO roleCreateDTO) {
        RoleVO roleVO = roleService.createRole(roleCreateDTO);
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
    @SiaeAuthorize("hasAuthority('" + AUTH_ROLE_EDIT + "')")
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
    @SiaeAuthorize("hasAuthority('" + AUTH_ROLE_DELETE + "')")
    public Result<Boolean> deleteRole(
            @Parameter(description = "角色ID") @PathVariable @NotNull Long roleId) {
        boolean result = roleService.deleteRole(roleId);
        return Result.success(result);
    }

    /**
     * 批量删除角色
     *
     * @param roleIds 角色ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除角色", description = "批量删除指定ID的角色")
    @DeleteMapping("/batch")
    @SiaeAuthorize("hasAuthority('" + AUTH_ROLE_DELETE + "')")
    public Result<Boolean> batchDeleteRoles(
            @Parameter(description = "角色ID列表")
            @RequestBody @NotEmpty List<@NotNull Long> roleIds) {
        Boolean result = roleService.batchDeleteRoles(roleIds);
        return Result.success(result);
    }

    /**
     * 分页查询角色列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页角色列表
     */
    @Operation(summary = "分页查询角色列表", description = "支持条件筛选的分页角色查询")
    @PostMapping("/page")
    @SiaeAuthorize("hasAuthority('" + AUTH_ROLE_QUERY + "')")
    public Result<PageVO<RoleVO>> getRolesPage(
            @Valid @RequestBody PageDTO<RoleQueryDTO> pageDTO) {
        PageVO<RoleVO> pageResult = roleService.getRolesPage(pageDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取所有角色
     *
     * @return 角色列表
     */
    @Operation(summary = "获取所有角色", description = "获取系统中所有角色（不分页）")
    @GetMapping
    @SiaeAuthorize("hasAuthority('" + AUTH_ROLE_QUERY + "')")
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
    @SiaeAuthorize("hasAuthority('" + AUTH_ROLE_QUERY + "')")
    public Result<RoleVO> getRole(
            @Parameter(description = "角色ID") @PathVariable @NotNull Long roleId) {
        RoleVO role = roleService.getRole(roleId);
        return Result.success(role);
    }

    /**
     * 获取角色权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Operation(summary = "获取角色权限列表", description = "获取指定角色的权限列表")
    @GetMapping("/{roleId}/permissions")
    @SiaeAuthorize("hasAuthority('" + AUTH_PERMISSION_QUERY + "')")
    public Result<List<PermissionVO>> getPermissionsByRoleId(
            @Parameter(description = "角色ID") @PathVariable @NotNull Long roleId) {
        List<PermissionVO> permissions = roleService.getPermissionsByRoleId(roleId);
        return Result.success(permissions);
    }

    /**
     * 更新角色权限
     * <p>
     * 完全替换角色的权限列表，支持批量分配和移除权限
     *
     * @param roleId  角色ID
     * @param request 角色权限更新请求DTO
     * @return 更新结果
     */
    @Operation(summary = "更新角色权限", description = "完全替换角色的权限列表")
    @PutMapping("/{roleId}/permissions")
    @SiaeAuthorize("hasAuthority('" + AUTH_ROLE_EDIT + "')")
    public Result<Boolean> updateRolePermissions(
            @Parameter(description = "角色ID") @PathVariable @NotNull Long roleId,
            @Valid @RequestBody RolePermissionUpdateDTO request) {
        Boolean result = roleService.updateRolePermissions(roleId, request.getPermissionIds());
        return Result.success(result);
    }

    /**
     * 追加角色权限
     *
     * @param roleId  角色ID
     * @param request 角色权限请求DTO
     * @return 分配结果
     */
    @Operation(summary = "分配权限", description = "为指定角色追加权限（不会移除现有权限）")
    @PostMapping("/{roleId}/permissions")
    @SiaeAuthorize("hasAuthority('" + AUTH_ROLE_EDIT + "')")
    public Result<Boolean> assignPermissions(
            @Parameter(description = "角色ID") @PathVariable @NotNull Long roleId,
            @Valid @RequestBody RolePermissionDTO request) {
        boolean result = roleService.assignPermissions(roleId, request.getPermissionIds());
        return Result.success(result);
    }
}