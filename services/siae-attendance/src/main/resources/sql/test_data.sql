-- ===================================================================
-- 考勤服务测试数据
-- 包含：考勤规则、考勤记录、请假申请、考勤异常、统计数据
-- ===================================================================

USE attendance_db;

-- ===================================================================
-- 清除现有数据（按照外键依赖顺序删除）
-- ===================================================================

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 清空所有表数据
TRUNCATE TABLE operation_log;
TRUNCATE TABLE attendance_statistics;
TRUNCATE TABLE attendance_anomaly;
TRUNCATE TABLE leave_request;
TRUNCATE TABLE attendance_record;
TRUNCATE TABLE attendance_rule;

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ===================================================================
-- 1. 考勤规则数据
-- ===================================================================

-- 上午考勤规则
INSERT INTO attendance_rule (
    name, description, attendance_type, related_id, target_type, target_ids,
    check_in_start_time, check_in_end_time, check_out_start_time, check_out_end_time,
    late_threshold_minutes, early_threshold_minutes, location_required, allowed_locations, location_radius_meters,
    effective_date, expiry_date, status, priority, created_by, created_at, updated_at, deleted
) VALUES (
    '上午考勤', '上午时段考勤规则：08:00-12:00', 0, NULL, 0, NULL,
    '08:00:00', '09:00:00', '11:30:00', '12:00:00',
    15, 15, 0, NULL, NULL,
    '2024-01-01', NULL, 1, 10, 1, NOW(), NOW(), 0
);

-- 下午考勤规则
INSERT INTO attendance_rule (
    name, description, attendance_type, related_id, target_type, target_ids,
    check_in_start_time, check_in_end_time, check_out_start_time, check_out_end_time,
    late_threshold_minutes, early_threshold_minutes, location_required, allowed_locations, location_radius_meters,
    effective_date, expiry_date, status, priority, created_by, created_at, updated_at, deleted
) VALUES (
    '下午考勤', '下午时段考勤规则：14:00-18:00', 0, NULL, 0, NULL,
    '14:00:00', '14:30:00', '17:30:00', '18:00:00',
    15, 15, 0, NULL, NULL,
    '2024-01-01', NULL, 1, 10, 1, NOW(), NOW(), 0
);

-- 全天考勤规则
INSERT INTO attendance_rule (
    name, description, attendance_type, related_id, target_type, target_ids,
    check_in_start_time, check_in_end_time, check_out_start_time, check_out_end_time,
    late_threshold_minutes, early_threshold_minutes, location_required, allowed_locations, location_radius_meters,
    effective_date, expiry_date, status, priority, created_by, created_at, updated_at, deleted
) VALUES (
    '全天考勤', '全天考勤规则：09:00-18:00', 0, NULL, 0, NULL,
    '08:30:00', '09:30:00', '17:30:00', '18:30:00',
    30, 30, 0, NULL, NULL,
    '2024-01-01', NULL, 1, 5, 1, NOW(), NOW(), 0
);

-- 活动考勤规则（假设活动ID为1001）
INSERT INTO attendance_rule (
    name, description, attendance_type, related_id, target_type, target_ids,
    check_in_start_time, check_in_end_time, check_out_start_time, check_out_end_time,
    late_threshold_minutes, early_threshold_minutes, location_required, allowed_locations, location_radius_meters,
    effective_date, expiry_date, status, priority, created_by, created_at, updated_at, deleted
) VALUES (
    '团建活动考勤', '公司团建活动考勤规则', 1, 1001, 0, NULL,
    '09:00:00', '10:00:00', '16:00:00', '17:00:00',
    10, 10, 1, '[{"name":"公园入口","latitude":39.9042,"longitude":116.4074},{"name":"活动中心","latitude":39.9050,"longitude":116.4080}]', 500,
    '2024-11-01', '2024-11-30', 1, 20, 1, NOW(), NOW(), 0
);

-- ===================================================================
-- 2. 考勤记录数据（假设用户ID：1-管理员, 2-张三, 3-李四）
-- ===================================================================

-- 用户1（管理员）- 正常考勤记录
INSERT INTO attendance_record (
    user_id, attendance_type, related_id, check_in_time, check_out_time,
    check_in_location, check_out_location, duration_minutes, attendance_date,
    rule_id, status, remark, created_at, updated_at, deleted
) VALUES (
    1, 0, NULL, '2024-11-25 08:45:00', '2024-11-25 18:10:00',
    '公司大楼', '公司大楼', 565, '2024-11-25',
    3, 1, '正常考勤', NOW(), NOW(), 0
);

