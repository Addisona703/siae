package com.hngy.siae.security.simple;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 简化增强权限控制系统使用示例Controller
 * 
 * 演示如何使用简化的权限控制方案
 * 
 * @author SIAE开发团队
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/simple-enhanced-permission")
@RequiredArgsConstructor
@Tag(name = "简化增强权限控制示例", description = "演示简化增强权限控制系统的使用方法")
public class SimpleEnhancedPermissionExampleController {

    private final AuthUtil authUtil;

    /**
     * 传统权限检查示例
     */
    @GetMapping("/traditional")
    @Operation(summary = "传统权限检查", description = "使用传统的@PreAuthorize注解")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> traditionalExample() {
        return Result.success("传统权限检查通过");
    }

    /**
     * 超级管理员检查示例
     */
    @GetMapping("/super-admin-check")
    @Operation(summary = "超级管理员检查", description = "演示超级管理员检查功能")
    @PreAuthorize("@authUtil.isSuperAdmin()")
    public Result<String> superAdminCheckExample() {
        return Result.success("超级管理员检查通过");
    }

    /**
     * 增强角色检查示例
     */
    @GetMapping("/enhanced-role")
    @Operation(summary = "增强角色检查", description = "演示增强的角色检查（自动包含超级管理员）")
    @PreAuthorize("@authUtil.hasRoleOrSuperAdmin('ADMIN')")
    public Result<String> enhancedRoleExample() {
        return Result.success("增强角色检查通过（管理员或超级管理员）");
    }

    /**
     * 增强权限检查示例
     */
    @GetMapping("/enhanced-permission")
    @Operation(summary = "增强权限检查", description = "演示增强的权限检查（自动包含超级管理员）")
    @PreAuthorize("@authUtil.hasPermissionOrSuperAdmin('USER_VIEW')")
    public Result<String> enhancedPermissionExample() {
        return Result.success("增强权限检查通过（拥有USER_VIEW权限或超级管理员）");
    }

    /**
     * 复合权限表达式示例 - OR逻辑
     */
    @GetMapping("/or-expression")
    @Operation(summary = "OR逻辑权限表达式", description = "演示OR逻辑的复合权限表达式")
    @PreAuthorize("@authUtil.isSuperAdmin() or hasRole('ADMIN') or hasAuthority('USER_VIEW')")
    public Result<String> orExpressionExample() {
        return Result.success("OR逻辑权限表达式检查通过");
    }

    /**
     * 复合权限表达式示例 - AND逻辑
     */
    @GetMapping("/and-expression")
    @Operation(summary = "AND逻辑权限表达式", description = "演示AND逻辑的复合权限表达式")
    @PreAuthorize("@authUtil.isSuperAdmin() or (hasRole('ADMIN') and hasAuthority('SYSTEM_CONFIG'))")
    public Result<String> andExpressionExample() {
        return Result.success("AND逻辑权限表达式检查通过");
    }

    /**
     * 多级权限检查示例
     */
    @GetMapping("/multi-level")
    @Operation(summary = "多级权限检查", description = "演示多级权限检查逻辑")
    @PreAuthorize("@authUtil.isSuperAdmin() or @authUtil.hasRoleOrSuperAdmin('ADMIN') or @authUtil.hasPermissionOrSuperAdmin('SPECIAL_ACCESS')")
    public Result<String> multiLevelExample() {
        return Result.success("多级权限检查通过");
    }

    /**
     * 编程式权限检查示例
     */
    @GetMapping("/programmatic-check")
    @Operation(summary = "编程式权限检查", description = "演示在业务逻辑中进行编程式权限检查")
    public Result<Map<String, Object>> programmaticCheckExample() {
        Map<String, Object> result = new HashMap<>();
        
        // 检查当前用户是否为超级管理员
        boolean isSuperAdmin = authUtil.isSuperAdmin();
        result.put("isSuperAdmin", isSuperAdmin);
        
        // 检查是否拥有管理员角色（包含超级管理员检查）
        boolean hasAdminRole = authUtil.hasRoleOrSuperAdmin("ADMIN");
        result.put("hasAdminRole", hasAdminRole);
        
        // 检查是否拥有特定权限（包含超级管理员检查）
        boolean hasUserViewPermission = authUtil.hasPermissionOrSuperAdmin("USER_VIEW");
        result.put("hasUserViewPermission", hasUserViewPermission);
        
        return Result.success(result);
    }

