package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.dto.request.BatchAssignRoleDTO;
import com.hngy.siae.auth.dto.request.UserSingleRoleDTO;
import com.hngy.siae.auth.dto.request.UserRoleQueryDTO;
import com.hngy.siae.auth.dto.request.UserRoleUpdateDTO;
import com.hngy.siae.auth.dto.response.UserRoleVO;
import com.hngy.siae.auth.service.UserRoleService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.auth.permissions.AuthPermissions.*;

/**
 * 用户角色控制器
 * 
 * @author KEYKB
 */
@Tag(name = "用户角色管理", description = "用户角色分配相关API")
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    /**
     * 为用户分配单个角色
     *
     * @param userId  用户ID
     * @param request 用户单角色分配请求DTO
     * @return 分配结果
     */
    @Operation(summary = "为用户分配单个角色", description = "为指定用户分配一个角色")
    @PostMapping("/{userId}/role")
    @SiaeAuthorize("hasAuthority('" + AUTH_USER_ROLE_ASSIGN + "')")
    public Result<Boolean> assignUserRoles(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId,
            @Valid @RequestBody UserSingleRoleDTO request) {
        Boolean result = userRoleService.assignUserRole(userId, request.getRoleId());
        return Result.success(result);
    }

    /**
     * 批量分配角色给用户
     *
     * @param roleId  角色ID
     * @param request 批量分配请求
     * @return 分配结果
     */
    @Operation(summary = "批量分配角色给用户", description = "给多个用户批量分配同一个角色")
    @PostMapping("/roles/{roleId}")
    @SiaeAuthorize("hasAuthority('" + AUTH_USER_ROLE_ASSIGN + "')")
    public Result<Boolean> batchAssignRoleToUsers(
            @Parameter(description = "角色ID") @PathVariable("roleId") @NotNull Long roleId,
            @Valid @RequestBody BatchAssignRoleDTO request) {
        Boolean result = userRoleService.batchAssignRoleToUsers(roleId, request.getUserIds());
        return Result.success(result);
    }

    /**
     * 获取用户角色关联列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    @Operation(summary = "获取用户角色关联列表", description = "分页查询用户角色关联关系，支持模糊查询")
    @PostMapping("/roles/list")
    @SiaeAuthorize("hasAuthority('" + AUTH_USER_ROLE_QUERY + "')")
    public Result<PageVO<UserRoleVO>> getUserRoleList(
            @Valid @RequestBody PageDTO<UserRoleQueryDTO> pageDTO) {
        PageVO<UserRoleVO> result = userRoleService.getUserRoleList(pageDTO);
        return Result.success(result);
    }

    /**
     * 更新用户角色关联
     *
     * @param userRoleId 用户角色关联ID
     * @param updateDTO  更新参数
     * @return 更新结果
     */
    @Operation(summary = "更新用户角色关联", description = "更新指定用户角色关联记录")
    @PutMapping("/roles/{userRoleId}")
    @SiaeAuthorize("hasAuthority('" + AUTH_USER_ROLE_UPDATE + "')")
    public Result<Boolean> updateUserRole(
            @Parameter(description = "用户角色关联ID") @PathVariable("userRoleId") @NotNull Long userRoleId,
            @Valid @RequestBody UserRoleUpdateDTO updateDTO) {
        Boolean result = userRoleService.updateUserRole(userRoleId, updateDTO);
        return Result.success(result);
    }
}