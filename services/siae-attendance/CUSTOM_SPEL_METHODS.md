# 自定义 SpEL 权限方法指南

## 概述

是的，你**完全可以自定义 SpEL 方法**！这是一个非常强大的功能，可以让你创建符合业务需求的权限检查逻辑。

## SpEL 可以使用方法

**重要澄清**：
- ✅ **SpEL 表达式中调用方法** - 完全允许，这就是 SpEL 的核心功能
- ❌ **在注解中直接写 Java 代码** - 不允许

SpEL（Spring Expression Language）就是为了让你能在字符串表达式中调用方法而设计的！

## 三种自定义方法的方式

### 方式 1: 扩展 SecurityExpressionRoot（推荐）✨

这是最推荐的方式，因为它继承了所有 Spring Security 的标准方法，同时可以添加自定义方法。

#### 步骤 1: 创建自定义表达式根类

```java
public class CustomSecurityExpressionRoot extends SecurityExpressionRoot {

    public CustomSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }

    /**
     * 自定义方法：检查用户是否是记录的所有者
     */
    public boolean isOwner(Long recordOwnerId) {
        if (recordOwnerId == null) {
            return false;
        }
        
        try {
            Long currentUserId = Long.parseLong(this.getPrincipal().toString());
            return currentUserId.equals(recordOwnerId);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 自定义方法：检查用户是否有权限或者是记录所有者
     */
    public boolean hasPermissionOrOwner(String permission, Long recordOwnerId) {
        return hasAuthority(permission) || isOwner(recordOwnerId);
    }
}
```

#### 步骤 2: 在切面中使用自定义类

```java
@Around("@annotation(siaeAuthorize)")
public Object checkPermission(ProceedingJoinPoint joinPoint, SiaeAuthorize siaeAuthorize) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    // 使用自定义的表达式根对象
    CustomSecurityExpressionRoot root = new CustomSecurityExpressionRoot(auth);
    
    StandardEvaluationContext context = new StandardEvaluationContext(root);
    
    // 解析表达式
    Boolean allowed = parser.parseExpression(expr).getValue(context, Boolean.class);
    // ...
}
```

#### 步骤 3: 在注解中使用

```java
// 使用自定义方法
@SiaeAuthorize("isOwner(#recordOwnerId)")
public Result<AttendanceRecordVO> getRecord(@PathVariable Long recordOwnerId) {
    // ...
}

// 组合使用
@SiaeAuthorize("hasPermissionOrOwner('ATTENDANCE_VIEW', #recordOwnerId)")
public Result<AttendanceRecordVO> getRecord(@PathVariable Long recordOwnerId) {
    // ...
}
```

---

### 方式 2: 在 SpEL 上下文中注册 Bean

这种方式允许你使用 Spring Bean 中的方法。

#### 步骤 1: 创建辅助 Bean

```java
@Component("securityHelper")
public class SecurityHelper {
    
    @Autowired
    private UserService userService;
    
    /**
     * 检查用户是否可以访问记录
     */
    public boolean canAccessRecord(Long recordId) {
        // 可以注入其他服务，执行复杂的业务逻辑
        Long currentUserId = getCurrentUserId();
        AttendanceRecord record = attendanceService.getById(recordId);
        return record != null && record.getUserId().equals(currentUserId);
    }
    
    /**
     * 检查用户是否是部门管理员
     */
    public boolean isDepartmentAdmin(Long departmentId) {
        Long currentUserId = getCurrentUserId();
        User user = userService.getById(currentUserId);
        return user.getDepartmentId().equals(departmentId) 
            && user.getRole().equals("DEPT_ADMIN");
    }
}
```

#### 步骤 2: 在切面中注册 Bean

```java
@Aspect
@Component
public class SiaeAuthorizeAspect {
    
    @Autowired
    private SecurityHelper securityHelper;  // 注入辅助Bean
    
    @Around("@annotation(siaeAuthorize)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, SiaeAuthorize siaeAuthorize) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        SecurityExpressionRoot root = new SecurityExpressionRoot(auth) {};
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        
        // 注册自定义Bean到SpEL上下文
        context.setVariable("securityHelper", securityHelper);
        
        // 解析表达式
        Boolean allowed = parser.parseExpression(expr).getValue(context, Boolean.class);
        // ...
    }
}
```

