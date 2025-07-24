package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.dto.request.UserPermissionDTO;
import com.hngy.siae.auth.service.UserPermissionService;
import com.hngy.siae.auth.dto.response.UserPermissionVO;
import com.hngy.siae.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hngy.siae.core.permissions.AuthPermissions.*;

/**
 * 用户权限关联控制器
 */
@Slf4j
@RestController
@RequestMapping("/user-permission")
@RequiredArgsConstructor
@Tag(name = "用户权限管理", description = "用户权限分配相关API")
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    @Operation(summary = "查询用户权限")
    @GetMapping("/list/{userId}")
    @PreAuthorize("hasAuthority('" + AUTH_USER_PERMISSION_QUERY + "')")
    public Result<List<UserPermissionVO>> getUserPermissions(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<UserPermissionVO> userPermissions = userPermissionService.getUserPermissionsByUserId(userId);
        return Result.success(userPermissions);
    }

    @Operation(summary = "查询用户权限ID列表")
    @GetMapping("/ids/{userId}")
    @PreAuthorize("hasAuthority('" + AUTH_USER_PERMISSION_QUERY + "')")
    public Result<List<Long>> getUserPermissionIds(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        List<Long> permissionIds = userPermissionService.getPermissionIdsByUserId(userId);
        return Result.success(permissionIds);
    }

    @Operation(summary = "分配用户权限")
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('" + AUTH_USER_PERMISSION_ASSIGN + "')")
    public Result<?> assignPermissions(@RequestBody @Validated UserPermissionDTO dto) {
        return userPermissionService.assignPermissionsToUser(dto);
    }

    @Operation(summary = "移除用户所有权限")
    @DeleteMapping("/remove/all/{userId}")
    @PreAuthorize("hasAuthority('" + AUTH_USER_PERMISSION_REMOVE + "')")
    public Result<?> removeAllPermissions(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        return userPermissionService.removeAllPermissionsFromUser(userId);
    }

    @Operation(summary = "移除用户指定权限")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('" + AUTH_USER_PERMISSION_REMOVE + "')")
    public Result<?> removePermissions(@RequestBody @Validated UserPermissionDTO dto) {
        return userPermissionService.removePermissionsFromUser(dto);
    }

    @Operation(summary = "检查用户是否拥有指定权限")
    @GetMapping("/check")
    @PreAuthorize("hasAuthority('" + AUTH_USER_PERMISSION_QUERY + "')")
    public Result<Boolean> checkPermission(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "权限ID") @RequestParam Long permissionId) {
        boolean hasPermission = userPermissionService.hasPermission(userId, permissionId);
        return Result.success(hasPermission);
    }
} 