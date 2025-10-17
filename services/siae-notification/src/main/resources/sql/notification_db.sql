-- ===================================================================
-- Notification Database Schema
-- 通知数据库结构
-- ===================================================================

CREATE DATABASE IF NOT EXISTS notification_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE notification_db;

-- ===================================================================
-- 1. 系统通知表
-- ===================================================================
CREATE TABLE system_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '通知类型：SYSTEM=系统通知,ANNOUNCEMENT=公告,REMIND=提醒',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    link_url VARCHAR(500) COMMENT '跳转链接',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读：0=未读,1=已读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- ===================================================================
-- 2. 邮件发送记录表
-- ===================================================================
CREATE TABLE email_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    recipient VARCHAR(200) NOT NULL COMMENT '收件人邮箱',
    subject VARCHAR(500) COMMENT '邮件主题',
    content TEXT COMMENT '邮件内容',
    status ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING' COMMENT '发送状态',
    error_msg TEXT COMMENT '错误信息',
    send_time TIMESTAMP NULL COMMENT '发送时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件发送记录表';

-- ===================================================================
-- 3. 短信发送记录表（预留）
-- ===================================================================
CREATE TABLE sms_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    content VARCHAR(500) NOT NULL COMMENT '短信内容',
    template_code VARCHAR(50) COMMENT '模板代码',
    status ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING' COMMENT '发送状态',
    error_msg TEXT COMMENT '错误信息',
    send_time TIMESTAMP NULL COMMENT '发送时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_phone (phone),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信发送记录表';

-- ===================================================================
-- 初始化测试数据
-- ===================================================================
INSERT INTO system_notification (user_id, type, title, content) VALUES
(1, 'SYSTEM', '欢迎使用SIAE系统', '感谢您注册SIAE系统，祝您使用愉快！'),
(1, 'ANNOUNCEMENT', '系统维护通知', '系统将于今晚22:00-24:00进行维护，请您提前保存数据。');