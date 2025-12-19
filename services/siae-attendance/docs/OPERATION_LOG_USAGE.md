# 操作日志使用指南

## 概述

操作日志功能通过 AOP 切面自动记录系统中的重要操作，包括签到、签退、请假申请、审批等敏感操作。

## 核心组件

### 1. @OperationLog 注解

用于标记需要记录操作日志的方法。

**属性说明：**

- `type`: 操作类型（必填），如：CHECK_IN, CHECK_OUT, CREATE, UPDATE, DELETE, APPROVE 等
- `module`: 操作模块（必填），如：ATTENDANCE, LEAVE, ANOMALY, RULE 等
- `description`: 操作描述（可选），支持 SpEL 表达式
- `recordParams`: 是否记录请求参数（默认 true）
- `recordResult`: 是否记录响应结果（默认 false）

### 2. OperationLogAspect 切面

自动拦截带有 `@OperationLog` 注解的方法，记录以下信息：

- 操作人ID（从 SecurityContext 获取）
- 操作类型和模块
- 操作描述
- 请求方法、URL、参数
- IP地址、User-Agent
- 执行时长
- 执行状态（成功/失败）
- 错误信息（如果失败）

### 3. OperationLogService

提供异步保存操作日志的功能，避免影响主业务流程性能。

## 使用示例

### 基本用法

```java
@PostMapping("/check-in")
@OperationLog(type = "CHECK_IN", module = "ATTENDANCE", description = "用户签到")
public Result<AttendanceRecordVO> checkIn(@Valid @RequestBody CheckInDTO dto) {
    // 业务逻辑
}
```

### 使用 SpEL 表达式

```java
@PostMapping("/leaves")
@OperationLog(
    type = "CREATE", 
    module = "LEAVE", 
    description = "创建请假申请: 类型=#dto.leaveType, 天数=#dto.days"
)
public Result<LeaveRequestVO> createLeave(@Valid @RequestBody LeaveRequestCreateDTO dto) {
    // 业务逻辑
}
```

### 记录响应结果

```java
@PostMapping("/leaves/{id}/approve")
@OperationLog(
    type = "APPROVE", 
    module = "LEAVE", 
    description = "审批请假申请: ID=#id",
    recordResult = true
)
public Result<LeaveRequestVO> approveLeave(@PathVariable Long id, 
                                           @RequestBody LeaveApprovalDTO dto) {
    // 业务逻辑
}
```

### 不记录请求参数

```java
@PostMapping("/sensitive-operation")
@OperationLog(
    type = "SENSITIVE", 
    module = "SYSTEM", 
    description = "敏感操作",
    recordParams = false
)
public Result<Void> sensitiveOperation(@RequestBody SensitiveDTO dto) {
    // 业务逻辑
}
```

## 已添加操作日志的接口

### AttendanceController

1. **签到** - `POST /check-in`
   - 类型: CHECK_IN
   - 模块: ATTENDANCE
   - 描述: 用户签到

2. **签退** - `POST /check-out`
   - 类型: CHECK_OUT
   - 模块: ATTENDANCE
   - 描述: 用户签退

3. **导出考勤记录** - `GET /records/export`
   - 类型: EXPORT
   - 模块: ATTENDANCE
   - 描述: 导出考勤记录

## 推荐的操作类型

### 通用操作类型
- `CREATE` - 创建
- `UPDATE` - 更新
- `DELETE` - 删除
- `QUERY` - 查询
- `EXPORT` - 导出
- `IMPORT` - 导入

### 考勤相关
- `CHECK_IN` - 签到
- `CHECK_OUT` - 签退

### 审批相关
- `APPROVE` - 批准
- `REJECT` - 拒绝
- `CANCEL` - 撤销

### 异常处理
- `HANDLE` - 处理异常
- `RESOLVE` - 解决问题

## 推荐的操作模块

- `ATTENDANCE` - 考勤管理
- `LEAVE` - 请假管理
- `ANOMALY` - 异常管理
- `RULE` - 规则管理
- `STATISTICS` - 统计报表
- `SYSTEM` - 系统管理

## 数据库表结构

操作日志存储在 `operation_log` 表中，包含以下字段：

