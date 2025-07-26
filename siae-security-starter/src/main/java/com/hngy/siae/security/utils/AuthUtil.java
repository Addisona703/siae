package com.hngy.siae.security.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限验证工具类
 * 提供统一的权限和角色验证方法，支持超级管理员逻辑
 *
 * @author SIAE Team
 */
@Slf4j
@Component("authUtil")
public class AuthUtil {

    /**
     * 超级管理员角色常量
     */
    private static final String SUPER_ADMIN_ROLE = "ROLE_ROOT";

    /**
     * 用户是否拥有任意权限或角色（OR逻辑）
     * 支持超级管理员直接放行
     *
     * @param authentication Spring Security认证对象
     * @param permissions 权限或角色数组
     * @return true表示拥有任意一个权限或角色，false表示都不拥有
     */
    public boolean hasAnyPermissionOrRole(Authentication authentication, String[] permissions) {
        // 基础验证
        if (isValidAuthentication(authentication)) {
            log.debug("认证对象无效或未认证");
            return false;
        }

        if (permissions == null || permissions.length == 0) {
            log.warn("权限数组为空，默认拒绝访问");
            return false;
        }

        // 获取用户权限集合
        Set<String> userAuthorities = getUserAuthorities(authentication);
        String username = authentication.getName();

        // 超级管理员直接放行
        if (userAuthorities.contains(SUPER_ADMIN_ROLE)) {
            log.debug("用户 [{}] 拥有超级管理员权限 [{}]，直接放行", username, SUPER_ADMIN_ROLE);
            return true;
        }

        // 过滤有效权限
        String[] validPermissions = Arrays.stream(permissions)
                .filter(StringUtils::hasText)
                .toArray(String[]::new);

        if (validPermissions.length == 0) {
            log.warn("过滤后权限数组为空，拒绝访问");
            return false;
        }

        // 检查是否拥有任意一个权限
        boolean hasPermission = Arrays.stream(validPermissions)
                .anyMatch(userAuthorities::contains);

        if (hasPermission) {
            String matchedPermissions = Arrays.stream(validPermissions)
                    .filter(userAuthorities::contains)
                    .collect(Collectors.joining(", "));
            log.debug("用户 [{}] 拥有权限 [{}]，验证通过", username, matchedPermissions);
        } else {
            log.debug("用户 [{}] 不拥有任何所需权限 [{}]，验证失败",
                    username, String.join(", ", validPermissions));
        }

        return hasPermission;
    }

    /**
     * 用户是否拥有所有权限和角色（AND逻辑）
     * 超级管理员直接放行
     *
     * @param authentication Spring Security认证对象
     * @param permissions 权限或角色数组
     * @return true表示拥有所有权限或角色，false表示缺少某些权限
     */
    public boolean hasAllPermissionOrRole(Authentication authentication, String[] permissions) {
        // 基础验证
        if (isValidAuthentication(authentication)) {
            log.debug("认证对象无效或未认证");
            return false;
        }

        if (permissions == null || permissions.length == 0) {
            log.warn("权限数组为空，默认拒绝访问");
            return false;
        }

        // 获取用户权限集合
        Set<String> userAuthorities = getUserAuthorities(authentication);
        String username = authentication.getName();

        // 超级管理员直接放行
        if (userAuthorities.contains(SUPER_ADMIN_ROLE)) {
            log.debug("用户 [{}] 拥有超级管理员权限 [{}]，直接放行", username, SUPER_ADMIN_ROLE);
            return true;
        }

        // 过滤有效权限
        String[] validPermissions = Arrays.stream(permissions)
                .filter(StringUtils::hasText)
                .toArray(String[]::new);

        if (validPermissions.length == 0) {
            log.warn("过滤后权限数组为空，拒绝访问");
            return false;
        }

        // 检查是否拥有所有权限
        boolean hasAllPermissions = Arrays.stream(validPermissions)
                .allMatch(userAuthorities::contains);

        if (hasAllPermissions) {
            log.debug("用户 [{}] 拥有所有所需权限 [{}]，验证通过",
                    username, String.join(", ", validPermissions));
        } else {
            String missingPermissions = Arrays.stream(validPermissions)
                    .filter(permission -> !userAuthorities.contains(permission))
                    .collect(Collectors.joining(", "));
            log.debug("用户 [{}] 缺少权限 [{}]，验证失败", username, missingPermissions);
        }

        return hasAllPermissions;
    }

    /**
     * 检查用户是否为超级管理员
     *
     * @param authentication 认证对象
     * @return true表示是超级管理员，false表示不是
     */
    public boolean isSuperAdmin(Authentication authentication) {
        if (isValidAuthentication(authentication)) {
            return false;
        }

        Set<String> userAuthorities = getUserAuthorities(authentication);
        return userAuthorities.contains(SUPER_ADMIN_ROLE);
    }

    /**
     * 检查当前用户是否为超级管理员
     * 便捷方法，用于在SpEL表达式中调用
     *
     * @return true表示是超级管理员，false表示不是
     */
    public boolean isSuperAdmin() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return isSuperAdmin(authentication);
    }

    /**
     * 增强的权限检查：超级管理员或拥有指定权限
     * 用于简化SpEL表达式，支持超级管理员全局放行
     *
     * @param permission 权限标识
     * @return true表示超级管理员或拥有权限，false表示权限不足
     */
    public boolean hasPermissionOrSuperAdmin(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 检查超级管理员
        if (isSuperAdmin(authentication)) {
            log.debug("用户 [{}] 是超级管理员，直接放行", authentication.getName());
            return true;
        }

        // 检查具体权限
        Set<String> userAuthorities = getUserAuthorities(authentication);
        boolean hasPermission = userAuthorities.contains(permission);

        if (log.isDebugEnabled()) {
            log.debug("用户 [{}] 权限检查 [{}]: {}",
                authentication.getName(), permission, hasPermission ? "通过" : "失败");
        }

        return hasPermission;
    }

    /**
     * 增强的角色检查：超级管理员或拥有指定角色
     * 用于简化SpEL表达式，支持超级管理员全局放行
     *
     * @param role 角色标识（不需要ROLE_前缀）
     * @return true表示超级管理员或拥有角色，false表示角色不足
     */
    public boolean hasRoleOrSuperAdmin(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 检查超级管理员
        if (isSuperAdmin(authentication)) {
            log.debug("用户 [{}] 是超级管理员，直接放行", authentication.getName());
            return true;
        }

        // 检查具体角色
        String roleAuthority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        Set<String> userAuthorities = getUserAuthorities(authentication);
        boolean hasRole = userAuthorities.contains(roleAuthority);

        if (log.isDebugEnabled()) {
            log.debug("用户 [{}] 角色检查 [{}]: {}",
                authentication.getName(), role, hasRole ? "通过" : "失败");
        }

        return hasRole;
    }

/*================= 此类内中调用的工具 =====================*/
    /**
     * 验证认证对象是否有效
     *
     * @param authentication 认证对象
     * @return true表示有效，false表示无效
     */
    private boolean isValidAuthentication(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated();
    }

    /**
     * 获取用户权限集合
     *
     * @param authentication 认证对象
     * @return 用户权限集合
     */
    private Set<String> getUserAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }
}
