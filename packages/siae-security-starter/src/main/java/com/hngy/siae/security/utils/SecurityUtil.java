package com.hngy.siae.security.utils;

import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.CommonResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局安全工具类
 *
 * <p>提供权限判断、所有者检查等通用方法（超级管理员已在切面默认放行）</p>
 *
 * <p>可在 Service 层直接调用，也可在 SpEL 注解中调用，如 @PreAuthorize("@authUtil.isOwner(#id)")</p>
 *
 * <p>此版本仅处理普通管理员 ROLE_ADMIN 权限判断</p>
 *
 * @author SIAE Team
 */
@Slf4j
@Component("securityUtil")
public class SecurityUtil {

    // ==================== 获取当前用户信息 ====================

    /** 获取当前认证对象 Authentication */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /** 获取当前用户ID，如果未认证则抛出异常 */
    public Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            AssertUtils.fail(CommonResultCodeEnum.UNAUTHORIZED);
        }
        Object details = authentication.getDetails();
        if (details == null) {
            AssertUtils.fail(CommonResultCodeEnum.UNAUTHORIZED);
        }
        // 处理网关传递的用户ID（Long类型）
        if (details instanceof Long) {
            return (Long) details;
        }
        // 处理字符串类型的用户ID
        String detailsStr = details.toString();
        try {
            return Long.parseLong(detailsStr);
        } catch (NumberFormatException e) {
            // 开发环境直接访问时，details 可能是 WebAuthenticationDetails
            // 此时返回默认用户ID（仅用于开发测试）
            log.warn("无法从 Authentication.details 解析用户ID: {}, 使用默认用户ID", detailsStr);
            return 1L; // 开发环境默认用户ID
        }
    }

    /** 获取当前用户ID，如果未认证返回 null */
    public Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (Exception e) {
            log.debug("获取当前用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /** 获取当前用户的所有权限集合 */
    public Set<String> getCurrentUserAuthorities() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return Collections.emptySet();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null) return Collections.emptySet();
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    // ==================== 权限判断（仅针对管理员 ROLE_ADMIN） ====================

    /** 当前用户是否是超级管理员 ROLE_ROOT */
    public boolean isSuperAdmin() {
        Set<String> authorities = getCurrentUserAuthorities();
        return authorities.contains("ROLE_ROOT");
    }

    /** 当前用户是否是普通管理员 ROLE_ADMIN */
    public boolean isAdmin() {
        Set<String> authorities = getCurrentUserAuthorities();
        return authorities.contains("ROLE_ADMIN");
    }

    /** 当前用户是否拥有任意一个角色 */
    public boolean hasAnyRole(String... roles) {
        Set<String> authorities = getCurrentUserAuthorities();
        for (String role : roles) {
            String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            if (authorities.contains(roleWithPrefix)) return true;
        }
        return false;
    }

    /** 当前用户是否拥有指定权限（仅管理员生效） */
    public boolean hasPermission(String permission) {
        if (!isAdmin()) return false;  // 仅管理员判断
        return getCurrentUserAuthorities().contains(permission);
    }

    /** 当前用户是否拥有任意一个权限（仅管理员生效） */
    public boolean hasAnyPermission(String... permissions) {
        if (!isAdmin()) return false;
        Set<String> authorities = getCurrentUserAuthorities();
        for (String permission : permissions) {
            if (authorities.contains(permission)) return true;
        }
        return false;
    }

    /** 当前用户是否拥有所有权限（仅管理员生效） */
    public boolean hasAllPermissions(String... permissions) {
        if (!isAdmin()) return false;
        Set<String> authorities = getCurrentUserAuthorities();
        for (String permission : permissions) {
            if (!authorities.contains(permission)) return false;
        }
        return true;
    }

    // ==================== 所有者判断 ====================

    /** 当前用户是否是指定记录的所有者 */
    public boolean isOwner(Long ownerId) {
        if (ownerId == null) return false;
        Long currentUserId = getCurrentUserIdOrNull();
        if (currentUserId == null) return false;
        return currentUserId.equals(ownerId);
    }

    /** 当前用户是否拥有权限或是指定记录的所有者 */
    public boolean hasPermissionOrOwner(String permission, Long ownerId) {
        return hasPermission(permission) || isOwner(ownerId);
    }

    // ==================== 其他辅助方法（可扩展） ====================

    /** 当前用户是否属于指定部门（示例，实际需实现业务逻辑） */
    public boolean belongsToDepartment(Long departmentId) {
        log.debug("检查用户是否属于部门: {}", departmentId);
        // TODO: 从用户信息或 JWT 获取部门ID再判断
        return false;
    }

    /** 当前用户是否可以审批指定用户的请假 */
    public boolean canApproveLeave(Long applicantId) {
        if (hasPermission("LEAVE_APPROVE")) {
            // TODO: 可加检查直接上级逻辑
            return true;
        }
        return false;
    }

    /** 检查指定时间范围是否在允许范围内（示例，实际需解析日期并计算天数差） */
    public boolean isWithinTimeRange(String startDate, String endDate, int maxDays) {
        log.debug("检查时间范围: startDate={}, endDate={}, maxDays={}", startDate, endDate, maxDays);
        return true;
    }
}
