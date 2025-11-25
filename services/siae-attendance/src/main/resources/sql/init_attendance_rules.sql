-- ===================================================================
-- 初始化考勤规则 - 支持多时段签到
-- ===================================================================

USE attendance_db;

-- 清空现有规则（可选，如果需要重新初始化）
-- DELETE FROM attendance_rule WHERE id IN (1, 2, 3);

-- ===================================================================
-- 1. 上午时段考勤规则
-- ===================================================================
INSERT INTO attendance_rule (
    id,
    name,
    description,
    attendance_type,
    related_id,
    target_type,
    target_ids,
    check_in_start_time,
    check_in_end_time,
    check_out_start_time,
    check_out_end_time,
    late_threshold_minutes,
    early_threshold_minutes,
    location_required,
    allowed_locations,
    location_radius_meters,
    effective_date,
    expiry_date,
    status,
    priority,
    created_by,
    created_at,
    updated_at,
    deleted
) VALUES (
    1,
    '上午考勤',
    '上午时段考勤规则：08:00-12:00',
    0,                          -- 日常考勤
    NULL,                       -- 无关联ID
    0,                          -- 全体成员
    NULL,                       -- 无特定对象
    '08:00:00',                 -- 签到开始时间
    '09:00:00',                 -- 签到结束时间（允许08:00-09:00之间签到）
    '11:30:00',                 -- 签退开始时间
    '12:00:00',                 -- 签退结束时间
    15,                         -- 迟到阈值：15分钟
    15,                         -- 早退阈值：15分钟
    0,                          -- 不需要位置验证
    NULL,                       -- 无位置限制
    NULL,                       -- 无半径限制
    '2024-01-01',               -- 生效日期
    NULL,                       -- 无失效日期（长期有效）
    1,                          -- 启用状态
    10,                         -- 优先级：10
    1,                          -- 创建人ID
    NOW(),
    NOW(),
    0
);

-- ===================================================================
-- 2. 下午时段考勤规则
-- ===================================================================
INSERT INTO attendance_rule (
    id,
    name,
    description,
    attendance_type,
    related_id,
    target_type,
    target_ids,
    check_in_start_time,
    check_in_end_time,
    check_out_start_time,
    check_out_end_time,
    late_threshold_minutes,
    early_threshold_minutes,
    location_required,
    allowed_locations,
    location_radius_meters,
    effective_date,
    expiry_date,
    status,
    priority,
    created_by,
    created_at,
    updated_at,
    deleted
) VALUES (
    2,
    '下午考勤',
    '下午时段考勤规则：14:00-18:00',
    0,                          -- 日常考勤
    NULL,                       -- 无关联ID
    0,                          -- 全体成员
    NULL,                       -- 无特定对象
    '14:00:00',                 -- 签到开始时间
    '14:30:00',                 -- 签到结束时间（允许14:00-14:30之间签到）
    '17:30:00',                 -- 签退开始时间
    '18:00:00',                 -- 签退结束时间
    15,                         -- 迟到阈值：15分钟
    15,                         -- 早退阈值：15分钟
    0,                          -- 不需要位置验证
    NULL,                       -- 无位置限制
    NULL,                       -- 无半径限制
    '2024-01-01',               -- 生效日期
    NULL,                       -- 无失效日期（长期有效）
    1,                          -- 启用状态
    20,                         -- 优先级：20
    1,                          -- 创建人ID
    NOW(),
    NOW(),
    0
);

-- ===================================================================
-- 3. 晚上时段考勤规则
-- ===================================================================
INSERT INTO attendance_rule (
    id,
    name,
    description,
    attendance_type,
    related_id,
    target_type,
    target_ids,
    check_in_start_time,
    check_in_end_time,
    check_out_start_time,
    check_out_end_time,
    late_threshold_minutes,
    early_threshold_minutes,
    location_required,
    allowed_locations,
    location_radius_meters,
    effective_date,
    expiry_date,
    status,
    priority,
    created_by,
    created_at,
    updated_at,
    deleted
) VALUES (
    3,
    '晚上考勤',
    '晚上时段考勤规则：19:00-22:00',
    0,                          -- 日常考勤
    NULL,                       -- 无关联ID
    0,                          -- 全体成员
    NULL,                       -- 无特定对象
    '19:00:00',                 -- 签到开始时间
    '19:30:00',                 -- 签到结束时间（允许19:00-19:30之间签到）
    '21:30:00',                 -- 签退开始时间
    '22:00:00',                 -- 签退结束时间
    15,                         -- 迟到阈值：15分钟
    15,                         -- 早退阈值：15分钟
    0,                          -- 不需要位置验证
    NULL,                       -- 无位置限制
    NULL,                       -- 无半径限制
    '2024-01-01',               -- 生效日期
    NULL,                       -- 无失效日期（长期有效）
    1,                          -- 启用状态
    30,                         -- 优先级：30
    1,                          -- 创建人ID
    NOW(),
    NOW(),
    0
);

-- ===================================================================
-- 验证插入结果
-- ===================================================================
SELECT 
    id,
    name,
    check_in_start_time,
    check_in_end_time,
    check_out_start_time,
    check_out_end_time,
    priority,
    status
FROM attendance_rule
WHERE id IN (1, 2, 3)
ORDER BY priority;
