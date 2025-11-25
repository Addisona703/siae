# Security 模块重构总结

## 重构日期
2024-11-25

## 重构目标
简化 `SiaeSecurityExpressionRoot` 和 `SecurityUtil` 的关系，避免代码重复，明确职责分工。

## 重构内容

### 1. 明确角色定义

**重要说明**：
- **ROLE_ROOT** = 超级管理员（拥有所有权限）
- **ROLE_ADMIN** = 普通管理员（按权限判断）

### 2. 职责分工

#### SecurityUtil（核心实现）
- **职责**：提供所有权限检查的实际逻辑
- **使用场景**：Service层、Controller层的Java代码
- **特点**：
  - `@Component("authUtil")` - 可以被Spring管理和注入
  - 提供静态方法风格的API
  - 可以在SpEL中通过 `@authUtil.method()` 调用

#### SiaeSecurityExpressionRoot（SpEL适配器）
- **职责**：作为SpEL表达式的适配器，委托给SecurityUtil
- **使用场景**：`@SiaeAuthorize` 注解中的SpEL表达式
- **特点**：
  - 继承 `SecurityExpressionRoot`，集成Spring Security
  - 所有方法都委托给 `SecurityUtil` 实现
  - 避免代码重复，保持单一职责

### 3. 修改的文件

#### 3.1 SiaeSecurityExpressionRoot.java

**修改前**：
```java
public class SiaeSecurityExpressionRoot extends SecurityExpressionRoot {
    public SiaeSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }
    
    // 直接实现权限检查逻辑
    public boolean isOwner(Long recordOwnerId) {
        // 重复的实现代码...
    }
}
```

**修改后**：
```java
public class SiaeSecurityExpressionRoot extends SecurityExpressionRoot {
    private final SecurityUtil securityUtil;
    
    public SiaeSecurityExpressionRoot(Authentication authentication, SecurityUtil securityUtil) {
        super(authentication);
        this.securityUtil = securityUtil;
    }
    
    // 委托给SecurityUtil
    public boolean isOwner(Long recordOwnerId) {
        return securityUtil.isOwner(recordOwnerId);
    }
}
```

#### 3.2 SiaeAuthorizeAspect.java

**修改前**：
```java
@Aspect
@Component
public class SiaeAuthorizeAspect {
    private final SpelExpressionParser parser = new SpelExpressionParser();
    
    @Around("@annotation(siaeAuthorize)")
    public Object checkPermission(...) {
        // 创建ExpressionRoot，没有注入SecurityUtil
        SiaeSecurityExpressionRoot root = new SiaeSecurityExpressionRoot(auth);
        // ...
    }
}
```

**修改后**：
```java
@Aspect
@Component
@RequiredArgsConstructor
public class SiaeAuthorizeAspect {
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final SecurityUtil securityUtil;  // 注入SecurityUtil
    
    @Around("@annotation(siaeAuthorize)")
    public Object checkPermission(...) {
        // 创建ExpressionRoot，注入SecurityUtil
        SiaeSecurityExpressionRoot root = new SiaeSecurityExpressionRoot(auth, securityUtil);
        // ...
    }
}
```

## 使用指南

### 场景1：Controller层注解（推荐）

```java
@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    // ✅ 使用SpEL表达式
    @GetMapping("/records/{id}")
    @SiaeAuthorize("hasPermissionOrOwner('ATTENDANCE_VIEW', #id)")
    public Result<AttendanceRecordVO> getRecord(@PathVariable Long id) {
        // 业务逻辑
    }

    // ✅ 检查超级管理员
    @DeleteMapping("/records/{id}")
    @SiaeAuthorize("isSuperAdmin()")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        // 只有超级管理员(ROLE_ROOT)可以删除
    }
}
```

### 场景2：Service层代码（推荐）

```java
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements IAttendanceService {

    private final SecurityUtil securityUtil;

    @Override
    public AttendanceRecordVO getRecord(Long id) {
        // ✅ 使用SecurityUtil
        Long currentUserId = securityUtil.getCurrentUserId();
        
        AttendanceRecord record = recordMapper.selectById(id);
        
        // 权限检查
        if (!securityUtil.hasPermission("ATTENDANCE_VIEW") 
            && !securityUtil.isOwner(record.getUserId())) {
            throw new BusinessException("无权访问该记录");
        }
        
        return convertToVO(record);
    }

    @Override
    public void deleteRecord(Long id) {
        // ✅ 超级管理员检查
        if (!securityUtil.isSuperAdmin()) {
            throw new BusinessException("只有超级管理员可以删除记录");
        }
        
        recordMapper.deleteById(id);
    }
}
```

