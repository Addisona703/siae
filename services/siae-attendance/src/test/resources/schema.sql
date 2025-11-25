-- Drop tables if they exist
DROP TABLE IF EXISTS attendance_anomaly;
DROP TABLE IF EXISTS attendance_record;
DROP TABLE IF EXISTS attendance_rule;
DROP TABLE IF EXISTS leave_request;
DROP TABLE IF EXISTS attendance_statistics;

-- 考勤记录表
CREATE TABLE attendance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    attendance_type TINYINT NOT NULL DEFAULT 0,
    related_id BIGINT,
    check_in_time TIMESTAMP NOT NULL,
    check_out_time TIMESTAMP,
    check_in_location VARCHAR(255),
    check_out_location VARCHAR(255),
    duration_minutes INT,
    attendance_date DATE NOT NULL,
    rule_id BIGINT,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_user_date ON attendance_record(user_id, attendance_date);
CREATE INDEX idx_date ON attendance_record(attendance_date);
CREATE INDEX idx_status ON attendance_record(status);
CREATE INDEX idx_type_related ON attendance_record(attendance_type, related_id);

-- 考勤规则表 (使用VARCHAR代替JSON for H2 compatibility)
CREATE TABLE attendance_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    attendance_type TINYINT NOT NULL DEFAULT 0,
    related_id BIGINT,
    target_type TINYINT NOT NULL,
    target_ids VARCHAR(1000),
    check_in_start_time TIME NOT NULL,
    check_in_end_time TIME NOT NULL,
    check_out_start_time TIME NOT NULL,
    check_out_end_time TIME NOT NULL,
    late_threshold_minutes INT NOT NULL DEFAULT 0,
    early_threshold_minutes INT NOT NULL DEFAULT 0,
    location_required TINYINT NOT NULL DEFAULT 0,
    allowed_locations VARCHAR(2000),
    location_radius_meters INT,
    effective_date DATE NOT NULL,
    expiry_date DATE,
    status TINYINT NOT NULL DEFAULT 1,
    priority INT NOT NULL DEFAULT 0,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_target ON attendance_rule(target_type, status);
CREATE INDEX idx_effective ON attendance_rule(effective_date, expiry_date);
CREATE INDEX idx_priority ON attendance_rule(priority DESC);
CREATE INDEX idx_type_related_rule ON attendance_rule(attendance_type, related_id);

-- 考勤异常表
CREATE TABLE attendance_anomaly (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_record_id BIGINT,
    user_id BIGINT NOT NULL,
    anomaly_type TINYINT NOT NULL,
    anomaly_date DATE NOT NULL,
    duration_minutes INT,
    description VARCHAR(500),
    resolved TINYINT NOT NULL DEFAULT 0,
    handler_id BIGINT,
    handler_note VARCHAR(500),
    handled_at TIMESTAMP,
    suppressed_by_leave BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_user_date_anomaly ON attendance_anomaly(user_id, anomaly_date);
CREATE INDEX idx_record ON attendance_anomaly(attendance_record_id);
CREATE INDEX idx_type ON attendance_anomaly(anomaly_type);
CREATE INDEX idx_resolved ON attendance_anomaly(resolved);

-- 请假申请表 (使用VARCHAR代替JSON for H2 compatibility)
CREATE TABLE leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    leave_type TINYINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days DECIMAL(5,1) NOT NULL,
    reason TEXT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    approver_id BIGINT,
    approval_note VARCHAR(500),
    approved_at TIMESTAMP,
    attachment_file_ids VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_user_status ON leave_request(user_id, status);
CREATE INDEX idx_date_range ON leave_request(start_date, end_date);
CREATE INDEX idx_approver ON leave_request(approver_id);
CREATE INDEX idx_status_leave ON leave_request(status);
CREATE INDEX idx_type_leave ON leave_request(leave_type);

-- 考勤统计表
CREATE TABLE attendance_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stat_month VARCHAR(7) NOT NULL,
    total_days INT NOT NULL DEFAULT 0,
    actual_days INT NOT NULL DEFAULT 0,
    late_count INT NOT NULL DEFAULT 0,
    early_count INT NOT NULL DEFAULT 0,
    absence_count INT NOT NULL DEFAULT 0,
    leave_days DECIMAL(5,1) NOT NULL DEFAULT 0,
    total_duration_minutes INT NOT NULL DEFAULT 0,
    attendance_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX uk_user_month ON attendance_statistics(user_id, stat_month);
CREATE INDEX idx_month ON attendance_statistics(stat_month);