-- 用户2（张三）- 迟到记录
INSERT INTO attendance_record (
    user_id, attendance_type, related_id, check_in_time, check_out_time,
    check_in_location, check_out_location, duration_minutes, attendance_date,
    rule_id, status, remark, created_at, updated_at, deleted
) VALUES (
    2, 0, NULL, '2024-11-25 09:45:00', '2024-11-25 18:05:00',
    '公司大楼', '公司大楼', 500, '2024-11-25',
    3, 2, '迟到15分钟', NOW(), NOW(), 0
);

-- 用户3（李四）- 早退记录
INSERT INTO attendance_record (
    user_id, attendance_type, related_id, check_in_time, check_out_time,
    check_in_location, check_out_location, duration_minutes, attendance_date,
    rule_id, status, remark, created_at, updated_at, deleted
) VALUES (
    3, 0, NULL, '2024-11-25 08:50:00', '2024-11-25 17:00:00',
    '公司大楼', '公司大楼', 490, '2024-11-25',
    3, 2, '早退30分钟', NOW(), NOW(), 0
);

-- 用户2（张三）- 昨天的正常记录
INSERT INTO attendance_record (
    user_id, attendance_type, related_id, check_in_time, check_out_time,
    check_in_location, check_out_location, duration_minutes, attendance_date,
    rule_id, status, remark, created_at, updated_at, deleted
) VALUES (
    2, 0, NULL, '2024-11-24 08:55:00', '2024-11-24 18:00:00',
    '公司大楼', '公司大楼', 545, '2024-11-24',
    3, 1, '正常考勤', NOW(), NOW(), 0
);

-- 用户1（管理员）- 活动考勤记录
INSERT INTO attendance_record (
    user_id, attendance_type, related_id, check_in_time, check_out_time,
    check_in_location, check_out_location, duration_minutes, attendance_date,
    rule_id, status, remark, created_at, updated_at, deleted
) VALUES (
    1, 1, 1001, '2024-11-20 09:30:00', '2024-11-20 16:30:00',
    '公园入口', '活动中心', 420, '2024-11-20',
    4, 1, '团建活动', NOW(), NOW(), 0
);

-- 用户2（张三）- 进行中的考勤（只签到未签退）
INSERT INTO attendance_record (
    user_id, attendance_type, related_id, check_in_time, check_out_time,
    check_in_location, check_out_location, duration_minutes, attendance_date,
    rule_id, status, remark, created_at, updated_at, deleted
) VALUES (
    2, 0, NULL, '2024-11-26 08:50:00', NULL,
    '公司大楼', NULL, NULL, '2024-11-26',
    3, 0, '进行中', NOW(), NOW(), 0
);

-- ===================================================================
-- 3. 考勤异常数据
-- ===================================================================

-- 用户2（张三）的迟到异常
INSERT INTO attendance_anomaly (
    attendance_record_id, user_id, anomaly_type, anomaly_date, duration_minutes,
    description, resolved, handler_id, handler_note, handled_at, suppressed_by_leave,
    created_at, updated_at, deleted
) VALUES (
    2, 2, 0, '2024-11-25', 15,
    '迟到15分钟', 0, NULL, NULL, NULL, NULL,
    NOW(), NOW(), 0
);

-- 用户3（李四）的早退异常
INSERT INTO attendance_anomaly (
    attendance_record_id, user_id, anomaly_type, anomaly_date, duration_minutes,
    description, resolved, handler_id, handler_note, handled_at, suppressed_by_leave,
    created_at, updated_at, deleted
) VALUES (
    3, 3, 1, '2024-11-25', 30,
    '早退30分钟', 0, NULL, NULL, NULL, NULL,
    NOW(), NOW(), 0
);

-- 用户3（李四）的缺勤异常（11-23没有考勤记录）
INSERT INTO attendance_anomaly (
    attendance_record_id, user_id, anomaly_type, anomaly_date, duration_minutes,
    description, resolved, handler_id, handler_note, handled_at, suppressed_by_leave,
    created_at, updated_at, deleted
) VALUES (
    NULL, 3, 2, '2024-11-23', NULL,
    '全天缺勤', 1, 1, '已请假，异常已处理', NOW(), 1,
    NOW(), NOW(), 0
);

-- 用户2（张三）的漏签退异常（11-22只签到未签退）
INSERT INTO attendance_anomaly (
    attendance_record_id, user_id, anomaly_type, anomaly_date, duration_minutes,
    description, resolved, handler_id, handler_note, handled_at, suppressed_by_leave,
    created_at, updated_at, deleted
) VALUES (
    NULL, 2, 4, '2024-11-22', NULL,
    '漏签退', 1, 1, '已补签', '2024-11-23 09:00:00', NULL,
    NOW(), NOW(), 0
);

