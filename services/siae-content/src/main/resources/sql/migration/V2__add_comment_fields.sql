-- ========================================
-- 评论表字段迁移
-- 添加 reply_to_user_id 和 like_count 字段
-- ========================================

USE content_db;

-- 添加回复目标用户ID字段
ALTER TABLE comment 
ADD COLUMN reply_to_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '回复目标用户ID（回复某人时使用）' 
AFTER parent_id;

-- 添加点赞数字段
ALTER TABLE comment 
ADD COLUMN like_count INT DEFAULT 0 COMMENT '点赞数' 
AFTER content;

-- 添加创建时间索引（如果不存在）
ALTER TABLE comment 
ADD INDEX idx_create_time(create_time);

-- 添加外键约束（如果不存在）
-- 注意：如果已有数据且存在不一致的 content_id，需要先清理数据
ALTER TABLE comment 
ADD CONSTRAINT fk_comment_content 
FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE;

-- 将 parent_id 的默认值从 0 改为 NULL（顶级评论）
-- 先更新现有数据
UPDATE comment SET parent_id = NULL WHERE parent_id = 0;

-- 修改字段定义
ALTER TABLE comment 
MODIFY COLUMN parent_id BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID（顶级评论为NULL）';
