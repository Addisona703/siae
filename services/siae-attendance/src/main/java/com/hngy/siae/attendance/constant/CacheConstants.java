package com.hngy.siae.attendance.constant;

/**
 * 缓存常量
 * 定义所有缓存的 key 前缀和过期时间
 *
 * @author SIAE Team
 */
public class CacheConstants {

    /**
     * 考勤规则缓存
     * 缓存时间：2小时（规则不经常变化）
     */
    public static final String CACHE_ATTENDANCE_RULE = "attendance:rule";
    public static final long CACHE_ATTENDANCE_RULE_TTL = 2 * 60 * 60; // 2小时

    /**
     * 用户适用规则缓存
     * 缓存时间：1小时
     */
    public static final String CACHE_USER_APPLICABLE_RULE = "attendance:user:rule";
    public static final long CACHE_USER_APPLICABLE_RULE_TTL = 60 * 60; // 1小时

    /**
     * 考勤统计缓存
     * 缓存时间：30分钟（统计数据可以接受一定延迟）
     */
    public static final String CACHE_ATTENDANCE_STATISTICS = "attendance:statistics";
    public static final long CACHE_ATTENDANCE_STATISTICS_TTL = 30 * 60; // 30分钟

    /**
     * 个人考勤统计缓存
     * 缓存时间：30分钟
     */
    public static final String CACHE_PERSONAL_STATISTICS = "attendance:statistics:personal";
    public static final long CACHE_PERSONAL_STATISTICS_TTL = 30 * 60; // 30分钟

    /**
     * 部门考勤统计缓存
     * 缓存时间：30分钟
     */
    public static final String CACHE_DEPARTMENT_STATISTICS = "attendance:statistics:department";
    public static final long CACHE_DEPARTMENT_STATISTICS_TTL = 30 * 60; // 30分钟

    /**
     * 签到防重复缓存
     * 缓存时间：24小时（一天内不能重复签到）
     */
    public static final String CACHE_CHECK_IN_DUPLICATE = "attendance:checkin:duplicate";
    public static final long CACHE_CHECK_IN_DUPLICATE_TTL = 24 * 60 * 60; // 24小时

    /**
     * 签退防重复缓存
     * 缓存时间：24小时
     */
    public static final String CACHE_CHECK_OUT_DUPLICATE = "attendance:checkout:duplicate";
    public static final long CACHE_CHECK_OUT_DUPLICATE_TTL = 24 * 60 * 60; // 24小时

    /**
     * 活动考勤规则缓存
     * 缓存时间：1小时
     */
    public static final String CACHE_ACTIVITY_RULE = "attendance:activity:rule";
    public static final long CACHE_ACTIVITY_RULE_TTL = 60 * 60; // 1小时

    /**
     * 活动考勤统计缓存
     * 缓存时间：15分钟
     */
    public static final String CACHE_ACTIVITY_STATISTICS = "attendance:activity:statistics";
    public static final long CACHE_ACTIVITY_STATISTICS_TTL = 15 * 60; // 15分钟

    /**
     * 生成缓存 key
     *
     * @param prefix 前缀
     * @param params 参数
     * @return 缓存 key
     */
    public static String generateKey(String prefix, Object... params) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object param : params) {
            sb.append(":").append(param);
        }
        return sb.toString();
    }
}
