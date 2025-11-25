# 活动考勤功能使用指南

## 概述

活动考勤功能是考勤服务的扩展功能，专门用于管理特定活动（如会议、培训、团建等）的考勤。与日常考勤不同，活动考勤具有以下特点：

- **临时性**：通常只在活动当天有效
- **灵活性**：可以为每个活动设置独立的考勤规则
- **独立统计**：活动考勤数据独立统计，不影响日常考勤统计

## 核心概念

### 考勤类型（AttendanceType）

系统支持两种考勤类型：

- **DAILY (0)**: 日常考勤 - 用于常规的每日考勤管理
- **ACTIVITY (1)**: 活动考勤 - 用于特定活动的考勤管理

### 数据模型

活动考勤使用与日常考勤相同的数据表，通过以下字段区分：

- `attendance_type`: 考勤类型（0-日常考勤，1-活动考勤）
- `related_id`: 关联ID（活动考勤时存储活动ID）

## 功能特性

### 1. 活动考勤规则创建

为特定活动创建独立的考勤规则，支持：

- 自定义签到签退时间窗口
- 位置验证（可选）
- 迟到/早退阈值设置
- 适用对象设置（全体/部门/个人）

**API接口**：
```
POST /api/v1/attendance/activity/rules
```

**请求示例**：
```json
{
  "name": "年度总结大会考勤规则",
  "description": "2024年度总结大会考勤",
  "attendanceType": "ACTIVITY",
  "relatedId": 12345,
  "targetType": "ALL",
  "checkInStartTime": "08:00:00",
  "checkInEndTime": "09:00:00",
  "checkOutStartTime": "17:00:00",
  "checkOutEndTime": "18:00:00",
  "lateThresholdMinutes": 10,
  "earlyThresholdMinutes": 10,
  "locationRequired": true,
  "allowedLocations": "[{\"name\":\"会议中心\",\"latitude\":39.9042,\"longitude\":116.4074}]",
  "locationRadiusMeters": 100,
  "effectiveDate": "2024-12-01",
  "expiryDate": "2024-12-01",
  "priority": 10
}
```

**权限要求**：`RULE_CREATE`

### 2. 活动考勤规则查询

查询所有活动考勤规则或特定活动的规则。

**API接口**：
```
GET /api/v1/attendance/activity/rules?activityId={activityId}
```

**参数说明**：
- `activityId` (可选): 活动ID，不传则查询所有活动考勤规则

**权限要求**：`RULE_LIST`

### 3. 活动考勤记录查询

#### 3.1 分页查询

**API接口**：
```
POST /api/v1/attendance/activity/records/page
```

**请求示例**：
```json
{
  "pageNum": 1,
  "pageSize": 20,
  "params": {
    "relatedId": 12345,
    "userId": 1001,
    "startDate": "2024-12-01",
    "endDate": "2024-12-01"
  }
}
```

**权限要求**：`ATTENDANCE_LIST`

#### 3.2 列表查询（不分页）

**API接口**：
```
GET /api/v1/attendance/activity/records?activityId={activityId}&userId={userId}
```

**参数说明**：
- `activityId` (必填): 活动ID
- `userId` (可选): 用户ID，用于筛选特定用户的记录

**权限要求**：`ATTENDANCE_LIST`

#### 3.3 个人活动考勤历史

查询当前用户参与的所有活动考勤记录。

**API接口**：
```
GET /api/v1/attendance/activity/records/my-history?startDate={startDate}&endDate={endDate}&pageNum={pageNum}&pageSize={pageSize}
```

**参数说明**：
- `startDate` (可选): 开始日期
- `endDate` (可选): 结束日期
- `pageNum` (可选): 页码，默认1
- `pageSize` (可选): 每页条数，默认10

**权限要求**：已认证用户

### 4. 活动考勤统计

统计特定活动的考勤情况，包括：

- 总记录数
- 签到人数
- 签退人数
- 完成考勤人数（签到且签退）
- 异常记录数
- 平均考勤时长

**API接口**：
```
GET /api/v1/attendance/activity/statistics/{activityId}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "activityId": 12345,
    "totalRecords": 150,
    "checkInCount": 148,
    "checkOutCount": 145,
    "completedCount": 143,
    "abnormalCount": 7,
    "averageDurationMinutes": 480
  }
}
```

**权限要求**：`STATISTICS_VIEW`

## 使用流程

### 典型使用场景：组织一场活动

#### 步骤1：创建活动考勤规则

在活动开始前，管理员为活动创建考勤规则：

```bash
curl -X POST http://localhost:8080/api/v1/attendance/activity/rules \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "技术分享会考勤",
    "relatedId": 12345,
    "attendanceType": "ACTIVITY",
    "targetType": "ALL",
    "checkInStartTime": "14:00:00",
    "checkInEndTime": "14:30:00",
    "checkOutStartTime": "17:00:00",
    "checkOutEndTime": "17:30:00",
    "effectiveDate": "2024-12-15",
    "expiryDate": "2024-12-15"
  }'
```

#### 步骤2：参与者签到

活动开始时，参与者使用签到接口进行签到：

```bash
curl -X POST http://localhost:8080/api/v1/attendance/check-in \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "userId": 1001,
    "timestamp": "2024-12-15T14:15:00",
    "location": "会议室A",
    "attendanceType": 1,
    "relatedId": 12345
  }'
```

#### 步骤3：参与者签退

活动结束时，参与者进行签退：