    /**
     * 条件权限检查示例
     */
    @PostMapping("/conditional-check")
    @Operation(summary = "条件权限检查", description = "根据业务条件进行权限检查")
    public Result<String> conditionalCheckExample(@RequestParam String action) {
        // 超级管理员可以执行任何操作
        if (authUtil.isSuperAdmin()) {
            return Result.success("超级管理员，可以执行任何操作：" + action);
        }
        
        // 根据不同的操作类型检查不同的权限
        switch (action.toLowerCase()) {
            case "create":
                if (authUtil.hasPermissionOrSuperAdmin("USER_CREATE")) {
                    return Result.success("有创建权限，可以执行创建操作");
                }
                break;
            case "update":
                if (authUtil.hasPermissionOrSuperAdmin("USER_UPDATE")) {
                    return Result.success("有更新权限，可以执行更新操作");
                }
                break;
            case "delete":
                if (authUtil.hasRoleOrSuperAdmin("ADMIN")) {
                    return Result.success("有管理员权限，可以执行删除操作");
                }
                break;
            default:
                return Result.error("不支持的操作类型");
        }
        
        return Result.error("权限不足，无法执行操作：" + action);
    }

    /**
     * 获取当前用户权限信息
     */
    @GetMapping("/current-user-info")
    @Operation(summary = "当前用户权限信息", description = "获取当前用户的权限和角色信息")
    public Result<Map<String, Object>> getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", authentication.getName());
        userInfo.put("authorities", authentication.getAuthorities());
        userInfo.put("isAuthenticated", authentication.isAuthenticated());
        userInfo.put("isSuperAdmin", authUtil.isSuperAdmin());
        userInfo.put("hasAdminRole", authUtil.hasRoleOrSuperAdmin("ADMIN"));
        userInfo.put("hasUserViewPermission", authUtil.hasPermissionOrSuperAdmin("USER_VIEW"));
        
        return Result.success(userInfo);
    }

    /**
     * 业务逻辑中的权限控制示例
     */
    @PutMapping("/business-logic-example")
    @Operation(summary = "业务逻辑权限控制", description = "演示在业务逻辑中进行权限控制")
    public Result<String> businessLogicExample(@RequestParam Long targetUserId) {
        // 获取当前用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        
        // 超级管理员可以操作任何用户
        if (authUtil.isSuperAdmin()) {
            return Result.success("超级管理员，可以操作用户ID：" + targetUserId);
        }
        
        // 管理员可以操作普通用户
        if (authUtil.hasRoleOrSuperAdmin("ADMIN")) {
            return Result.success("管理员，可以操作用户ID：" + targetUserId);
        }
        
        // 普通用户只能操作自己的数据
        // 这里简化处理，实际项目中需要从认证对象中获取用户ID
        if (authUtil.hasPermissionOrSuperAdmin("USER_SELF_UPDATE")) {
            return Result.success("普通用户，只能操作自己的数据");
        }
        
        return Result.error("权限不足，无法操作用户ID：" + targetUserId);
    }

    /**
     * 简化前后对比示例
     */
    @GetMapping("/comparison")
    @Operation(summary = "简化前后对比", description = "展示简化前后的权限检查方式对比")
    public Result<Map<String, String>> comparisonExample() {
        Map<String, String> comparison = new HashMap<>();
        
        comparison.put("传统方式", "@PreAuthorize(\"hasRole('ROOT') or hasRole('ADMIN')\")");
        comparison.put("简化方式", "@PreAuthorize(\"@authUtil.hasRoleOrSuperAdmin('ADMIN')\")");
        
        comparison.put("传统权限检查", "@PreAuthorize(\"hasRole('ROOT') or hasAuthority('USER_VIEW')\")");
        comparison.put("简化权限检查", "@PreAuthorize(\"@authUtil.hasPermissionOrSuperAdmin('USER_VIEW')\")");
        
        comparison.put("复杂表达式", "@PreAuthorize(\"hasRole('ROOT') or (hasRole('ADMIN') and hasAuthority('SYSTEM_CONFIG'))\")");
        comparison.put("简化表达式", "@PreAuthorize(\"@authUtil.isSuperAdmin() or (hasRole('ADMIN') and hasAuthority('SYSTEM_CONFIG'))\")");
        
        return Result.success(comparison);
    }
}