- `id` - 主键ID
- `user_id` - 操作人ID
- `operation_type` - 操作类型
- `operation_module` - 操作模块
- `operation_desc` - 操作描述
- `request_method` - 请求方法（GET/POST等）
- `request_url` - 请求URL
- `request_params` - 请求参数（JSON格式）
- `response_result` - 响应结果（JSON格式）
- `ip_address` - IP地址
- `user_agent` - 用户代理
- `execution_time` - 执行时长（毫秒）
- `status` - 状态（0-失败，1-成功）
- `error_message` - 错误信息
- `created_at` - 创建时间

## 性能优化

1. **异步保存**：使用 `@Async` 注解异步保存日志，不阻塞主业务流程
2. **数据截断**：自动截断过长的参数和响应结果（限制5000字符）
3. **错误隔离**：日志保存失败不影响主业务执行

## 注意事项

1. 确保 Spring Boot 应用启用了异步支持（`@EnableAsync`）
2. 操作日志会自动过滤 HttpServletRequest 和 HttpServletResponse 对象
3. 对于敏感数据，建议设置 `recordParams = false`
4. SpEL 表达式中可以使用方法参数名称访问参数值
5. 日志保存失败只会记录错误日志，不会抛出异常

## 扩展建议

### 添加更多控制器方法的日志

在需要记录日志的 Controller 方法上添加 `@OperationLog` 注解：

```java
// LeaveController
@PostMapping("/")
@OperationLog(type = "CREATE", module = "LEAVE", description = "提交请假申请")
public Result<LeaveRequestVO> createLeave(@RequestBody LeaveRequestCreateDTO dto) {
    // ...
}

@PostMapping("/{id}/approve")
@OperationLog(type = "APPROVE", module = "LEAVE", description = "审批请假: ID=#id", recordResult = true)
public Result<LeaveRequestVO> approveLeave(@PathVariable Long id, @RequestBody LeaveApprovalDTO dto) {
    // ...
}

// AnomalyController
@PostMapping("/{id}/handle")
@OperationLog(type = "HANDLE", module = "ANOMALY", description = "处理考勤异常: ID=#id")
public Result<AttendanceAnomalyVO> handleAnomaly(@PathVariable Long id, @RequestBody AnomalyHandleDTO dto) {
    // ...
}

// RuleController
@PostMapping("/")
@OperationLog(type = "CREATE", module = "RULE", description = "创建考勤规则: #dto.name")
public Result<AttendanceRuleVO> createRule(@RequestBody AttendanceRuleCreateDTO dto) {
    // ...
}

@PutMapping("/{id}")
@OperationLog(type = "UPDATE", module = "RULE", description = "更新考勤规则: ID=#id")
public Result<AttendanceRuleVO> updateRule(@PathVariable Long id, @RequestBody AttendanceRuleUpdateDTO dto) {
    // ...
}

@DeleteMapping("/{id}")
@OperationLog(type = "DELETE", module = "RULE", description = "删除考勤规则: ID=#id")
public Result<Boolean> deleteRule(@PathVariable Long id) {
    // ...
}
```

### 查询操作日志

可以创建一个 OperationLogController 来查询操作日志：

```java
@RestController
@RequestMapping("/operation-logs")
public class OperationLogController {
    
    @GetMapping("/page")
    @SiaeAuthorize("hasAuthority('OPERATION_LOG_VIEW')")
    public Result<PageVO<OperationLog>> pageQuery(@RequestBody PageDTO<OperationLogQueryDTO> pageDTO) {
        // 实现分页查询
    }
}
```

## 总结

操作日志功能已完整实现，包括：

✅ 自定义注解 `@OperationLog`
✅ AOP 切面 `OperationLogAspect`
✅ 异步保存服务 `OperationLogService`
✅ 数据库实体和 Mapper
✅ 已在 AttendanceController 的关键方法上添加日志注解
✅ 支持 SpEL 表达式动态描述
✅ 异步保存，不影响主业务性能
✅ 自动记录请求信息、执行时长、错误信息等

后续可根据需要在其他 Controller 方法上添加 `@OperationLog` 注解来扩展日志记录范围。
