package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.dto.request.UserPermissionDTO;
import com.hngy.siae.auth.service.UserPermissionService;
import com.hngy.siae.auth.dto.response.UserPermissionVO;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.auth.permissions.AuthPermissions.*;

/**
 * 用户权限关联控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/user-permission")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户权限管理", description = "用户权限分配相关API")
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    /**
     * 分页查询用户权限列表
     *
     * @param userId 用户ID
     * @param pageDTO 分页参数
     * @return 用户权限分页列表
     */
    @Operation(summary = "分页查询用户权限", description = "根据用户ID分页查询该用户拥有的权限信息")
    @GetMapping("/list/{userId}")
    @SiaeAuthorize("hasAuthority('" + AUTH_USER_PERMISSION_QUERY + "')")
    public Result<PageVO<UserPermissionVO>> getUserPermissions(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId,
            @Valid PageDTO<Object> pageDTO) {
        PageVO<UserPermissionVO> userPermissions = userPermissionService.getUserPermissionsByUserId(userId, pageDTO);
        return Result.success(userPermissions);
    }



    /**
     * 分配用户权限（覆盖模式）
     *
     * @param dto 用户权限分配参数
     * @return 操作结果
     */
    @Operation(summary = "分配用户权限", description = "为用户分配权限，会先清除原有权限再分配新权限")
    @PostMapping("/assign")
    @SiaeAuthorize("hasAuthority('" + AUTH_USER_PERMISSION_ASSIGN + "')")
    public Result<Boolean> assignPermissions(
            @Parameter(description = "用户权限分配参数", required = true) @RequestBody @Valid UserPermissionDTO dto) {
        Boolean result = userPermissionService.assignPermissionsToUser(dto);
        return Result.success(result);
    }

    /**
     * 追加用户权限（增量模式）
     *
     * @param dto 用户权限追加参数
     * @return 操作结果
     */
    @Operation(summary = "追加用户权限", description = "为用户追加权限，不会影响原有权限")
    @PostMapping("/append")
    @SiaeAuthorize("hasAuthority('" + AUTH_USER_PERMISSION_ASSIGN + "')")
    public Result<Boolean> appendPermissions(
            @Parameter(description = "用户权限追加参数", required = true) @RequestBody @Valid UserPermissionDTO dto) {
        Boolean result = userPermissionService.appendPermissionsToUser(dto);
        return Result.success(result);
    }

    /**
     * 移除用户所有权限
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "移除用户所有权限", description = "清除指定用户的所有权限")
    @DeleteMapping("/remove/all/{userId}")
    @SiaeAuthorize("hasAuthority('" + AUTH_USER_PERMISSION_REMOVE + "')")
    public Result<Boolean> removeAllPermissions(
            @Parameter(description = "用户ID", required = true) @PathVariable @NotNull Long userId) {
        Boolean result = userPermissionService.removeAllPermissionsFromUser(userId);
        return Result.success(result);
    }

    /**
     * 移除用户指定权限
     *
     * @param dto 用户权限移除参数
     * @return 操作结果
     */
    @Operation(summary = "移除用户指定权限", description = "移除用户的指定权限")
    @DeleteMapping("/remove")
    @SiaeAuthorize("hasAuthority('" + AUTH_USER_PERMISSION_REMOVE + "')")
    public Result<Boolean> removePermissions(
            @Parameter(description = "用户权限移除参数", required = true) @RequestBody @Valid UserPermissionDTO dto) {
        Boolean result = userPermissionService.removePermissionsFromUser(dto);
        return Result.success(result);
    }

}