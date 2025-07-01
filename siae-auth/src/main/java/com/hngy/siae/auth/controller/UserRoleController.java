package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.common.ApiResult;
import com.hngy.siae.auth.dto.UserRoleRequest;
import com.hngy.siae.auth.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户角色控制器
 * 
 * @author KEYKB
 */
@Tag(name = "用户角色管理", description = "用户角色关联管理")
@RestController
@RequestMapping("/api/v1/auth/users")
public class UserRoleController {
    
    private final RoleService roleService;
    
    /**
     * 构造函数
     *
     * @param roleService 角色服务
     */
    public UserRoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    /**
     * 分配用户角色
     *
     * @param userId  用户ID
     * @param request 用户角色分配请求
     * @return 操作结果
     */
    @Operation(summary = "分配用户角色", description = "为指定用户分配角色")
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public ApiResult<Boolean> assignUserRoles(@PathVariable Long userId, @Valid @RequestBody UserRoleRequest request) {
        boolean result = roleService.assignUserRoles(userId, request.getRoleIds());
        return ApiResult.success(result);
    }
} 