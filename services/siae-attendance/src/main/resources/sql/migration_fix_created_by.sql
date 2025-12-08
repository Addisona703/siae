-- 修复 created_by 字段，添加默认值 0（表示系统创建）
-- 这样在没有登录用户时也可以创建记录

-- 修改 attendance_rule 表的 created_by 字段
ALTER TABLE attendance_rule MODIFY COLUMN created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人ID(0表示系统创建)';

-- 如果需要，也可以修改其他表的 created_by 字段
-- ALTER TABLE attendance_record MODIFY COLUMN created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人ID(0表示系统创建)';
-- ALTER TABLE leave_request MODIFY COLUMN created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人ID(0表示系统创建)';
-- ALTER TABLE attendance_anomaly MODIFY COLUMN created_by BIGINT NOT NULL DEFAULT 0 COMMENT '创建人ID(0表示系统创建)';