-- ===================================================================
-- 4. 请假申请数据
-- ===================================================================

-- 用户3（李四）的病假申请（已批准，对应11-23的缺勤）
INSERT INTO leave_request (
    user_id, leave_type, start_date, end_date, days, reason,
    status, approver_id, approval_note, approved_at, attachment_file_ids,
    created_at, updated_at, deleted
) VALUES (
    3, 0, '2024-11-23', '2024-11-23', 1.0, '感冒发烧，需要休息',
    1, 1, '同意请假，注意休息', '2024-11-22 16:00:00', NULL,
    '2024-11-22 14:00:00', NOW(), 0
);

-- 用户2（张三）的事假申请（待审核）
INSERT INTO leave_request (
    user_id, leave_type, start_date, end_date, days, reason,
    status, approver_id, approval_note, approved_at, attachment_file_ids,
    created_at, updated_at, deleted
) VALUES (
    2, 1, '2024-11-28', '2024-11-29', 2.0, '家中有事需要处理',
    0, 1, NULL, NULL, NULL,
    NOW(), NOW(), 0
);

-- 用户3（李四）的事假申请（已拒绝）
INSERT INTO leave_request (
    user_id, leave_type, start_date, end_date, days, reason,
    status, approver_id, approval_note, approved_at, attachment_file_ids,
    created_at, updated_at, deleted
) VALUES (
    3, 1, '2024-12-01', '2024-12-03', 3.0, '计划旅游',
    2, 1, '年底工作繁忙，暂不批准', '2024-11-24 10:00:00', NULL,
    '2024-11-23 15:00:00', NOW(), 0
);

-- 用户2（张三）的病假申请（已批准）
INSERT INTO leave_request (
    user_id, leave_type, start_date, end_date, days, reason,
    status, approver_id, approval_note, approved_at, attachment_file_ids,
    created_at, updated_at, deleted
) VALUES (
    2, 0, '2024-11-15', '2024-11-16', 2.0, '身体不适，需要就医',
    1, 1, '同意请假', '2024-11-14 17:00:00', '["file_001.jpg", "file_002.pdf"]',
    '2024-11-14 16:00:00', NOW(), 0
);

-- 用户3（李四）的事假申请（已撤销）
INSERT INTO leave_request (
    user_id, leave_type, start_date, end_date, days, reason,
    status, approver_id, approval_note, approved_at, attachment_file_ids,
    created_at, updated_at, deleted
) VALUES (
    3, 1, '2024-12-10', '2024-12-11', 2.0, '个人事务',
    3, NULL, NULL, NULL, NULL,
    '2024-11-20 10:00:00', '2024-11-21 09:00:00', 0
);

-- ===================================================================
-- 5. 考勤统计数据（2024年11月）
-- ===================================================================

-- 用户1（管理员）的11月统计
INSERT INTO attendance_statistics (
    user_id, stat_month, total_days, actual_days, late_count, early_count,
    absence_count, leave_days, total_duration_minutes, attendance_rate,
    created_at, updated_at
) VALUES (
    1, '2024-11', 22, 20, 0, 0,
    0, 0, 10800, 90.91,
    NOW(), NOW()
);

-- 用户2（张三）的11月统计
INSERT INTO attendance_statistics (
    user_id, stat_month, total_days, actual_days, late_count, early_count,
    absence_count, leave_days, total_duration_minutes, attendance_rate,
    created_at, updated_at
) VALUES (
    2, '2024-11', 22, 18, 2, 0,
    0, 2.0, 9720, 81.82,
    NOW(), NOW()
);

-- 用户3（李四）的11月统计
INSERT INTO attendance_statistics (
    user_id, stat_month, total_days, actual_days, late_count, early_count,
    absence_count, leave_days, total_duration_minutes, attendance_rate,
    created_at, updated_at
) VALUES (
    3, '2024-11', 22, 17, 0, 1,
    1, 1.0, 9180, 77.27,
    NOW(), NOW()
);

-- 用户1（管理员）的10月统计
INSERT INTO attendance_statistics (
    user_id, stat_month, total_days, actual_days, late_count, early_count,
    absence_count, leave_days, total_duration_minutes, attendance_rate,
    created_at, updated_at
) VALUES (
    1, '2024-10', 23, 23, 0, 0,
    0, 0, 12420, 100.00,
    NOW(), NOW()
);

-- ===================================================================
-- 6. 操作日志数据
-- ===================================================================

