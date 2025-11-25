package com.hngy.siae.security.expression;

import com.hngy.siae.security.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;

/**
 * 自定义安全表达式根对象
 *
 * <p>扩展 Spring Security 的 SecurityExpressionRoot，添加自定义权限检查方法</p>
 *
 * <p>所有权限判断委托给 {@link SecurityUtil} 实现，
 * 超级管理员已在切面默认放行，表达式仅处理普通管理员 ROLE_ADMIN</p>
 *
 * <p>可在 SpEL 注解中直接使用，例如：
 * &#064;SiaeAuthorize("isOwner(#recordId)")</p>
 *
 * @author SIAE Team
 */
@Slf4j
public class SiaeSecurityExpressionRoot extends SecurityExpressionRoot {

    /** 安全工具类，实际的权限检查逻辑都委托给它 */
    private final SecurityUtil securityUtil;

    /** 构造函数 */
    public SiaeSecurityExpressionRoot(Authentication authentication, SecurityUtil securityUtil) {
        super(authentication);
        this.securityUtil = securityUtil;
    }

    // ==================== 所有者判断 ====================

    /** 检查用户是否是指定记录的所有者 */
    public boolean isOwner(Long recordOwnerId) {
        return securityUtil.isOwner(recordOwnerId);
    }

    /** 检查用户是否拥有权限或是指定记录的所有者 */
    public boolean hasPermissionOrOwner(String permission, Long recordOwnerId) {
        return securityUtil.hasPermissionOrOwner(permission, recordOwnerId);
    }

    // ==================== 权限判断（仅管理员 ROLE_ADMIN） ====================

    /** 检查当前用户是否是普通管理员 ROLE_ADMIN */
    public boolean isAdmin() {
        return securityUtil.isAdmin();
    }

    /** 检查用户是否拥有指定权限（仅管理员生效） */
    public boolean hasPermission(String permission) {
        return securityUtil.hasPermission(permission);
    }

    /** 检查用户是否拥有任意一个权限（仅管理员生效） */
    public boolean hasAnyPermission(String... permissions) {
        return securityUtil.hasAnyPermission(permissions);
    }

    /** 检查用户是否拥有所有权限（仅管理员生效） */
    public boolean hasAllPermissions(String... permissions) {
        return securityUtil.hasAllPermissions(permissions);
    }

    // ==================== 其他辅助方法（可扩展） ====================

    /** 检查用户是否属于指定部门 */
    public boolean belongsToDepartment(Long departmentId) {
        return securityUtil.belongsToDepartment(departmentId);
    }

    /** 检查用户是否可以审批指定用户的请假 */
    public boolean canApproveLeave(Long applicantId) {
        return securityUtil.canApproveLeave(applicantId);
    }

    /** 检查时间范围是否在允许范围内 */
    public boolean isWithinTimeRange(String startDate, String endDate, int maxDays) {
        return securityUtil.isWithinTimeRange(startDate, endDate, maxDays);
    }
}