#### 步骤 3: 在注解中使用

```java
// 使用 #beanName.method() 语法
@SiaeAuthorize("#securityHelper.canAccessRecord(#recordId)")
public Result<AttendanceRecordVO> getRecord(@PathVariable Long recordId) {
    // ...
}

@SiaeAuthorize("#securityHelper.isDepartmentAdmin(#departmentId)")
public Result<List<UserVO>> getDepartmentUsers(@PathVariable Long departmentId) {
    // ...
}
```

---

### 方式 3: 使用 @Bean 名称直接调用

如果你的 Bean 有 `@Component` 注解并指定了名称，可以直接在 SpEL 中使用。

#### 步骤 1: 创建命名 Bean

```java
@Component("auth")  // 指定Bean名称
public class AuthUtil {
    
    public boolean canApprove(Long leaveRequestId) {
        // 实现审批权限检查逻辑
        return true;
    }
    
    public boolean isInSameDepartment(Long userId) {
        // 检查是否在同一部门
        return true;
    }
}
```

#### 步骤 2: 在切面中注册应用上下文

```java
@Aspect
@Component
public class SiaeAuthorizeAspect {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Around("@annotation(siaeAuthorize)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, SiaeAuthorize siaeAuthorize) {
        // ...
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        
        // 设置Bean解析器，允许SpEL访问Spring容器中的Bean
        context.setBeanResolver(new BeanFactoryResolver(applicationContext));
        
        // ...
    }
}
```

#### 步骤 3: 在注解中使用

```java
// 使用 @beanName.method() 语法
@SiaeAuthorize("@auth.canApprove(#leaveRequestId)")
public Result<LeaveRequestVO> approveLeave(@PathVariable Long leaveRequestId) {
    // ...
}

@SiaeAuthorize("@auth.isInSameDepartment(#userId)")
public Result<UserVO> getColleagueInfo(@PathVariable Long userId) {
    // ...
}
```

---

## 实际使用示例

### 示例 1: 检查记录所有者

```java
// Controller
@GetMapping("/records/{id}")
@SiaeAuthorize("hasAuthority('ATTENDANCE_VIEW') or isOwner(#id)")
public Result<AttendanceRecordVO> getRecord(@PathVariable Long id) {
    // 有查看权限或者是记录所有者都可以访问
}
```

### 示例 2: 复杂的业务逻辑

```java
// Controller
@PostMapping("/leaves/{id}/approve")
@SiaeAuthorize("hasAuthority('LEAVE_APPROVE') and #securityHelper.canApproveLeave(#id)")
public Result<LeaveRequestVO> approveLeave(@PathVariable Long id) {
    // 需要有审批权限，并且满足业务规则（如是直接上级）
}
```

### 示例 3: 组合多个条件

```java
// Controller
@DeleteMapping("/records/{id}")
@SiaeAuthorize("hasRole('ADMIN') or (isOwner(#id) and hasAuthority('ATTENDANCE_DELETE_OWN'))")
public Result<Boolean> deleteRecord(@PathVariable Long id) {
    // 管理员可以删除任何记录
    // 或者是记录所有者且有删除自己记录的权限
}
```

### 示例 4: 使用方法参数

```java
// Controller
@PostMapping("/leaves")
@SiaeAuthorize("isAuthenticated() and #dto.userId == authentication.principal")
public Result<LeaveRequestVO> createLeave(@RequestBody LeaveRequestDTO dto) {
    // 只能为自己创建请假申请
}
```

---

## 可用的 SpEL 变量

在 SpEL 表达式中，你可以使用以下变量：

### 1. 方法参数
使用 `#参数名` 访问：
```java
@SiaeAuthorize("isOwner(#recordId)")
public Result<Void> deleteRecord(@PathVariable Long recordId) { }
```

### 2. Authentication 对象
使用 `authentication` 访问当前认证对象：
```java
@SiaeAuthorize("#dto.userId == authentication.principal")
public Result<Void> updateUser(@RequestBody UserDTO dto) { }
```

