-- Content table for testing
CREATE TABLE IF NOT EXISTS content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    type TINYINT NOT NULL COMMENT '内容类型',
    description VARCHAR(500),
    cover_file_id VARCHAR(36),
    uploaded_by BIGINT,
    status TINYINT DEFAULT 0 COMMENT '状态：0草稿，1待审核，2已发布，3垃圾箱，4已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    category_id BIGINT
);

-- Statistics table for testing
CREATE TABLE IF NOT EXISTS statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_id BIGINT NOT NULL UNIQUE,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Category table for testing
CREATE TABLE IF NOT EXISTS content_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    enabled TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Audit log table for testing
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_id BIGINT NOT NULL,
    target_type TINYINT NOT NULL,
    from_status TINYINT,
    to_status TINYINT NOT NULL,
    audit_reason VARCHAR(500),
    audit_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert default category for testing
INSERT INTO content_category (id, name, parent_id, sort_order, enabled) VALUES (1, '默认分类', 0, 1, 1);
