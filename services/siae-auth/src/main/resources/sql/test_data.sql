USE auth_db;

-- 删除已有数据（可选）
-- DELETE FROM login_log;

-- 创建用于生成序列的通用数字表（普通表，非临时表）
DROP TABLE IF EXISTS seq_100;
CREATE TABLE seq_100 (n INT PRIMARY KEY);
INSERT INTO seq_100 (n) VALUES
                            (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),
                            (11),(12),(13),(14),(15),(16),(17),(18),(19),(20),
                            (21),(22),(23),(24),(25),(26),(27),(28),(29),(30),
                            (31),(32),(33),(34),(35),(36),(37),(38),(39),(40),
                            (41),(42),(43),(44),(45),(46),(47),(48),(49),(50),
                            (51),(52),(53),(54),(55),(56),(57),(58),(59),(60),
                            (61),(62),(63),(64),(65),(66),(67),(68),(69),(70),
                            (71),(72),(73),(74),(75),(76),(77),(78),(79),(80),
                            (81),(82),(83),(84),(85),(86),(87),(88),(89),(90),
                            (91),(92),(93),(94),(95),(96),(97),(98),(99),(100);

-- 插入更多用户（增加用户基数，让图表更丰富）
DROP TABLE IF EXISTS test_users;
CREATE TABLE test_users (user_id INT PRIMARY KEY);
INSERT INTO test_users (user_id) VALUES
    (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),
    (11),(12),(13),(14),(15),(16),(17),(18),(19),(20),
    (21),(22),(23),(24),(25),(26),(27),(28),(29),(30);

-- 插入登录日志 - 使用更真实的增长趋势
INSERT INTO login_log (user_id, username, login_ip, status, msg, login_time)
SELECT
    u.user_id,
    CONCAT('user', u.user_id),
    CONCAT('192.168.', FLOOR(1 + RAND() * 254), '.', FLOOR(1 + RAND() * 254)) AS login_ip,
    -- 80% 成功率
    IF(RAND() < 0.8, 1, 0) AS status,
    IF(RAND() < 0.8, '登录成功', '密码错误') AS msg,
    DATE_ADD(DATE_ADD(CURDATE() - INTERVAL d.n DAY, INTERVAL (8 + FLOOR(RAND() * 15)) HOUR),
             INTERVAL FLOOR(RAND() * 3600) SECOND) AS login_time
FROM
    test_users u
        JOIN
    seq_100 d ON d.n <= 90  -- 最近90天
WHERE
    -- 根据天数创建增长趋势：越近期登录越频繁
    RAND() < (
        CASE
            WHEN d.n <= 7 THEN 0.9    -- 最近7天：90%的用户每天登录
            WHEN d.n <= 30 THEN 0.7   -- 最近30天：70%的用户每天登录
            WHEN d.n <= 60 THEN 0.5   -- 最近60天：50%的用户每天登录
            ELSE 0.3                  -- 60天以前：30%的用户每天登录
        END
    )
    -- 周末登录率降低
    AND (
        DAYOFWEEK(CURDATE() - INTERVAL d.n DAY) NOT IN (1, 7)  -- 非周末
        OR RAND() < 0.5  -- 周末50%概率登录
    );

-- 为每个用户在每天生成1-3次登录记录（活跃用户）
INSERT INTO login_log (user_id, username, login_ip, status, msg, login_time)
SELECT
    u.user_id,
    CONCAT('user', u.user_id),
    CONCAT('192.168.', FLOOR(1 + RAND() * 254), '.', FLOOR(1 + RAND() * 254)) AS login_ip,
    1 AS status,
    '登录成功' AS msg,
    DATE_ADD(DATE_ADD(CURDATE() - INTERVAL d.n DAY, INTERVAL (14 + FLOOR(RAND() * 6)) HOUR),
             INTERVAL FLOOR(RAND() * 3600) SECOND) AS login_time
FROM
    test_users u
        JOIN
    seq_100 d ON d.n <= 90
        JOIN
    seq_100 t ON t.n <= 2  -- 每天额外1-2次登录
WHERE
    u.user_id <= 10  -- 只有前10个用户是活跃用户
    AND RAND() < (
        CASE
            WHEN d.n <= 7 THEN 0.8
            WHEN d.n <= 30 THEN 0.6
            ELSE 0.4
        END
    );

-- ================================================================
-- 说明：
-- 1. 创建了30个测试用户
-- 2. 生成最近 90 天的登录数据，有明显的增长趋势：
--    - 最近7天：每天约 25-30 人登录
--    - 最近30天：每天约 20-25 人登录
--    - 最近60天：每天约 15-20 人登录
--    - 60天前：每天约 10-15 人登录
-- 3. 周末登录率降低，模拟真实场景
-- 4. 前10个用户是活跃用户，每天登录2-3次
-- 5. 登录成功率约80%
-- 6. 时间随机分布在 8:00~22:59:59
-- ================================================================
