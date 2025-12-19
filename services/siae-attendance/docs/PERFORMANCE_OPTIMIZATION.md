# 考勤服务性能优化总结

本文档总结了考勤服务的性能优化实现，包括 Redis 缓存、数据库索引优化和接口限流。

## 1. Redis 缓存优化

### 1.1 缓存配置

已实现 Redis 配置类 `RedisConfig.java`，配置了：
- RedisTemplate 序列化方式（使用 Jackson2JsonRedisSerializer）
- CacheManager 缓存管理器
- 默认缓存过期时间：1小时

### 1.2 缓存策略

#### 考勤规则缓存
- **缓存位置**：`RuleServiceImpl`
- **缓存内容**：
  - 用户适用规则（缓存时间：1小时）
  - 活动考勤规则（使用 Spring Cache 注解）
- **缓存更新**：规则创建、更新、删除时自动清除缓存
- **缓存 Key**：`attendance:user:rule:{userId}:{date}`

#### 考勤统计缓存
- **缓存位置**：`StatisticsServiceImpl`
- **缓存内容**：
  - 个人考勤统计（缓存时间：30分钟）
  - 部门考勤统计（缓存时间：30分钟）
  - 活动考勤统计（使用 Spring Cache 注解）
- **缓存 Key**：
  - 个人统计：`attendance:statistics:personal:{userId}:{startDate}:{endDate}`
  - 部门统计：`attendance:statistics:department:{departmentId}:{startDate}:{endDate}`

### 1.3 缓存常量

定义在 `CacheConstants.java` 中：
```java
// 规则缓存
CACHE_ATTENDANCE_RULE = "attendance:rule"
CACHE_USER_APPLICABLE_RULE = "attendance:user:rule"
CACHE_ACTIVITY_RULE = "attendance:activity:rule"

// 统计缓存
CACHE_PERSONAL_STATISTICS = "attendance:statistics:personal"
CACHE_DEPARTMENT_STATISTICS = "attendance:statistics:department"
CACHE_ACTIVITY_STATISTICS = "attendance:activity:statistics"

// 防重复缓存
CACHE_CHECK_IN_DUPLICATE = "attendance:checkin:duplicate"
CACHE_CHECK_OUT_DUPLICATE = "attendance:checkout:duplicate"
```

### 1.4 缓存优势

1. **减少数据库查询**：高频查询的规则和统计数据从缓存读取
2. **提升响应速度**：缓存命中时响应时间从数十毫秒降至几毫秒
3. **降低数据库负载**：减少数据库连接和查询压力
4. **支持高并发**：Redis 的高性能支持更高的并发访问

## 2. 数据库索引优化

### 2.1 索引优化建议

详细的索引优化建议请参考 `DATABASE_INDEX_OPTIMIZATION.md` 文档。

主要优化点：
1. **attendance_record 表**：添加复合索引优化用户考勤记录查询
2. **attendance_anomaly 表**：添加索引优化异常查询和统计
3. **leave_request 表**：添加索引优化请假冲突检测和审批查询
4. **attendance_rule 表**：添加索引优化规则匹配查询
5. **attendance_statistics 表**：添加索引优化统计数据查询
6. **operation_log 表**：添加索引优化日志查询

### 2.2 索引设计原则

1. **复合索引优先**：将多个查询条件组合成复合索引
2. **最左前缀原则**：索引列顺序按查询频率和选择性排序
3. **覆盖索引**：尽可能让索引包含查询所需的所有列
4. **避免过度索引**：平衡查询性能和写入性能

## 3. 接口限流（已实现但暂时禁用）

### 3.1 限流实现

已实现基于 Redis + Lua 脚本的分布式限流：

#### 限流组件
- **注解**：`@RateLimit` - 标记需要限流的接口
- **切面**：`RateLimitAspect` - 拦截并执行限流逻辑
- **配置**：`RateLimitConfig` - 限流规则配置

#### 限流算法
使用滑动窗口算法（基于 Redis ZSET）：
```lua
-- 清除窗口外的记录
redis.call('ZREMRANGEBYSCORE', key, 0, clearBefore)
-- 检查当前窗口内的请求数
local amount = redis.call('ZCARD', key)
if amount < limit then
    -- 允许请求，记录时间戳
    redis.call('ZADD', key, now, now)
    return 1
else
    -- 拒绝请求
    return 0
end
```

### 3.2 限流配置

默认限流规则（在 `application-dev.yaml` 中配置）：
```yaml
attendance:
  rate-limit:
    enabled: false  # 暂时禁用
    check-in:
      permits: 10   # 每分钟10次
      window: 60
    check-out:
      permits: 10   # 每分钟10次
      window: 60
    query:
      permits: 100  # 每分钟100次
      window: 60
    export:
      permits: 5    # 每分钟5次
      window: 60
```

### 3.3 限流类型

支持三种限流类型：
1. **USER**：按用户限流（默认）
2. **IP**：按 IP 地址限流
3. **GLOBAL**：全局限流

### 3.4 使用方式

在 Controller 方法上添加注解（目前已注释）：
```java
@RateLimit(key = "attendance:check-in", type = RateLimit.RateLimitType.USER)
public Result<AttendanceRecordVO> checkIn(@Valid @RequestBody CheckInDTO dto) {
    // ...
}
```

### 3.5 启用限流

如需启用限流功能：
1. 将 `application-dev.yaml` 中的 `enabled` 设置为 `true`
2. 取消 Controller 中 `@RateLimit` 注解的注释
3. 根据实际需求调整限流参数

## 4. 性能优化效果预估

### 4.1 缓存优化效果

| 场景 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 规则查询 | 20-50ms | 2-5ms | 80-90% |
| 统计查询 | 100-300ms | 5-10ms | 95-97% |
| 高并发场景 | 数据库压力大 | Redis 承载 | 显著降低 |

### 4.2 索引优化效果

| 场景 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 用户考勤查询 | 全表扫描 | 索引查询 | 90%+ |
| 异常统计 | 慢查询 | 快速查询 | 80%+ |
| 请假冲突检测 | 较慢 | 快速 | 70%+ |

### 4.3 限流保护效果

- **防止恶意攻击**：限制单个用户/IP 的请求频率
- **保护系统稳定**：防止突发流量导致系统崩溃
- **公平资源分配**：确保所有用户都能正常访问

## 5. 监控和维护

### 5.1 缓存监控

建议监控以下指标：
- 缓存命中率
- 缓存大小
- 缓存过期情况
- Redis 内存使用

### 5.2 数据库监控

建议监控以下指标：
- 慢查询日志
- 索引使用情况
- 查询执行计划
- 数据库连接数

### 5.3 限流监控

建议监控以下指标：
- 限流触发次数
- 被限流的用户/IP
- 限流规则效果
- 系统负载情况

## 6. 后续优化建议

1. **缓存预热**：系统启动时预加载热点数据
2. **缓存降级**：Redis 故障时的降级策略
3. **读写分离**：数据库读写分离提升性能
4. **分库分表**：数据量大时考虑分库分表
5. **异步处理**：统计计算等耗时操作异步化
6. **CDN 加速**：静态资源使用 CDN 加速

## 7. 注意事项

1. **缓存一致性**：确保缓存与数据库数据一致
2. **缓存雪崩**：设置合理的过期时间，避免同时失效
3. **缓存穿透**：对不存在的数据也进行缓存
4. **索引维护**：定期分析和优化索引
5. **限流策略**：根据实际业务调整限流参数
