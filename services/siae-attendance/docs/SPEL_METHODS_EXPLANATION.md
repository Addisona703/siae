# SpEL 权限表达式方法详解

## 概述

在 `@SiaeAuthorize` 注解中使用的这些方法（如 `isAuthenticated()`, `hasAuthority()` 等）都是 **Spring Security 框架提供的内置方法**，它们定义在 `SecurityExpressionRoot` 类中。

## 工作原理

### 1. 执行流程

```
@SiaeAuthorize("hasAuthority('ATTENDANCE_VIEW')")
         ↓
SiaeAuthorizeAspect 拦截
         ↓
创建 SecurityExpressionRoot 对象（包含当前用户的认证信息）
         ↓
使用 SpEL 解析器解析表达式
         ↓
调用 SecurityExpressionRoot 中的方法
         ↓
返回 true/false 决定是否允许访问
```

### 2. 关键代码位置

在 `SiaeAuthorizeAspect.java` 中：

```java
// 1. 获取当前用户的认证信息
Authentication auth = SecurityContextHolder.getContext().getAuthentication();

// 2. 创建 SecurityExpressionRoot 对象
//    这个对象包含了所有可用的权限检查方法
SecurityExpressionRoot root = new SecurityExpressionRoot(auth) {};

// 3. 创建 SpEL 上下文，将 root 设置为根对象
StandardEvaluationContext context = new StandardEvaluationContext(root);

// 4. 解析并执行表达式
Boolean allowed = parser.parseExpression(expr).getValue(context, Boolean.class);
```

## 可用方法详解

### 1. `isAuthenticated()`

**来源**: `SecurityExpressionRoot.isAuthenticated()`

**作用**: 检查用户是否已认证（已登录）

**实现**:
```java
public final boolean isAuthenticated() {
    return !isAnonymous();
}
```

**使用示例**:
```java
@SiaeAuthorize("isAuthenticated()")
public Result<AttendanceRecordVO> checkIn(@RequestBody CheckInDTO dto) {
    // 只要用户登录了就可以签到
}
```

---

### 2. `hasAuthority('PERMISSION')`

**来源**: `SecurityExpressionRoot.hasAuthority(String authority)`

**作用**: 检查用户是否拥有指定的权限

**实现**:
```java
public final boolean hasAuthority(String authority) {
    return hasAnyAuthority(authority);
}

private boolean hasAnyAuthorityName(String prefix, Set<String> roles) {
    Set<String> roleSet = getAuthoritySet();
    for (String role : roles) {
        String defaultedRole = getRoleWithDefaultPrefix(prefix, role);
        if (roleSet.contains(defaultedRole)) {
            return true;
        }
    }
    return false;
}
```

**使用示例**:
```java
@SiaeAuthorize("hasAuthority('ATTENDANCE_LIST')")
public Result<PageVO<AttendanceRecordVO>> pageQuery(@RequestBody PageDTO dto) {
    // 需要 ATTENDANCE_LIST 权限才能查询列表
}
```

---

### 3. `hasAnyAuthority('PERM1', 'PERM2')`

**来源**: `SecurityExpressionRoot.hasAnyAuthority(String... authorities)`

**作用**: 检查用户是否拥有任意一个指定的权限（OR 逻辑）

**实现**:
```java
public final boolean hasAnyAuthority(String... authorities) {
    return hasAnyAuthorityName(null, authorities);
}
```

**使用示例**:
```java
@SiaeAuthorize("hasAnyAuthority('ATTENDANCE_VIEW', 'ATTENDANCE_VIEW_ALL')")
public Result<AttendanceRecordDetailVO> getRecord(@PathVariable Long id) {
    // 拥有 ATTENDANCE_VIEW 或 ATTENDANCE_VIEW_ALL 任意一个权限即可
}
```

---

### 4. `hasRole('ROLE')`

**来源**: `SecurityExpressionRoot.hasRole(String role)`

**作用**: 检查用户是否拥有指定的角色

**注意**: 会自动添加 `ROLE_` 前缀，所以 `hasRole('ADMIN')` 实际检查的是 `ROLE_ADMIN`

**实现**:
```java
public final boolean hasRole(String role) {
    return hasAnyRole(role);
}

private boolean hasAnyAuthorityName(String prefix, Set<String> roles) {
    Set<String> roleSet = getAuthoritySet();
    for (String role : roles) {
        String defaultedRole = getRoleWithDefaultPrefix(prefix, role);
        if (roleSet.contains(defaultedRole)) {
            return true;
        }
    }
    return false;
}
```

