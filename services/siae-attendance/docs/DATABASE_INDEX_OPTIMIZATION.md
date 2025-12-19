# 数据库索引优化建议

本文档列出了考勤服务的数据库索引优化建议，用于提升查询性能。

## 索引优化原则

1. **复合索引的列顺序很重要**：应该按照查询条件的使用频率和选择性排序
2. **覆盖索引**：尽可能让索引包含查询所需的所有列
3. **避免过度索引**：每个索引都会增加写入开销
4. **定期维护**：使用 ANALYZE 命令更新表统计信息

## 推荐添加的索引

### 1. attendance_record 表

```sql
-- 复合索引：用户ID + 状态 + 日期（用于查询用户的已完成考勤记录）
CREATE INDEX idx_user_status_date ON attendance_record(user_id, status, attendance_date);

-- 复合索引：考勤日期 + 状态（用于按日期统计不同状态的考勤）
CREATE INDEX idx_date_status ON attendance_record(attendance_date, status);

-- 索引：规则ID（用于按规则查询考勤记录）
CREATE INDEX idx_rule_id ON attendance_record(rule_id);
```

**优化的查询场景**：
- 查询用户在特定时间段内的已完成考勤记录
- 按日期统计不同状态的考勤数量
- 查询使用特定规则的考勤记录

### 2. attendance_anomaly 表

```sql
-- 复合索引：用户ID + 异常类型 + 日期（用于查询用户特定类型的异常）
CREATE INDEX idx_user_type_date ON attendance_anomaly(user_id, anomaly_type, anomaly_date);

-- 复合索引：异常日期 + 处理状态（用于查询待处理的异常）
CREATE INDEX idx_date_resolved ON attendance_anomaly(anomaly_date, resolved);

-- 部分索引：被请假抑制字段（用于查询被请假抑制的异常）
CREATE INDEX idx_suppressed_by_leave ON attendance_anomaly(suppressed_by_leave) 
WHERE suppressed_by_leave IS NOT NULL;
```

**优化的查询场景**：
- 查询用户特定类型的异常记录
- 查询待处理的异常
- 查询被请假抑制的异常

### 3. leave_request 表

```sql
-- 复合索引：状态 + 审批人（用于查询待审批的请假）
CREATE INDEX idx_status_approver ON leave_request(status, approver_id);

-- 复合索引：用户ID + 日期范围（用于检查请假冲突）
CREATE INDEX idx_user_date_range ON leave_request(user_id, start_date, end_date);

-- 复合索引：状态 + 日期范围（用于查询特定时间段的已批准请假）
CREATE INDEX idx_status_date_range ON leave_request(status, start_date, end_date);
```

**优化的查询场景**：
- 查询待审批的请假申请
- 检查请假时间冲突
- 查询特定时间段的已批准请假

### 4. attendance_rule 表

```sql
-- 复合索引：状态 + 生效日期 + 失效日期 + 优先级（用于查询适用规则）
CREATE INDEX idx_status_effective_priority ON attendance_rule(
    status, effective_date, expiry_date, priority DESC
);

-- 复合索引：考勤类型 + 关联ID + 状态（用于快速查询活动规则）
CREATE INDEX idx_type_related_status ON attendance_rule(
    attendance_type, related_id, status
);
```

**优化的查询场景**：
- 查询适用的考勤规则
- 查询活动考勤规则

### 5. attendance_statistics 表

```sql
-- 索引：统计月份 + 出勤率（用于排序和筛选）
CREATE INDEX idx_month_rate ON attendance_statistics(stat_month, attendance_rate);
```

**优化的查询场景**：
- 按出勤率排序查询统计数据
- 筛选出勤率低于某个阈值的记录

### 6. operation_log 表

```sql
-- 复合索引：操作模块 + 操作类型 + 创建时间（用于日志查询）
CREATE INDEX idx_module_type_created ON operation_log(
    operation_module, operation_type, created_at DESC
);

-- 复合索引：用户ID + 创建时间（用于查询用户操作历史）
CREATE INDEX idx_user_created ON operation_log(user_id, created_at DESC);

-- 索引：状态（用于筛选成功/失败的操作）
CREATE INDEX idx_status ON operation_log(status);
```

**优化的查询场景**：
- 按模块和类型查询操作日志
- 查询用户操作历史
- 筛选失败的操作

## 索引维护

### 定期更新统计信息

```sql
-- 更新表统计信息，帮助查询优化器选择最佳索引
ANALYZE attendance_record;
ANALYZE attendance_anomaly;
ANALYZE leave_request;
ANALYZE attendance_rule;
ANALYZE attendance_statistics;
ANALYZE operation_log;
```

### 监控索引使用情况

```sql
-- 查看未使用的索引（MySQL）
SELECT 
    s.table_schema,
    s.table_name,
    s.index_name,
    s.cardinality
FROM information_schema.statistics s
LEFT JOIN information_schema.index_statistics i 
    ON s.table_schema = i.table_schema 
    AND s.table_name = i.table_name 
    AND s.index_name = i.index_name
WHERE s.table_schema = 'attendance_db'
    AND i.index_name IS NULL
    AND s.index_name != 'PRIMARY';
```

## 性能测试建议

在添加索引后，建议进行以下性能测试：

1. **查询性能测试**：对比添加索引前后的查询执行时间
2. **写入性能测试**：评估索引对插入、更新操作的影响
3. **存储空间评估**：监控索引占用的存储空间
4. **并发测试**：在高并发场景下测试索引的效果

## 注意事项

1. **索引不是越多越好**：每个索引都会增加写入开销和存储空间
2. **根据实际查询优化**：应该基于实际的查询模式添加索引
3. **定期审查**：随着业务发展，定期审查和调整索引策略
4. **监控慢查询**：使用慢查询日志识别需要优化的查询
