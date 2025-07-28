package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.dto.request.PermissionCreateDTO;
import com.hngy.siae.auth.dto.request.PermissionQueryDTO;
import com.hngy.siae.auth.dto.request.PermissionTreeUpdateDTO;
import com.hngy.siae.auth.dto.request.PermissionUpdateDTO;
import com.hngy.siae.auth.dto.response.PermissionTreeVO;
import com.hngy.siae.auth.dto.response.PermissionVO;
import com.hngy.siae.auth.service.PermissionService;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
     * @param permissionCreate 创建权限请求
     * @return 权限响应
     */
    @Operation(summary = "创建权限", description = "创建新的权限")
    @PostMapping
    @SiaeAuthorize("hasAuthority('" + AUTH_PERMISSION_ADD + "')")
    public Result<PermissionVO> createPermission(
            @Valid @RequestBody PermissionCreateDTO permissionCreate) {
        PermissionVO permissionVO = permissionService.createPermission(permissionCreate);
        return Result.success(permissionVO);
    }
    
    /**
     * 分页查询权限列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页权限列表
     */
    @Operation(summary = "分页查询权限列表", description = "支持条件筛选的分页权限查询")
    @PostMapping("/page")
    @SiaeAuthorize("hasAuthority('" + AUTH_PERMISSION_QUERY + "')")
    public Result<PageVO<PermissionVO>> getPermissionsPage(
            @Valid @RequestBody PageDTO<PermissionQueryDTO> pageDTO) {
        PageVO<PermissionVO> pageResult = permissionService.getPermissionsPage(pageDTO);
        return Result.success(pageResult);
    }
    
    /**
     * 查询权限树结构
     *
     * @param enabledOnly 是否只查询启用状态的权限
     * @return 权限树列表
     */
    @Operation(summary = "查询权限树结构", description = "按照层级关系查询权限树形结构")
    @GetMapping("/tree")
    @SiaeAuthorize("hasAuthority('" + AUTH_PERMISSION_QUERY + "')")
    public Result<List<PermissionTreeVO>> getPermissionTree(
            @Parameter(description = "是否只查询启用状态的权限，默认false")
            @RequestParam(defaultValue = "false") Boolean enabledOnly) {
        List<PermissionTreeVO> permissionTree = permissionService.getPermissionTree(enabledOnly);
        return Result.success(permissionTree);
    }

    /**
     * 批量更新权限树结构
     * <p>
     * 用于支持前端拖拽操作后修改权限的层级依赖关系。
     * 可以批量更新多个权限节点的父级关系和排序值。
     *
     * @param permissionTreeUpdates 权限树结构批量更新请求列表
     * @return 更新结果
     */
    @Operation(
        summary = "批量更新权限树结构",
        description = "支持前端拖拽操作，批量更新权限的层级依赖关系和排序"
    )
    @PutMapping("/tree")
    @SiaeAuthorize("hasAuthority('" + AUTH_PERMISSION_EDIT + "')")
    public Result<Boolean> updatePermissionTree(
            @Parameter(description = "权限树结构批量更新请求列表")
            @Valid @RequestBody List<PermissionTreeUpdateDTO> permissionTreeUpdates) {
        Boolean result = permissionService.updatePermissionTree(permissionTreeUpdates);
        return Result.success(result);
    }

    /**
     * 获取权限详情
     *
     * @param permissionId 权限ID
     * @return 权限响应
     */
    @Operation(summary = "获取权限详情", description = "获取指定ID的权限详细信息")
    @GetMapping("/{permissionId}")
    @SiaeAuthorize("hasAuthority('" + AUTH_PERMISSION_QUERY + "')")
    public Result<PermissionVO> getPermission(
            @Parameter(description = "权限ID") @PathVariable Long permissionId) {
        AssertUtils.notNull(permissionId, "权限ID不能为空");
        PermissionVO permission = permissionService.getPermission(permissionId);
        return Result.success(permission);
    }

    /**
     * 更新权限
     *
     * @param permissionUpdateDTO 更新权限请求
     * @return 权限响应
     */
    @Operation(summary = "更新权限", description = "更新指定权限的信息")
    @PutMapping
    @SiaeAuthorize("hasAuthority('" + AUTH_PERMISSION_EDIT + "')")
    public Result<PermissionVO> updatePermission(
            @Valid @RequestBody PermissionUpdateDTO permissionUpdateDTO) {
        PermissionVO permissionVO = permissionService.updatePermission(permissionUpdateDTO);
        return Result.success(permissionVO);
    }

    /**
     * 删除权限
     *
     * @param permissionId 权限ID
     * @return 删除结果
     */
    @Operation(summary = "删除权限", description = "删除指定ID的权限")
    @DeleteMapping("/{permissionId}")
    @SiaeAuthorize("hasAuthority('" + AUTH_PERMISSION_DELETE + "')")
    public Result<Boolean> deletePermission(
            @Parameter(description = "权限ID") @PathVariable Long permissionId) {
        AssertUtils.notNull(permissionId, "权限ID不能为空");
        Boolean result = permissionService.deletePermission(permissionId);
        return Result.success(result);
    }

    /**
     * 批量删除权限
     *
     * @param permissionIds 权限ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除权限", description = "批量删除指定ID的权限")
    @DeleteMapping("/batch")
    @SiaeAuthorize("hasAuthority('" + AUTH_PERMISSION_DELETE + "')")
    public Result<Boolean> batchDeletePermissions(
            @Parameter(description = "权限ID列表") @RequestBody List<Long> permissionIds) {
        AssertUtils.notEmpty(permissionIds, "权限ID列表不能为空");
        Boolean result = permissionService.batchDeletePermissions(permissionIds);
        return Result.success(result);
    }
}