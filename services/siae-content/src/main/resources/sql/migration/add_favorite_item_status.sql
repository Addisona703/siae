-- 为 favorite_item 表添加状态字段，支持逻辑删除
ALTER TABLE favorite_item ADD COLUMN status TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常' AFTER sort_order;

-- 添加索引
ALTER TABLE favorite_item ADD INDEX idx_status(status);