**使用示例**:
```java
@SiaeAuthorize("hasRole('ADMIN')")
public Result<Boolean> deleteRule(@PathVariable Long id) {
    // 需要 ROLE_ADMIN 角色才能删除规则
}
```

---

### 5. `hasAnyRole('ROLE1', 'ROLE2')`

**来源**: `SecurityExpressionRoot.hasAnyRole(String... roles)`

**作用**: 检查用户是否拥有任意一个指定的角色（OR 逻辑）

**注意**: 同样会自动添加 `ROLE_` 前缀

**实现**:
```java
public final boolean hasAnyRole(String... roles) {
    return hasAnyAuthorityName(defaultRolePrefix, roles);
}
```

**使用示例**:
```java
@SiaeAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public Result<ReportVO> generateReport(@RequestBody ReportDTO dto) {
    // 拥有 ROLE_ADMIN 或 ROLE_MANAGER 任意一个角色即可
}
```

---

## 其他可用方法

Spring Security 的 `SecurityExpressionRoot` 还提供了其他方法：

### `isAnonymous()`
检查用户是否是匿名用户（未登录）

### `isRememberMe()`
检查用户是否通过"记住我"功能登录

### `isFullyAuthenticated()`
检查用户是否完全认证（不包括"记住我"登录）

### `hasPermission(Object target, Object permission)`
检查用户对特定对象是否有指定权限（需要配置 PermissionEvaluator）

### `hasIpAddress(String ipAddress)`
检查请求是否来自指定的IP地址

## 组合使用

可以使用逻辑运算符组合多个表达式：

### AND 逻辑
```java
@SiaeAuthorize("isAuthenticated() and hasAuthority('ATTENDANCE_EXPORT')")
```

### OR 逻辑
```java
@SiaeAuthorize("hasRole('ADMIN') or hasAuthority('ATTENDANCE_VIEW_ALL')")
```

### NOT 逻辑
```java
@SiaeAuthorize("isAuthenticated() and !isAnonymous()")
```

### 复杂组合
```java
@SiaeAuthorize("(hasRole('ADMIN') or hasRole('MANAGER')) and hasAuthority('REPORT_GENERATE')")
```

## 在项目中的实际应用

### 示例 1: 签到接口
```java
@PostMapping("/check-in")
@SiaeAuthorize("isAuthenticated()")
public Result<AttendanceRecordVO> checkIn(@RequestBody CheckInDTO dto) {
    // 任何已登录用户都可以签到
}
```

### 示例 2: 查询列表接口
```java
@PostMapping("/records/page")
@SiaeAuthorize("hasAuthority('ATTENDANCE_LIST')")
public Result<PageVO<AttendanceRecordVO>> pageQuery(@RequestBody PageDTO dto) {
    // 需要特定权限才能查询列表
}
```

### 示例 3: 导出接口
```java
@GetMapping("/records/export")
@SiaeAuthorize("hasAuthority('ATTENDANCE_EXPORT')")
public void exportRecords(...) {
    // 需要导出权限
}
```

### 示例 4: 管理员接口
```java
@DeleteMapping("/rules/{id}")
@SiaeAuthorize("hasAnyRole('ADMIN', 'ROOT')")
public Result<Boolean> deleteRule(@PathVariable Long id) {
    // 只有管理员或超级管理员可以删除规则
}
```

## 调试技巧

### 1. 查看用户权限
在 `SiaeAuthorizeAspect` 中已经添加了日志：
```java
log.info("用户权限检查开始 - 用户: {}, 所有权限: {}", 
    auth.getName(),
    auth.getAuthorities().stream()
        .map(a -> a.getAuthority())
        .collect(Collectors.toList()));
```

### 2. 查看表达式评估结果
```java
log.info("评估权限表达式: {}，用户: {}", expr, auth.getName());
```

### 3. 启用 DEBUG 日志
在 `application.yml` 中：
```yaml
logging:
  level:
    com.hngy.siae.security: DEBUG
```

## 总结

这些方法都是 **Spring Security 框架的标准功能**，不是我们自己实现的。它们通过以下机制工作：

1. **SecurityExpressionRoot** - Spring Security 提供的基类，包含所有权限检查方法
2. **SpEL (Spring Expression Language)** - Spring 的表达式语言，用于解析和执行表达式
3. **SiaeAuthorizeAspect** - 我们的切面类，负责拦截注解并调用 Spring Security 的方法

这种设计的优点：
- ✅ 使用 Spring Security 的标准功能，稳定可靠
- ✅ 表达式灵活，可以组合使用
- ✅ 易于理解和维护
- ✅ 与 Spring Security 生态系统完全兼容
