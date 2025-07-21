DROP DATABASE IF EXISTS content_db;
CREATE DATABASE IF NOT EXISTS content_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE content_db;

-- 内容主表
CREATE TABLE content (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  title VARCHAR(255) NOT NULL COMMENT '资源标题',
  type TINYINT NOT NULL COMMENT '资源类型（0文章、1笔记、2提问、3文件、4视频）',
  description TEXT COMMENT '资源摘要，用于列表页或预览页展示',
  category_id BIGINT UNSIGNED DEFAULT 0 COMMENT '关联的分类ID，外键，指向 content_category 表',
  uploaded_by BIGINT UNSIGNED NOT NULL COMMENT '上传者/作者用户 ID',
  status TINYINT DEFAULT 0 COMMENT '状态：0草稿，1待审核，2已发布，3已删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认当前时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认当前时间，更新时自动刷新',
  INDEX idx_type(type),
  INDEX idx_status(status),
  INDEX idx_uploaded_by(uploaded_by),
  INDEX idx_create_time(create_time),
  INDEX idx_category_id(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容主表';

-- 文章详情表
CREATE TABLE content_article (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '关联的内容ID，外键，指向 content 表',
  content LONGTEXT NOT NULL COMMENT '文章正文内容',
  cover_url VARCHAR(512) COMMENT '封面图片URL',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认当前时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认当前时间，更新时自动刷新',
  INDEX idx_content_id(content_id),
  CONSTRAINT fk_article_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章详情表';

-- 问题详情表
CREATE TABLE content_question (
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
CREATE TABLE content_note (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '关联的内容ID，外键，指向 content 表',
  content LONGTEXT NOT NULL COMMENT '笔记内容',
  format VARCHAR(32) DEFAULT 'markdown' COMMENT '笔记格式：markdown/rich_text',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认当前时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认当前时间，更新时自动刷新',
  INDEX idx_content_id(content_id),
  CONSTRAINT fk_note_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记详情表';

-- 文件详情表
CREATE TABLE content_file (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '关联的内容ID，外键，指向 content 表',
  file_name VARCHAR(255) NOT NULL COMMENT '文件名称',
  file_path VARCHAR(512) NOT NULL COMMENT '文件存储路径',
  file_size BIGINT NOT NULL COMMENT '文件大小，单位：字节',
  file_type VARCHAR(32) NOT NULL COMMENT '文件MIME类型',
  download_count INT DEFAULT 0 COMMENT '下载次数',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认当前时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认当前时间，更新时自动刷新',
  INDEX idx_content_id(content_id),
  CONSTRAINT fk_file_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件详情表';

-- 视频详情表
CREATE TABLE content_video (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '关联的内容ID，外键，指向 content 表',
  video_url VARCHAR(512) NOT NULL COMMENT '视频访问URL',
  duration INT NOT NULL COMMENT '视频时长，单位：秒',
  cover_url VARCHAR(512) COMMENT '视频封面图URL',
  resolution VARCHAR(32) COMMENT '视频分辨率',
  play_count INT DEFAULT 0 COMMENT '播放次数',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认当前时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认当前时间，更新时自动刷新',
  INDEX idx_content_id(content_id),
  CONSTRAINT fk_video_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频详情表';

-- 分类表
CREATE TABLE content_category (
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
CREATE TABLE content_tag (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  name VARCHAR(64) NOT NULL COMMENT '标签名称',
  description VARCHAR(255) COMMENT '标签描述',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容标签表';

-- 内容-标签关联表（多对多）
CREATE TABLE content_tag_relation (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '内容ID',
  tag_id BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关联创建时间',
  UNIQUE KEY uniq_content_tag (content_id, tag_id),
  INDEX idx_content(content_id),
  INDEX idx_tag(tag_id),
  CONSTRAINT fk_ctr_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
  CONSTRAINT fk_ctr_tag FOREIGN KEY (tag_id) REFERENCES content_tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容标签关系表';


-- 评论表
CREATE TABLE content_comment (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  content_id BIGINT UNSIGNED NOT NULL COMMENT '内容ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '评论用户ID',
  parent_id BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID',
  content TEXT NOT NULL COMMENT '评论内容',
  status TINYINT DEFAULT 0 COMMENT '状态：0待审核，1已发布，2已删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_content_id(content_id),
  INDEX idx_user_id(user_id),
  INDEX idx_parent_id(parent_id),
  INDEX idx_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容评论表';

-- 统计表
CREATE TABLE content_statistics (
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
CREATE TABLE content_user_action (
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


-- 审核记录表
CREATE TABLE content_audit (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  target_id BIGINT UNSIGNED NOT NULL COMMENT '被审核对象的主键ID',
  target_type TINYINT NOT NULL COMMENT '审核对象类型（如 0content、1comment）',
  audit_status TINYINT DEFAULT 0 COMMENT '审核状态（0待审核、1通过、2不通过）',
  audit_reason VARCHAR(255) COMMENT '审核意见',
  audit_by BIGINT UNSIGNED COMMENT '审核人用户ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_target(target_type, target_id),
  INDEX idx_status(audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';
