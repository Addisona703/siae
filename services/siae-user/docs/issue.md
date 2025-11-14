新增两张表
```sql
CREATE TABLE member_department (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT UNSIGNED NOT NULL,
department_id BIGINT UNSIGNED NOT NULL,
join_date DATE NOT NULL,
has_position TINYINT DEFAULT 0 COMMENT '是否在该部门担任职位：0否，1是',
FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE RESTRICT
);

CREATE TABLE user_position (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT UNSIGNED NOT NULL,
position_id BIGINT UNSIGNED NOT NULL,
department_id BIGINT UNSIGNED NULL COMMENT 'NULL 表示全协会职位',
start_date DATE NOT NULL DEFAULT CURRENT_DATE,
end_date DATE NULL COMMENT '为空表示还在任',
FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
FOREIGN KEY (position_id) REFERENCES position(id) ON DELETE RESTRICT,
FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE RESTRICT
);
```
离任怎么收回
只要执行：

UPDATE user_role
SET end_date = CURRENT_DATE
WHERE id = ?;

应用层的权限查询加一句：

WHERE end_date IS NULL

离任马上失效，不需要额外逻辑。

需要通过消息队列发消息给auth服务更新用户角色

删除member表的部门和职位id字段还有候选成员的部门id字段，改用上面两个表映射