```bash
curl -X POST http://localhost:8080/api/v1/attendance/check-out \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "userId": 1001,
    "timestamp": "2024-12-15T17:10:00"
  }'
```

#### 步骤4：查看活动考勤统计

活动结束后，管理员查看考勤统计：

```bash
curl -X GET http://localhost:8080/api/v1/attendance/activity/statistics/12345 \
  -H "Authorization: Bearer {token}"
```

#### 步骤5：导出活动考勤记录

如需导出活动考勤记录，可以使用通用的导出接口：

```bash
curl -X GET "http://localhost:8080/api/v1/attendance/records/export?startDate=2024-12-15&endDate=2024-12-15&format=csv" \
  -H "Authorization: Bearer {token}" \
  --output activity_attendance.csv
```

然后手动筛选 `attendance_type = 1` 且 `related_id = 12345` 的记录。

## 权限配置

活动考勤功能使用以下权限：

| 权限代码 | 说明 | 适用角色 |
|---------|------|---------|
| `RULE_CREATE` | 创建考勤规则 | 管理员 |
| `RULE_LIST` | 查询规则列表 | 管理员、部门负责人 |
| `ATTENDANCE_LIST` | 查询考勤记录 | 管理员、部门负责人 |
| `STATISTICS_VIEW` | 查看统计数据 | 管理员、部门负责人 |
| 已认证用户 | 签到签退、查看个人记录 | 所有用户 |

## 最佳实践

### 1. 规则设置建议

- **时间窗口**：建议签到窗口设置为活动开始前30分钟到开始后30分钟
- **位置验证**：对于重要活动建议启用位置验证
- **优先级**：活动考勤规则的优先级应高于日常考勤规则（建议设置为10以上）
- **有效期**：活动考勤规则通常设置 `effectiveDate = expiryDate`（只在活动当天有效）

### 2. 异常处理

- **迟到/早退**：系统会自动根据规则检测迟到和早退，并创建异常记录
- **缺勤**：如果参与者未签到，需要手动创建缺勤异常记录
- **漏签退**：如果参与者只签到未签退，系统会标记为异常

### 3. 数据查询优化

- 查询活动考勤记录时，始终指定 `relatedId`（活动ID）以提高查询效率
- 对于大型活动（参与人数>1000），建议使用分页查询
- 统计数据会实时计算，对于频繁查询的场景可以考虑缓存

## 与日常考勤的区别

| 特性 | 日常考勤 | 活动考勤 |
|-----|---------|---------|
| 考勤类型 | DAILY (0) | ACTIVITY (1) |
| 关联ID | NULL | 活动ID |
| 规则有效期 | 长期有效 | 通常只在活动当天有效 |
| 统计方式 | 按月统计 | 按活动统计 |
| 请假联动 | 支持 | 不支持（活动考勤不受请假影响） |
| 自动缺勤检测 | 支持（定时任务） | 不支持（需手动处理） |

## 技术实现

### 数据库设计

活动考勤复用日常考勤的数据表，通过 `attendance_type` 和 `related_id` 字段区分：

```sql
-- 查询活动考勤记录
SELECT * FROM attendance_record 
WHERE attendance_type = 1 
  AND related_id = 12345;

-- 查询活动考勤规则
SELECT * FROM attendance_rule 
WHERE attendance_type = 1 
  AND related_id = 12345;
```

### 服务层实现

- **ActivityAttendanceController**: 活动考勤专用控制器
- **IRuleService.getActivityRule()**: 查询活动考勤规则
- **IStatisticsService.calculateActivityStatistics()**: 计算活动考勤统计

### 扩展性

当前实现支持以下扩展：

1. **多活动并发**：系统支持同时进行多个活动的考勤管理
2. **自定义规则**：每个活动可以有独立的考勤规则
3. **灵活统计**：可以按活动、按用户、按时间范围进行统计

## 常见问题

### Q1: 活动考勤是否会影响日常考勤统计？

A: 不会。活动考勤（`attendance_type = 1`）和日常考勤（`attendance_type = 0`）是完全独立的，统计时会分别计算。

### Q2: 一个用户可以同时参加多个活动吗？

A: 可以。系统支持用户在同一天参加多个活动，每个活动的考勤记录通过 `related_id` 区分。

### Q3: 活动考勤规则的优先级如何设置？

A: 建议将活动考勤规则的优先级设置为10以上，确保在活动当天优先使用活动规则而不是日常规则。

### Q4: 如何处理活动考勤的异常情况？

A: 活动考勤的异常检测与日常考勤相同，系统会自动检测迟到、早退等异常。对于缺勤，需要管理员手动创建异常记录。

### Q5: 活动考勤记录可以修改吗？

A: 考勤记录一旦创建不建议修改。如有特殊情况，可以通过异常处理流程进行调整。

## 未来规划

以下功能计划在后续版本中实现：

1. **活动考勤模板**：预设常用的活动考勤规则模板
2. **批量签到**：支持管理员批量为参与者签到
3. **活动考勤报表**：生成活动考勤专用报表
4. **活动考勤提醒**：活动开始前自动提醒参与者
5. **二维码签到**：支持扫描二维码进行活动签到

## 相关文档

- [考勤服务总体设计文档](./README.md)
- [考勤规则配置指南](./MULTI_SHIFT_GUIDE.md)
- [权限配置说明](./PERMISSION_GUIDE.md)
- [操作日志使用说明](./OPERATION_LOG_USAGE.md)

## 联系支持

如有问题或建议，请联系：

- 技术支持：support@siae.com
- 开发团队：dev@siae.com
