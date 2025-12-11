DROP DATABASE IF EXISTS content_db;
CREATE DATABASE IF NOT EXISTS content_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE content_db;

-- 内容主表
CREATE TABLE content (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  title VARCHAR(255) NOT NULL COMMENT '资源标题',
  type TINYINT NOT NULL COMMENT '资源类型（0文章、1笔记、2提问、3文件、4视频）',
  description TEXT COMMENT '资源摘要，用于列表页或预览页展示',
  cover_file_id VARCHAR(36) COMMENT '封面文件ID（UUID字符串），关联media服务',
  category_id BIGINT UNSIGNED DEFAULT 0 COMMENT '关联的分类ID，外键，指向 category 表',
  uploaded_by BIGINT UNSIGNED NOT NULL COMMENT '上传者/作者用户 ID',
  status TINYINT DEFAULT 0 COMMENT '状态：0草稿，1待审核，2已发布，3已删除',
  version INT DEFAULT 0 COMMENT '乐观锁版本号',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认当前时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认当前时间，更新时自动刷新',
  INDEX idx_type(type),
  INDEX idx_status(status),
  INDEX idx_uploaded_by(uploaded_by),
  INDEX idx_create_time(create_time),
  INDEX idx_category_id(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容主表';

-- 文章详情表
CREATE TABLE article (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '关联的内容ID，外键，指向 content 表',
  content LONGTEXT NOT NULL COMMENT '文章正文内容',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认当前时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认当前时间，更新时自动刷新',
  INDEX idx_content_id(content_id),
  CONSTRAINT fk_article_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章详情表';

-- 问题详情表
CREATE TABLE question (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '关联的内容ID，外键，指向 content 表',
  content LONGTEXT NOT NULL COMMENT '问题详细描述',
  answer_count INT DEFAULT 0 COMMENT '回答数量',
  solved TINYINT DEFAULT 0 COMMENT '是否已解决：0未解决，1已解决',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认当前时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认当前时间，更新时自动刷新',
  INDEX idx_content_id(content_id),
  CONSTRAINT fk_question_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题详情表';

-- 笔记详情表
CREATE TABLE note (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '关联的内容ID，外键，指向 content 表',
  content LONGTEXT NOT NULL COMMENT '笔记内容',
  format VARCHAR(32) DEFAULT 'markdown' COMMENT '笔记格式：markdown/rich_text',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认当前时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认当前时间，更新时自动刷新',
  INDEX idx_content_id(content_id),
  CONSTRAINT fk_note_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记详情表';

-- 文件详情表（精简版，文件元数据通过 Media 服务获取）
CREATE TABLE file (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '关联的内容ID，外键，指向 content 表',
  file_id VARCHAR(36) COMMENT '文件ID（UUID字符串），关联media服务',
  download_count INT DEFAULT 0 COMMENT '下载次数',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认当前时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认当前时间，更新时自动刷新',
  INDEX idx_content_id(content_id),
  CONSTRAINT fk_file_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件详情表';

-- 视频详情表（精简版，视频元数据通过 Media 服务获取）
CREATE TABLE video (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '关联的内容ID，外键，指向 content 表',
  video_file_id VARCHAR(36) COMMENT '视频文件ID（UUID字符串），关联media服务',
  play_count INT DEFAULT 0 COMMENT '播放次数',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认当前时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认当前时间，更新时自动刷新',
  INDEX idx_content_id(content_id),
  CONSTRAINT fk_video_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频详情表';

-- 分类表
CREATE TABLE category (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  name VARCHAR(64) NOT NULL COMMENT '分类名称',
  code VARCHAR(64) NOT NULL COMMENT '分类编码',
  parent_id BIGINT UNSIGNED DEFAULT NULL COMMENT '父分类ID，NULL 表示无父类',
  status TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用，2=已删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_parent(parent_id),
  INDEX idx_status(status),
  UNIQUE INDEX uk_name(name),
  UNIQUE INDEX uk_code(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容分类表';


-- 标签字典表
CREATE TABLE tag (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  name VARCHAR(64) NOT NULL COMMENT '标签名称',
  description VARCHAR(255) COMMENT '标签描述',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容标签表';

-- 内容-标签关联表（多对多）
CREATE TABLE tag_relation (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '内容ID',
  tag_id BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关联创建时间',
  UNIQUE KEY uniq_content_tag (content_id, tag_id),
  INDEX idx_content(content_id),
  INDEX idx_tag(tag_id),
  CONSTRAINT fk_ctr_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
  CONSTRAINT fk_ctr_tag FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容标签关系表';


-- 评论表
CREATE TABLE comment (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '内容ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '评论用户ID',
  parent_id BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID（顶级评论为NULL）',
  reply_to_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '回复目标用户ID（回复某人时使用）',
  content TEXT NOT NULL COMMENT '评论内容',
  like_count INT DEFAULT 0 COMMENT '点赞数',
  status TINYINT DEFAULT 0 COMMENT '状态：0草稿，1待审核，2已发布，3已删除',
  version INT DEFAULT 0 COMMENT '乐观锁版本号',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_content_id(content_id),
  INDEX idx_user_id(user_id),
  INDEX idx_parent_id(parent_id),
  INDEX idx_status(status),
  INDEX idx_create_time(create_time),
  CONSTRAINT fk_comment_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容评论表';

-- 统计表
CREATE TABLE statistics (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '内容ID',
  view_count INT DEFAULT 0 COMMENT '浏览次数',
  like_count INT DEFAULT 0 COMMENT '点赞次数',
  favorite_count INT DEFAULT 0 COMMENT '收藏次数',
  comment_count INT DEFAULT 0 COMMENT '评论次数',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_content_id(content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容统计表';

-- 内容行为表
CREATE TABLE user_action (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  target_id BIGINT UNSIGNED NOT NULL COMMENT '目标对象ID（如内容、评论、用户等）',
  target_type TINYINT NOT NULL COMMENT '目标类型（0：content，1：comment，2：user 等）',
  action_type TINYINT NOT NULL COMMENT '行为类型（0：view，1：like，2：favorite，3：report 等）',
  status TINYINT DEFAULT 1 COMMENT '状态（1：激活，0：取消）',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_target_action(user_id, target_id, target_type, action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为记录表';


-- 审核历史记录表（追加模式，支持审核追溯）
CREATE TABLE audit_log (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  target_id BIGINT UNSIGNED NOT NULL COMMENT '目标ID（内容ID或评论ID）',
  target_type TINYINT NOT NULL COMMENT '目标类型：0-内容，1-评论',
  from_status TINYINT COMMENT '审核前状态：0-草稿，1-待审核，2-已通过，3-已删除',
  to_status TINYINT NOT NULL COMMENT '审核后状态：0-草稿，1-待审核，2-已通过，3-已删除',
  audit_reason VARCHAR(500) COMMENT '审核原因/备注',
  audit_by BIGINT UNSIGNED COMMENT '审核人ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_target(target_id, target_type),
  INDEX idx_create_time(create_time),
  INDEX idx_audit_by(audit_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核历史记录表';

-- ========================================
-- 收藏功能扩展
-- ========================================

-- 收藏夹表
CREATE TABLE favorite_folder (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  name VARCHAR(64) NOT NULL COMMENT '收藏夹名称',
  description VARCHAR(255) COMMENT '收藏夹描述',
  is_default TINYINT DEFAULT 0 COMMENT '是否默认收藏夹（0否，1是）',
  is_public TINYINT DEFAULT 0 COMMENT '是否公开（0私密，1公开）',
  sort_order INT DEFAULT 0 COMMENT '排序序号',
  item_count INT DEFAULT 0 COMMENT '收藏内容数量',
  status TINYINT DEFAULT 1 COMMENT '状态（0已删除，1正常）',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id(user_id),
  INDEX idx_status(status),
  INDEX idx_sort(user_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏夹表';

-- 收藏内容表
CREATE TABLE favorite_item (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  folder_id BIGINT UNSIGNED NOT NULL COMMENT '收藏夹ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '内容ID',
  note TEXT COMMENT '收藏备注',
  sort_order INT DEFAULT 0 COMMENT '在收藏夹内的排序',
  status TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_folder_content(folder_id, content_id),
  INDEX idx_user_id(user_id),
  INDEX idx_content_id(content_id),
  INDEX idx_folder_sort(folder_id, sort_order),
  INDEX idx_status(status),
  INDEX idx_create_time(create_time),
  CONSTRAINT fk_favorite_folder FOREIGN KEY (folder_id) REFERENCES favorite_folder(id) ON DELETE CASCADE,
  CONSTRAINT fk_favorite_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏内容表';

-- ========================================
-- 测试数据插入
-- ========================================

-- 插入分类
INSERT INTO category (name, code, parent_id, status) VALUES
('技术', 'tech', NULL, 1),
('生活', 'life', NULL, 1),
('Java', 'java', 1, 1),
('数据库', 'db', 1, 1);

-- 插入标签
INSERT INTO tag (name, description) VALUES
('SpringBoot', '与SpringBoot相关'),
('MySQL', 'MySQL数据库'),
('健康', '健康生活'),
('旅行', '旅游经历');

-- 插入内容主表
INSERT INTO content (title, type, description, category_id, uploaded_by, status) VALUES
('Spring Boot 教程', 0, '全面介绍Spring Boot的使用方法', 3, 1, 2),
('MySQL优化技巧', 1, '常见MySQL性能优化方案', 4, 1, 2),
('如何保持健康生活？', 2, '有哪些保持健康的建议？', 2, 2, 2),
('Java设计模式PPT', 3, '适合初学者的设计模式PPT', 3, 1, 2),
('一次难忘的旅行', 4, '视频记录了我的旅行日记', 2, 2, 2);

-- 插入文章详情
INSERT INTO article (content_id, content) VALUES
(1, 'Spring Boot 是一个快速开发框架，主要用于简化Spring应用的搭建和配置。');

-- 插入笔记详情
INSERT INTO note (content_id, content, format) VALUES
(2, '# MySQL优化\n- 使用索引\n- 避免SELECT *', 'markdown');

-- 插入问题详情
INSERT INTO question (content_id, content, answer_count, solved) VALUES
(3, '长期久坐办公，如何保证身体健康？', 2, 1);

-- 插入文件详情（使用示例UUID，文件元数据通过 Media 服务获取）
INSERT INTO file (content_id, file_id) VALUES
(4, 'a1b2c3d4-e5f6-7890-abcd-ef1234567890');

-- 插入视频详情（使用示例UUID，视频元数据通过 Media 服务获取）
INSERT INTO video (content_id, video_file_id) VALUES
(5, 'b2c3d4e5-f6a7-8901-bcde-f12345678901');

-- 内容-标签关联
INSERT INTO tag_relation (content_id, tag_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(5, 4);

-- 插入评论（状态：0=草稿，1=待审核，2=已发布，3=已删除）
INSERT INTO comment (content_id, user_id, parent_id, reply_to_user_id, content, like_count, status) VALUES
(1, 2, NULL, NULL, '写得很好，受益匪浅！', 5, 2),
(3, 3, NULL, NULL, '每天早上跑步30分钟是个好方法。', 3, 2),
(3, 4, 2, 3, '我也推荐打太极！', 1, 2);

-- 插入统计
INSERT INTO statistics (content_id, view_count, like_count, favorite_count, comment_count) VALUES
(1, 120, 10, 5, 1),
(2, 90, 8, 4, 0),
(3, 200, 5, 2, 2),
(4, 50, 1, 1, 0),
(5, 300, 15, 10, 1);

-- 插入用户行为
INSERT INTO user_action (user_id, target_id, target_type, action_type, status) VALUES
(2, 1, 0, 1, 1),
(2, 1, 0, 2, 1),
(3, 3, 0, 0, 1),
(4, 5, 0, 1, 1);

-- 插入审核历史记录（状态：0=草稿，1=待审核，2=已通过，3=已删除）
INSERT INTO audit_log (target_id, target_type, from_status, to_status, audit_reason, audit_by) VALUES
(1, 0, 1, 2, '内容质量较高，审核通过', 100),
(3, 0, 1, 2, '健康问题，已审核', 101),
(2, 0, 1, 2, '技术笔记无违规内容', 100);

-- 插入收藏夹测试数据
INSERT INTO favorite_folder (user_id, name, description, is_default, is_public, sort_order, item_count) VALUES
(2, '默认收藏夹', '系统自动创建的默认收藏夹', 1, 0, 0, 2),
(2, '技术学习', '收藏的技术类文章和资源', 0, 1, 1, 1),
(3, '默认收藏夹', '系统自动创建的默认收藏夹', 1, 0, 0, 1),
(4, '默认收藏夹', '系统自动创建的默认收藏夹', 1, 0, 0, 1);

-- 插入收藏内容测试数据
INSERT INTO favorite_item (folder_id, user_id, content_id, note, sort_order, status) VALUES
(1, 2, 1, '很实用的Spring Boot教程，值得反复学习', 0, 1),
(1, 2, 5, '旅行视频拍得不错', 1, 1),
(2, 2, 2, 'MySQL优化笔记，工作中经常用到', 0, 1),
(3, 3, 3, NULL, 0, 1),
(4, 4, 5, '想去同样的地方旅行', 0, 1);