### 3. 自定义变量
通过 `context.setVariable()` 注册的变量：
```java
context.setVariable("currentTime", LocalDateTime.now());
// 在表达式中使用: #currentTime.isAfter(#dto.startTime)
```

### 4. Spring Bean
通过 `@beanName` 或 `#beanName` 访问：
```java
@SiaeAuthorize("@userService.isActive(#userId)")
public Result<Void> doSomething(@PathVariable Long userId) { }
```

---

## SpEL 支持的操作符

### 逻辑操作符
- `and` 或 `&&` - 逻辑与
- `or` 或 `||` - 逻辑或
- `not` 或 `!` - 逻辑非

### 比较操作符
- `==` - 等于
- `!=` - 不等于
- `<` - 小于
- `>` - 大于
- `<=` - 小于等于
- `>=` - 大于等于

### 三元操作符
```java
@SiaeAuthorize("hasRole('ADMIN') ? true : isOwner(#id)")
```

### 正则表达式
```java
@SiaeAuthorize("#email matches '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}'")
```

---

## 最佳实践

### 1. 保持表达式简单
❌ 不好：
```java
@SiaeAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and #dto.departmentId == authentication.details.departmentId and #dto.amount < 10000) or (hasRole('USER') and isOwner(#dto.userId) and #dto.status == 'DRAFT')")
```

✅ 好：
```java
@SiaeAuthorize("@approvalHelper.canApprove(#dto)")
```

### 2. 将复杂逻辑封装到方法中
```java
// 在 CustomSecurityExpressionRoot 中
public boolean canApproveExpense(ExpenseDTO dto) {
    if (hasRole("ADMIN")) return true;
    if (hasRole("MANAGER") && dto.getAmount() < 10000) return true;
    if (hasRole("USER") && isOwner(dto.getUserId()) && "DRAFT".equals(dto.getStatus())) return true;
    return false;
}

// 使用
@SiaeAuthorize("canApproveExpense(#dto)")
```

### 3. 添加日志
在自定义方法中添加日志，方便调试：
```java
public boolean isOwner(Long recordOwnerId) {
    boolean result = // ... 检查逻辑
    log.debug("所有者检查: currentUserId={}, recordOwnerId={}, result={}", 
            currentUserId, recordOwnerId, result);
    return result;
}
```

### 4. 处理空值
```java
public boolean isOwner(Long recordOwnerId) {
    if (recordOwnerId == null) {
        return false;  // 明确处理null情况
    }
    // ...
}
```

### 5. 单元测试
为自定义方法编写单元测试：
```java
@Test
void testIsOwner() {
    Authentication auth = new UsernamePasswordAuthenticationToken("123", null);
    CustomSecurityExpressionRoot root = new CustomSecurityExpressionRoot(auth);
    
    assertTrue(root.isOwner(123L));
    assertFalse(root.isOwner(456L));
}
```

---

## 性能考虑

### 1. 避免在 SpEL 中执行耗时操作
❌ 不好：
```java
@SiaeAuthorize("@userService.getUserFromDatabase(#userId).isActive()")
```

✅ 好：
```java
@SiaeAuthorize("@userService.isUserActive(#userId)")  // 内部可以使用缓存
```

### 2. 使用缓存
```java
@Component
public class SecurityHelper {
    
    @Cacheable("userPermissions")
    public boolean hasPermission(Long userId, String permission) {
        // 查询数据库
    }
}
```

### 3. 避免重复计算
```java
// 不好：每次都计算
@SiaeAuthorize("isOwner(#id) and isOwner(#id)")

// 好：计算一次
@SiaeAuthorize("isOwner(#id)")
```

---

## 总结

1. ✅ **SpEL 完全支持调用方法** - 这是它的核心功能
2. ✅ **有三种方式自定义方法**：
   - 扩展 SecurityExpressionRoot（推荐）
   - 注册 Bean 到 SpEL 上下文
   - 使用 @Bean 名称直接调用
3. ✅ **可以访问方法参数、Authentication 对象、Spring Bean**
4. ✅ **支持复杂的逻辑操作符和表达式**
5. ⚠️ **注意性能和可维护性**

现在你可以根据业务需求自由地创建自定义权限检查方法了！
