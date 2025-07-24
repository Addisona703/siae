package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.dto.request.UserRoleDTO;
import com.hngy.siae.auth.service.RoleService;
import com.hngy.siae.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.core.permissions.AuthPermissions.*;

/**
 * 用户角色控制器
 * 
 * @author KEYKB
 */
@Tag(name = "用户角色管理", description = "用户角色分配相关API")
@RestController
@RequestMapping("/user-role")
@RequiredArgsConstructor
public class UserRoleController {

    private final RoleService roleService;

    /**
     * 为用户分配角色
     *
     * @param userId  用户ID
     * @param request 用户角色请求DTO
     * @return 分配结果
     */
    @Operation(summary = "为用户分配角色", description = "为指定用户分配一个或多个角色")
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('" + AUTH_USER_ROLE_ASSIGN + "')")
    public Result<Boolean> assignUserRoles(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId,
            @Valid @RequestBody UserRoleDTO request) {
        boolean result = roleService.assignUserRoles(userId, request.getRoleIds());
        return Result.success(result);
    }
} 