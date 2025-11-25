package com.hngy.siae.core.permissions;

/**
 * 考勤模块权限常量定义
 * <p>
 * 命名规范：模块:资源:操作（如 attendance:record:view）
 * 常量命名规范：ATTENDANCE_资源_操作（如 ATTENDANCE_RECORD_VIEW）
 *
 * @author SIAE Team
 * @date 2025/01/21
 */
public class AttendancePermissions {

    // ==================== 考勤记录权限 ====================
    
    /**
     * 考勤记录权限
     */
    public static class Record {
        /** 创建考勤记录 */
        public static final String CREATE = "attendance:record:create";
        
        /** 更新考勤记录 */
        public static final String UPDATE = "attendance:record:update";
        
        /** 删除考勤记录 */
        public static final String DELETE = "attendance:record:delete";
        
        /** 查询考勤记录 */
        public static final String VIEW = "attendance:record:view";
        
        /** 分页查询考勤记录列表 */
        public static final String LIST = "attendance:record:list";
        
        /** 导出考勤记录 */
        public static final String EXPORT = "attendance:record:export";
    }

    // ==================== 考勤异常权限 ====================
    
    /**
     * 考勤异常权限
     */
    public static class Anomaly {
        /** 查询考勤异常 */
        public static final String VIEW = "attendance:anomaly:view";
        
        /** 分页查询考勤异常列表 */
        public static final String LIST = "attendance:anomaly:list";
        
        /** 处理考勤异常 */
        public static final String HANDLE = "attendance:anomaly:handle";
    }

    // ==================== 考勤规则权限 ====================
    
    /**
     * 考勤规则权限
     */
    public static class Rule {
        /** 创建考勤规则 */
        public static final String CREATE = "attendance:rule:create";
        
        /** 更新考勤规则 */
        public static final String UPDATE = "attendance:rule:update";
        
        /** 删除考勤规则 */
        public static final String DELETE = "attendance:rule:delete";
        
        /** 查询考勤规则 */
        public static final String VIEW = "attendance:rule:view";
        
        /** 分页查询考勤规则列表 */
        public static final String LIST = "attendance:rule:list";
    }

    // ==================== 考勤班次权限 ====================
    
    /**
     * 考勤班次权限
     */
    public static class Shift {
        /** 创建考勤班次 */
        public static final String CREATE = "attendance:shift:create";
        
        /** 更新考勤班次 */
        public static final String UPDATE = "attendance:shift:update";
        
        /** 删除考勤班次 */
        public static final String DELETE = "attendance:shift:delete";
        
        /** 查询考勤班次 */
        public static final String VIEW = "attendance:shift:view";
        
        /** 分页查询考勤班次列表 */
        public static final String LIST = "attendance:shift:list";
    }

    // ==================== 活动考勤权限 ====================
    
    /**
     * 活动考勤权限
     */
    public static class Activity {
        /** 创建活动考勤 */
        public static final String CREATE = "attendance:activity:create";
        
        /** 更新活动考勤 */
        public static final String UPDATE = "attendance:activity:update";
        
        /** 删除活动考勤 */
        public static final String DELETE = "attendance:activity:delete";
        
        /** 查询活动考勤 */
        public static final String VIEW = "attendance:activity:view";
        
        /** 分页查询活动考勤列表 */
        public static final String LIST = "attendance:activity:list";
        
        /** 签到 */
        public static final String CHECKIN = "attendance:activity:checkin";
        
        /** 签退 */
        public static final String CHECKOUT = "attendance:activity:checkout";
    }

    // ==================== 请假管理权限 ====================
    
    /**
     * 请假管理权限
     */
    public static class Leave {
        /** 创建请假申请 */
        public static final String CREATE = "attendance:leave:create";
        
        /** 更新请假申请 */
        public static final String UPDATE = "attendance:leave:update";
        
        /** 删除/撤销请假申请 */
        public static final String DELETE = "attendance:leave:delete";
        
        /** 查询请假申请 */
        public static final String VIEW = "attendance:leave:view";
        
        /** 分页查询请假申请列表 */
        public static final String LIST = "attendance:leave:list";
        
        /** 审批请假申请 */
        public static final String APPROVE = "attendance:leave:approve";
    }

    // ==================== 考勤统计权限 ====================
    
    /**
     * 考勤统计权限
     */
    public static class Statistics {
        /** 查询考勤统计 */
        public static final String VIEW = "attendance:statistics:view";
        
        /** 导出考勤统计 */
        public static final String EXPORT = "attendance:statistics:export";
        
        /** 生成报表 */
        public static final String REPORT_GENERATE = "attendance:statistics:report:generate";
    }
}