### 场景3：混合使用

```java
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final IAttendanceService attendanceService;
    private final SecurityUtil securityUtil;

    // ✅ 注解做粗粒度控制
    @GetMapping("/records/page")
    @SiaeAuthorize("hasPermission('ATTENDANCE_LIST')")
    public Result<PageVO<AttendanceRecordVO>> pageQuery(
            @RequestBody PageDTO<AttendanceQueryDTO> pageDTO) {
        
        // ✅ 代码做细粒度控制
        if (!securityUtil.isSuperAdmin()) {
            // 非超级管理员只能查看自己的数据
            pageDTO.getParams().setUserId(securityUtil.getCurrentUserId());
        }
        
        return Result.success(attendanceService.pageQuery(pageDTO));
    }
}
```

## 权限层级说明

### 超级管理员（ROLE_ROOT）
- 拥有所有权限
- 可以访问所有资源
- 可以执行所有操作
- 检查方法：`securityUtil.isSuperAdmin()`

### 普通管理员（ROLE_ADMIN）
- 按具体权限判断
- 需要检查是否有特定权限
- 检查方法：`securityUtil.hasPermission("PERMISSION_CODE")`

### 普通用户
- 只能访问自己的资源
- 需要检查所有者关系
- 检查方法：`securityUtil.isOwner(ownerId)`

## 可用的权限检查方法

### SecurityUtil 提供的方法

| 方法 | 说明 | 使用场景 |
|------|------|---------|
| `getCurrentUserId()` | 获取当前用户ID | 获取当前登录用户 |
| `getCurrentUserIdOrNull()` | 获取当前用户ID（可空） | 可选的用户ID获取 |
| `isSuperAdmin()` | 是否是超级管理员(ROLE_ROOT) | 超管权限检查 |
| `hasPermission(String)` | 是否有指定权限 | 权限检查 |
| `isSuperAdminOrHasPermission(String)` | 超管或有权限 | 组合权限检查 |
| `isOwner(Long)` | 是否是记录所有者 | 所有者检查 |
| `hasPermissionOrOwner(String, Long)` | 有权限或是所有者 | 组合检查 |
| `canApproveLeave(Long)` | 是否可以审批请假 | 业务权限检查 |

### SpEL表达式中可用的方法

所有 `SecurityUtil` 的方法都可以在SpEL中使用，例如：

```java
@SiaeAuthorize("isOwner(#id)")
@SiaeAuthorize("hasPermission('ATTENDANCE_VIEW')")
@SiaeAuthorize("isSuperAdmin()")
@SiaeAuthorize("hasPermissionOrOwner('ATTENDANCE_VIEW', #recordId)")
@SiaeAuthorize("isSuperAdminOrHasPermission('ATTENDANCE_EXPORT')")
```

## 优势

### 1. 代码复用
- 所有权限逻辑只在 `SecurityUtil` 中实现一次
- `SiaeSecurityExpressionRoot` 只是简单的委托
- 避免了代码重复和维护困难

### 2. 职责清晰
- `SecurityUtil`：核心权限逻辑
- `SiaeSecurityExpressionRoot`：SpEL适配器
- `SiaeAuthorizeAspect`：AOP拦截器

### 3. 易于测试
- `SecurityUtil` 可以独立测试
- 不需要模拟Spring Security上下文

### 4. 易于扩展
- 新增权限检查方法只需在 `SecurityUtil` 中添加
- `SiaeSecurityExpressionRoot` 自动继承新方法

## 注意事项

1. **超级管理员定义**：只有 `ROLE_ROOT` 是超级管理员，`ROLE_ADMIN` 是普通管理员
2. **权限检查顺序**：先检查超级管理员，再检查具体权限
3. **所有者检查**：需要确保 `authentication.getName()` 返回的是用户ID
4. **SpEL表达式**：参数名需要与方法参数名一致（使用 `#` 引用）

## 后续优化建议

1. 实现 `belongsToDepartment()` 方法（需要集成用户服务）
2. 实现 `isWithinTimeRange()` 方法（需要日期解析逻辑）
3. 添加更多业务相关的权限检查方法
4. 考虑添加权限缓存机制（如果性能成为瓶颈）

## 相关文档

- [权限配置指南](../../services/siae-attendance/PERMISSION_GUIDE.md)
- [考勤服务权限说明](../../services/siae-attendance/README.md)

## 变更历史

| 日期 | 版本 | 变更内容 | 变更人 |
|------|------|---------|--------|
| 2024-11-25 | 1.0.0 | 重构Security模块，统一权限检查逻辑 | SIAE Team |
