package com.hngy.siae.attendance.permissions;

/**
 * 考勤服务权限常量
 * 定义考勤服务相关的权限标识符
 * 使用嵌套类组织不同模块的权限
 *
 * @author Siae Studio
 */
public final class AttendancePermissions {

    private AttendancePermissions() {
        // 工具类，禁止实例化
    }

    /**
     * 考勤记录权限
     */
    public static final class Record {
        private Record() {}
        
        /**
         * 查看考勤记录详情权限
         */
        public static final String VIEW = "attendance:record:view";
        
        /**
         * 查看考勤记录列表权限
         */
        public static final String LIST = "attendance:record:list";
        
        /**
         * 导出考勤记录权限
         */
        public static final String EXPORT = "attendance:record:export";
    }

    /**
     * 请假管理权限
     */
    public static final class Leave {
        private Leave() {}
        
        /**
         * 更新请假申请权限
         */
        public static final String UPDATE = "attendance:leave:update";
        
        /**
         * 审批请假申请权限
         */
        public static final String APPROVE = "attendance:leave:approve";
        
        /**
         * 查看请假申请详情权限
         */
        public static final String VIEW = "attendance:leave:view";
        
        /**
         * 查看请假申请列表权限
         */
        public static final String LIST = "attendance:leave:list";
    }

    /**
     * 考勤规则权限
     */
    public static final class Rule {
        private Rule() {}
        
        /**
         * 创建考勤规则权限
         */
        public static final String CREATE = "attendance:rule:create";
        
        /**
         * 更新考勤规则权限
         */
        public static final String UPDATE = "attendance:rule:update";
        
        /**
         * 删除考勤规则权限
         */
        public static final String DELETE = "attendance:rule:delete";
        
        /**
         * 查看考勤规则详情权限
         */
        public static final String VIEW = "attendance:rule:view";
        
        /**
         * 查看考勤规则列表权限
         */
        public static final String LIST = "attendance:rule:list";
    }

    /**
     * 考勤异常权限
     */
    public static final class Anomaly {
        private Anomaly() {}
        
        /**
         * 查看考勤异常详情权限
         */
        public static final String VIEW = "attendance:anomaly:view";
        
        /**
         * 查看考勤异常列表权限
         */
        public static final String LIST = "attendance:anomaly:list";
        
        /**
         * 处理考勤异常权限
         */
        public static final String HANDLE = "attendance:anomaly:handle";
    }

    /**
     * 考勤统计权限
     */
    public static final class Statistics {
        private Statistics() {}
        
        /**
         * 查看考勤统计权限
         */
        public static final String VIEW = "attendance:statistics:view";
        
        /**
         * 生成报表权限
         */
        public static final String REPORT_GENERATE = "attendance:statistics:report-generate";
        
        /**
         * 导出报表权限
         */
        public static final String EXPORT = "attendance:statistics:export";
    }

    /**
     * 活动考勤权限
     */
    public static final class Activity {
        private Activity() {}
        
        /**
         * 查看活动考勤列表权限
         */
        public static final String LIST = "attendance:activity:list";
        
        /**
         * 查看活动考勤详情权限
         */
        public static final String VIEW = "attendance:activity:view";
    }
}
