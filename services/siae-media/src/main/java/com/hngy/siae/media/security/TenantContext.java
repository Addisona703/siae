package com.hngy.siae.media.security;

import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.MediaResultCodeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * 租户上下文
 * 用于在请求处理过程中传递租户信息
 * <p>
 * 使用场景：
 * - 多租户数据隔离
 * - 审计日志记录
 * - 配额管理
 * - 权限控制
 * <p>
 * 注意：
 * - 使用 ThreadLocal 存储，确保线程安全
 * - 请求结束后会自动清理，避免内存泄漏
 * - 如果使用异步处理，需要手动传递上下文
 *
 * @author SIAE Team
 */
@Slf4j
public class TenantContext {

    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

    /**
     * 设置租户ID
     */
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * 获取当前租户ID
     * 
     * @return 租户ID，如果未设置则返回 null
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 获取当前租户ID，如果未设置则抛出异常
     * 
     * @return 租户ID
     * @throws IllegalStateException 如果租户ID未设置
     */
    public static String getRequiredTenantId() {
        String tenantId = TENANT_ID.get();
        if (tenantId == null) {
            log.error("租户ID未设置，请检查请求头是否包含 X-Tenant-Id");
        }
        AssertUtils.notNull(tenantId, MediaResultCodeEnum.TENANT_ID_MISSING);
        return tenantId;
    }

    /**
     * 设置用户ID
     */
    public static void setUserId(String userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取当前用户ID
     * 
     * @return 用户ID，如果未设置则返回 null
     */
    public static String getUserId() {
        return USER_ID.get();
    }

    /**
     * 获取当前用户ID，如果未设置则抛出异常
     * 
     * @return 用户ID
     * @throws IllegalStateException 如果用户ID未设置
     */
    public static String getRequiredUserId() {
        String userId = USER_ID.get();
        if (userId == null) {
            log.error("用户ID未设置，请检查请求头是否包含 X-User-Id");
        }
        AssertUtils.notNull(userId, MediaResultCodeEnum.USER_ID_MISSING);
        return userId;
    }

    /**
     * 清理上下文
     * 注意：此方法由 TenantInterceptor 自动调用，业务代码无需手动调用
     */
    public static void clear() {
        TENANT_ID.remove();
        USER_ID.remove();
    }

}