-- 管理员审批请假
INSERT INTO operation_log (
    user_id, operation_type, operation_module, operation_desc,
    request_method, request_url, request_params, response_result,
    ip_address, user_agent, execution_time, status, error_message, created_at
) VALUES (
    1, 'APPROVE_LEAVE', 'LEAVE', '审批请假申请',
    'POST', '/api/v1/attendance/leaves/1/approve', '{"approved":true,"reason":"同意请假，注意休息"}', '{"code":200,"message":"success"}',
    '192.168.1.100', 'Mozilla/5.0', 125, 1, NULL, '2024-11-22 16:00:00'
);

-- 用户签到
INSERT INTO operation_log (
    user_id, operation_type, operation_module, operation_desc,
    request_method, request_url, request_params, response_result,
    ip_address, user_agent, execution_time, status, error_message, created_at
) VALUES (
    2, 'CHECK_IN', 'ATTENDANCE', '签到',
    'POST', '/api/v1/attendance/records/check-in', '{"location":"公司大楼"}', '{"code":200,"message":"success"}',
    '192.168.1.101', 'Mozilla/5.0', 89, 1, NULL, '2024-11-25 09:45:00'
);

-- 用户签退
INSERT INTO operation_log (
    user_id, operation_type, operation_module, operation_desc,
    request_method, request_url, request_params, response_result,
    ip_address, user_agent, execution_time, status, error_message, created_at
) VALUES (
    2, 'CHECK_OUT', 'ATTENDANCE', '签退',
    'POST', '/api/v1/attendance/records/check-out', '{"location":"公司大楼"}', '{"code":200,"message":"success"}',
    '192.168.1.101', 'Mozilla/5.0', 76, 1, NULL, '2024-11-25 18:05:00'
);

-- 创建请假申请
INSERT INTO operation_log (
    user_id, operation_type, operation_module, operation_desc,
    request_method, request_url, request_params, response_result,
    ip_address, user_agent, execution_time, status, error_message, created_at
) VALUES (
    2, 'CREATE_LEAVE', 'LEAVE', '创建请假申请',
    'POST', '/api/v1/attendance/leaves', '{"leaveType":1,"startDate":"2024-11-28","endDate":"2024-11-29","reason":"家中有事"}', '{"code":200,"message":"success"}',
    '192.168.1.101', 'Mozilla/5.0', 156, 1, NULL, NOW()
);

-- 导出考勤记录
INSERT INTO operation_log (
    user_id, operation_type, operation_module, operation_desc,
    request_method, request_url, request_params, response_result,
    ip_address, user_agent, execution_time, status, error_message, created_at
) VALUES (
    1, 'EXPORT_RECORDS', 'ATTENDANCE', '导出考勤记录',
    'GET', '/api/v1/attendance/records/export', 'startDate=2024-11-01&endDate=2024-11-30&format=csv', 'file_download',
    '192.168.1.100', 'Mozilla/5.0', 2345, 1, NULL, '2024-11-25 10:00:00'
);

-- 生成报表
INSERT INTO operation_log (
    user_id, operation_type, operation_module, operation_desc,
    request_method, request_url, request_params, response_result,
    ip_address, user_agent, execution_time, status, error_message, created_at
) VALUES (
    1, 'GENERATE_REPORT', 'STATISTICS', '生成考勤报表',
    'POST', '/api/v1/attendance/statistics/report', '{"reportType":"monthly","month":"2024-11"}', '{"code":200,"message":"success"}',
    '192.168.1.100', 'Mozilla/5.0', 1876, 1, NULL, '2024-11-25 11:00:00'
);

-- ===================================================================
-- 数据说明
-- ===================================================================
-- 用户ID说明：
--   1 - 管理员（admin）
--   2 - 张三（普通员工）
--   3 - 李四（普通员工）
--
-- 考勤规则ID说明：
--   1 - 上午考勤规则
--   2 - 下午考勤规则
--   3 - 全天考勤规则
--   4 - 活动考勤规则
--
-- 考勤类型：
--   0 - 日常考勤
--   1 - 活动考勤
--
-- 异常类型：
--   0 - 迟到
--   1 - 早退
--   2 - 缺勤
--   3 - 漏签到
--   4 - 漏签退
--
-- 请假类型：
--   0 - 病假
--   1 - 事假
--
-- 请假状态：
--   0 - 待审核
--   1 - 已批准
--   2 - 已拒绝
--   3 - 已撤销
--
-- 考勤记录状态：
--   0 - 进行中
--   1 - 已完成
--   2 - 异常
-- ===================================================================
