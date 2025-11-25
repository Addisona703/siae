-- ===================================================================
-- 考勤服务数据库初始化脚本
-- Database: attendance_db
-- ===================================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS attendance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE attendance_db;

-- ===================================================================
-- 1. 考勤记录表
-- ===================================================================
CREATE TABLE IF NOT EXISTS attendance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    attendance_type TINYINT NOT NULL DEFAULT 0 COMMENT '考勤类型(0-日常考勤,1-活动考勤)',
    related_id BIGINT NULL COMMENT '关联ID(活动考勤时存活动ID)',
    check_in_time DATETIME NOT NULL COMMENT '签到时间',
    check_out_time DATETIME NULL COMMENT '签退时间',
    check_in_location VARCHAR(255) NULL COMMENT '签到地点',
    check_out_location VARCHAR(255) NULL COMMENT '签退地点',
    duration_minutes INT NULL COMMENT '考勤时长(分钟)',
    attendance_date DATE NOT NULL COMMENT '考勤日期',
    rule_id BIGINT NULL COMMENT '应用的规则ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0-进行中,1-已完成,2-异常)',
    remark VARCHAR(500) NULL COMMENT '备注',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_user_date (user_id, attendance_date),
    INDEX idx_date (attendance_date),
    INDEX idx_status (status),
    INDEX idx_type_related (attendance_type, related_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤记录表';

-- ===================================================================
-- 2. 考勤异常表
-- ===================================================================
CREATE TABLE IF NOT EXISTS attendance_anomaly (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    attendance_record_id BIGINT NULL COMMENT '关联的考勤记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    anomaly_type TINYINT NOT NULL COMMENT '异常类型(0-迟到,1-早退,2-缺勤,3-漏签到,4-漏签退)',
    anomaly_date DATE NOT NULL COMMENT '异常日期',
    duration_minutes INT NULL COMMENT '异常时长(分钟)',
    description VARCHAR(500) NULL COMMENT '异常描述',
    resolved TINYINT NOT NULL DEFAULT 0 COMMENT '是否已处理(0-未处理,1-已处理)',
    handler_id BIGINT NULL COMMENT '处理人ID',
    handler_note VARCHAR(500) NULL COMMENT '处理说明',
    handled_at DATETIME NULL COMMENT '处理时间',
    suppressed_by_leave BIGINT NULL COMMENT '被请假抑制(请假申请ID)',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_user_date (user_id, anomaly_date),
    INDEX idx_record (attendance_record_id),
    INDEX idx_type (anomaly_type),
    INDEX idx_resolved (resolved)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤异常表';

-- ===================================================================
-- 3. 请假申请表
-- ===================================================================
CREATE TABLE IF NOT EXISTS leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    leave_type TINYINT NOT NULL COMMENT '请假类型(0-病假,1-事假)',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    days DECIMAL(5,1) NOT NULL COMMENT '请假天数',
    reason TEXT NOT NULL COMMENT '请假原因',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0-待审核,1-已批准,2-已拒绝,3-已撤销)',
    approver_id BIGINT NULL COMMENT '审批人ID',
    approval_note VARCHAR(500) NULL COMMENT '审批意见',
    approved_at DATETIME NULL COMMENT '审批时间',
    attachment_file_ids JSON NULL COMMENT '附件文件ID列表',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_user_status (user_id, status),
    INDEX idx_date_range (start_date, end_date),
    INDEX idx_approver (approver_id),
    INDEX idx_status (status),
    INDEX idx_type (leave_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请表';

-- ===================================================================
-- 4. 考勤规则表
-- ===================================================================
CREATE TABLE IF NOT EXISTS attendance_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '规则名称',
    description VARCHAR(500) NULL COMMENT '规则描述',
    attendance_type TINYINT NOT NULL DEFAULT 0 COMMENT '考勤类型(0-日常考勤,1-活动考勤)',
    related_id BIGINT NULL COMMENT '关联ID(活动考勤时存活动ID)',
    target_type TINYINT NOT NULL COMMENT '适用对象类型(0-全体,1-部门,2-个人)',
    target_ids JSON NULL COMMENT '适用对象ID列表',
    check_in_start_time TIME NOT NULL COMMENT '签到开始时间',
    check_in_end_time TIME NOT NULL COMMENT '签到结束时间',
    check_out_start_time TIME NOT NULL COMMENT '签退开始时间',
    check_out_end_time TIME NOT NULL COMMENT '签退结束时间',
    late_threshold_minutes INT NOT NULL DEFAULT 0 COMMENT '迟到阈值(分钟)',
    early_threshold_minutes INT NOT NULL DEFAULT 0 COMMENT '早退阈值(分钟)',
    location_required TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要位置验证',
    allowed_locations JSON NULL COMMENT '允许的位置列表',
    location_radius_meters INT NULL COMMENT '位置半径(米)',
    effective_date DATE NOT NULL COMMENT '生效日期',
    expiry_date DATE NULL COMMENT '失效日期',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级(数字越大优先级越高)',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_target (target_type, status),
    INDEX idx_effective (effective_date, expiry_date),
    INDEX idx_priority (priority DESC),
    INDEX idx_type_related (attendance_type, related_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤规则表';

-- ===================================================================
-- 5. 考勤统计表
-- ===================================================================
CREATE TABLE IF NOT EXISTS attendance_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stat_month VARCHAR(7) NOT NULL COMMENT '统计月份(YYYY-MM)',
    total_days INT NOT NULL DEFAULT 0 COMMENT '应出勤天数',
    actual_days INT NOT NULL DEFAULT 0 COMMENT '实际出勤天数',
    late_count INT NOT NULL DEFAULT 0 COMMENT '迟到次数',
    early_count INT NOT NULL DEFAULT 0 COMMENT '早退次数',
    absence_count INT NOT NULL DEFAULT 0 COMMENT '缺勤次数',
    leave_days DECIMAL(5,1) NOT NULL DEFAULT 0 COMMENT '请假天数',
    total_duration_minutes INT NOT NULL DEFAULT 0 COMMENT '总考勤时长(分钟)',
    attendance_rate DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '出勤率(%)',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_user_month (user_id, stat_month),
    INDEX idx_month (stat_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤统计表';

-- ===================================================================
-- 6. 操作日志表
-- ===================================================================
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '操作人ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_module VARCHAR(50) NOT NULL COMMENT '操作模块',
    operation_desc VARCHAR(500) NULL COMMENT '操作描述',
    request_method VARCHAR(10) NULL COMMENT '请求方法',
    request_url VARCHAR(500) NULL COMMENT '请求URL',
    request_params TEXT NULL COMMENT '请求参数',
    response_result TEXT NULL COMMENT '响应结果',
    ip_address VARCHAR(50) NULL COMMENT 'IP地址',
    user_agent VARCHAR(500) NULL COMMENT '用户代理',
    execution_time INT NULL COMMENT '执行时长(ms)',
    status TINYINT NOT NULL COMMENT '状态(0-失败,1-成功)',
    error_message TEXT NULL COMMENT '错误信息',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_type (operation_type),
    INDEX idx_module (operation_module),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ===================================================================
-- 初始化默认考勤规则
-- ===================================================================
INSERT INTO attendance_rule (
    name, description, attendance_type, target_type, 
    check_in_start_time, check_in_end_time, 
    check_out_start_time, check_out_end_time,
    late_threshold_minutes, early_threshold_minutes,
    location_required, effective_date, status, priority, created_by, created_at, updated_at
) VALUES (
    '默认考勤规则', '全体成员默认考勤规则', 0, 0,
    '08:00:00', '09:00:00',
    '17:00:00', '18:00:00',
    15, 15,
    0, '2024-01-01', 1, 0, 1, NOW(), NOW()
);
