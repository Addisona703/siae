# 考勤服务权限控制指南

## 概述

考勤服务实现了基于Spring Security的权限控制系统，包括：
- 接口级权限控制（通过`@SiaeAuthorize`注解）
- 数据级权限过滤（在Service层实现）
- 超级管理员全局放行机制

## 权限配置

### 启用权限控制

在配置文件中设置：

```yaml
siae:
  security:
    enabled: true  # 启用权限控制
```

开发环境可以设置为`false`以快速测试。

## 权限定义

所有权限定义在`PermissionConstants`类中：

### 考勤记录权限
- `ATTENDANCE_VIEW` - 查看考勤记录详情
- `ATTENDANCE_VIEW_ALL` - 查看所有考勤记录
- `ATTENDANCE_LIST` - 查询考勤记录列表
- `ATTENDANCE_EXPORT` - 导出考勤记录
- `ATTENDANCE_UPDATE` - 修改考勤记录
- `ATTENDANCE_DELETE` - 删除考勤记录

### 请假权限
- `LEAVE_VIEW` - 查看请假申请详情
- `LEAVE_VIEW_ALL` - 查看所有请假申请
- `LEAVE_LIST` - 查询请假申请列表
- `LEAVE_CREATE` - 创建请假申请
- `LEAVE_UPDATE` - 更新请假申请
- `LEAVE_DELETE` - 删除请假申请
- `LEAVE_APPROVE` - 审批请假申请

### 考勤异常权限
- `ANOMALY_VIEW` - 查看考勤异常详情
- `ANOMALY_VIEW_ALL` - 查看所有考勤异常
- `ANOMALY_LIST` - 查询考勤异常列表
- `ANOMALY_HANDLE` - 处理考勤异常

### 考勤规则权限
- `RULE_VIEW` - 查看考勤规则详情
- `RULE_LIST` - 查询考勤规则列表
- `RULE_CREATE` - 创建考勤规则
- `RULE_UPDATE` - 更新考勤规则
- `RULE_DELETE` - 删除考勤规则

### 统计报表权限
- `STATISTICS_VIEW` - 查看统计数据
- `REPORT_GENERATE` - 生成报表
- `REPORT_EXPORT` - 导出报表

### 角色
- `ROLE_ROOT` - 超级管理员（拥有所有权限）
- `ROLE_ADMIN` - 管理员（拥有所有权限）
- `ROLE_USER` - 普通用户

## 使用方法

### 1. Controller层权限控制

使用`@SiaeAuthorize`注解：

```java
@PostMapping("/records/page")
@SiaeAuthorize("hasAuthority('ATTENDANCE_LIST')")
public Result<PageVO<AttendanceRecordVO>> pageQuery(@RequestBody PageDTO<AttendanceQueryDTO> pageDTO) {
    // ...
}
```

支持的SpEL表达式：
- `isAuthenticated()` - 已认证用户
- `hasAuthority('PERMISSION')` - 拥有指定权限
- `hasAnyAuthority('PERM1', 'PERM2')` - 拥有任意一个权限
- `hasRole('ROLE')` - 拥有指定角色
- `hasAnyRole('ROLE1', 'ROLE2')` - 拥有任意一个角色

### 2. Service层数据权限过滤

在Service方法中使用`SecurityUtil`：

```java
@Override
public PageVO<AttendanceRecordVO> pageQuery(PageDTO<AttendanceQueryDTO> pageDTO, 
                                             Long currentUserId, 
                                             boolean hasListPermission) {
    // 数据权限过滤：如果没有列表查询权限，只能查看自己的数据
    if (!hasListPermission && currentUserId != null) {
        queryDTO.setUserId(currentUserId);
    }
    // ...
}
```

### 3. 获取当前用户信息

使用`SecurityUtil`工具类：

```java
// 获取当前用户ID
Long userId = SecurityUtil.getCurrentUserId();

// 获取当前用户ID（可能为null）
Long userId = SecurityUtil.getCurrentUserIdOrNull();

// 检查权限
boolean hasPermission = SecurityUtil.hasPermission("ATTENDANCE_VIEW");

// 检查是否是超级管理员
boolean isSuperAdmin = SecurityUtil.isSuperAdmin();

// 检查是否是记录所有者
boolean isOwner = SecurityUtil.isOwner(recordOwnerId);
```

## 数据权限规则

### 基本原则
1. **超级管理员**（`ROLE_ROOT`、`ROLE_ADMIN`）可以访问所有数据
2. **有VIEW_ALL权限**的用户可以查看所有数据
3. **普通用户**只能查看自己的数据

### 实现方式
1. Controller层检查接口权限
2. Service层根据用户权限过滤数据
3. 数据权限切面提供额外的安全保障

## 权限验证流程

```
请求 → SecurityFilterChain → @SiaeAuthorize注解检查 → Controller方法
                                                    ↓
                                            Service层数据过滤
                                                    ↓
                                            返回结果
```

## 测试建议

### 开发环境
设置`siae.security.enabled=false`以快速测试功能。

### 测试环境
1. 创建不同权限的测试用户
2. 测试各种权限组合
3. 验证数据权限过滤是否正确

### 生产环境
1. 确保`siae.security.enabled=true`
2. 定期审计权限配置
3. 监控权限拒绝日志

## 常见问题

### Q: 如何添加新的权限？
A: 在`PermissionConstants`中定义新权限，然后在Controller方法上添加`@SiaeAuthorize`注解。

### Q: 超级管理员如何配置？
A: 在用户服务中为用户分配`ROLE_ROOT`或`ROLE_ADMIN`角色。

### Q: 如何调试权限问题？
A: 查看日志中的权限检查信息，日志级别设置为DEBUG可以看到详细的权限验证过程。

### Q: 数据权限过滤在哪里实现？
A: 主要在Service层的查询方法中实现，根据用户权限动态调整查询条件。

## 安全建议

1. **最小权限原则** - 只授予用户必需的权限
2. **定期审计** - 定期检查用户权限配置
3. **日志监控** - 监控权限拒绝和异常访问
4. **敏感操作** - 对敏感操作（如删除、导出）进行额外验证
5. **数据脱敏** - 导出数据时注意脱敏处理
