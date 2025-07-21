USE content_db;

-- 插入分类
INSERT INTO content_category (name, code, parent_id, status) VALUES 
('技术', 'tech', NULL, 1),
('生活', 'life', NULL, 1),
('Java', 'java', 1, 1),
('数据库', 'db', 1, 1);

-- 插入标签
INSERT INTO content_tag (name, description) VALUES 
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
INSERT INTO content_article (content_id, content, cover_url) VALUES 
(1, 'Spring Boot 是一个快速开发框架，主要用于简化Spring应用的搭建和配置。', 'https://example.com/cover/springboot.png');

-- 插入笔记详情
INSERT INTO content_note (content_id, content, format) VALUES 
(2, '# MySQL优化\n- 使用索引\n- 避免SELECT *', 'markdown');

-- 插入问题详情
INSERT INTO content_question (content_id, content, answer_count, solved) VALUES 
(3, '长期久坐办公，如何保证身体健康？', 2, 1);

-- 插入文件详情
INSERT INTO content_file (content_id, file_name, file_path, file_size, file_type) VALUES 
(4, 'Java设计模式.pdf', '/upload/java/design-patterns.pdf', 2048000, 'application/pdf');

-- 插入视频详情
INSERT INTO content_video (content_id, video_url, duration, cover_url, resolution) VALUES 
(5, 'https://example.com/video/travel.mp4', 360, 'https://example.com/video/cover.jpg', '1080p');

-- 内容-标签关联
INSERT INTO content_tag_relation (content_id, tag_id) VALUES 
(1, 1),
(2, 2),
(3, 3),
(5, 4);

-- 插入评论
INSERT INTO content_comment (content_id, user_id, parent_id, content, status) VALUES 
(1, 2, NULL, '写得很好，受益匪浅！', 1),
(3, 3, NULL, '每天早上跑步30分钟是个好方法。', 1),
(3, 4, 2, '我也推荐打太极！', 1);

-- 插入统计
INSERT INTO content_statistics (content_id, view_count, like_count, favorite_count, comment_count) VALUES 
(1, 120, 10, 5, 1),
(2, 90, 8, 4, 0),
(3, 200, 5, 2, 2),
(4, 50, 1, 1, 0),
(5, 300, 15, 10, 1);

-- 插入用户行为
INSERT INTO content_user_action (user_id, target_id, target_type, action_type, status) VALUES 
(2, 1, 0, 1, 1),
(2, 1, 0, 2, 1),
(3, 3, 0, 0, 1),
(4, 5, 0, 1, 1);

-- 插入审核记录
INSERT INTO content_audit (target_id, target_type, audit_status, audit_reason, audit_by) VALUES 
(1, 0, 1, '内容质量较高，审核通过', 100),
(3, 0, 1, '健康问题，已审核', 101),
(2, 0, 1, '技术笔记无违规内容', 100);
