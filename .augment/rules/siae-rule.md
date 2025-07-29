---
type: "always_apply"
description: "SIAE项目编码规范和最佳实践指南"
---
# SIAE项目编码规范

## 1. 注解和文档规范

### 1.1 作者注解规范
- **规则**: 所有类上的JavaDoc注释都必须添加 `@author KEYKB`，方法上的JavaDoc注释不需要添加 `@author` 字段
- **适用范围**: 类级别、方法级别的JavaDoc注释

```java
/**
 * 用户服务实现类
 *
 * @author KEYKB
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 创建用户
     *
     * @param userDTO 用户创建参数
     * @return 创建结果
     */
    @Override
    public Boolean createUser(UserCreateDTO userDTO) {
        // 实现逻辑
    }
}
```

### 1.2 权限注解规范
- **规则**: 所有权限认证使用 `@SiaeAuthorize` 注解
- **权限常量**: 使用 `com.hngy.siae.core.permissions.AuthPermissions` 中定义的常量

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @PostMapping
    @SiaeAuthorize("hasAuthority('" + AUTH_USER_CREATE + "')")
    public Result<Boolean> createUser(@Valid @RequestBody UserCreateDTO userDTO) {
        // 实现逻辑
    }
}
```

## 2. 服务层规范

### 2.1 服务接口继承规范
- **规则**: 所有服务接口必须继承 `IService<T>`
- **目的**: 获得MyBatis-Plus提供的基础CRUD方法

```java
/**
 * 用户服务接口
 *
 * @author KEYKB
 */
public interface UserService extends IService<User> {

    /**
     * 自定义业务方法
     */
    PageVO<UserVO> getUsersPage(PageDTO<UserQueryDTO> pageDTO);
}
```

### 2.2 服务实现类继承规范
- **规则**: 所有服务实现类必须继承 `ServiceImpl<Mapper, Entity>`
- **依赖注入**: 使用 `@RequiredArgsConstructor` 注解

```java
/**
 * 用户服务实现类
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final RoleService roleService;

    @Override
    public PageVO<UserVO> getUsersPage(PageDTO<UserQueryDTO> pageDTO) {
        // 使用基类方法
        Page<User> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        IPage<User> userPage = page(page, queryWrapper);

        // 转换为VO
        List<UserVO> userVOs = userPage.getRecords().stream()
                .map(user -> BeanConvertUtil.to(user, UserVO.class))
                .toList();

        return PageConvertUtil.convert(userPage, userVOs);
    }
}
```

## 3. 参数校验和错误处理规范

### 3.1 AssertUtils使用规范
- **规则**: 使用 `AssertUtils` 进行参数校验和业务校验
- **错误码**: 使用 `siae-core` 包下的错误码枚举，禁止使用硬编码字符串

```java
@Override
public Boolean createUser(UserCreateDTO userDTO) {
    // 参数校验
    AssertUtils.notNull(userDTO, CommonResultCodeEnum.VALIDATE_FAILED);
    AssertUtils.notEmpty(userDTO.getUsername(), CommonResultCodeEnum.VALIDATE_FAILED);

    // 业务校验
    boolean userExists = count(new LambdaQueryWrapper<User>()
            .eq(User::getUsername, userDTO.getUsername())) > 0;
    AssertUtils.isFalse(userExists, AuthResultCodeEnum.USER_ALREADY_EXISTS);

    // 业务逻辑
    User user = BeanConvertUtil.to(userDTO, User.class);
    boolean saved = save(user);
    AssertUtils.isTrue(saved, AuthResultCodeEnum.USER_CREATE_FAILED);

    return true;
}
```

### 3.2 错误码管理规范
- **规则**: 如果需要的错误码不存在，必须添加到对应的错误码枚举中
- **分类**: 按模块分类管理错误码（如：`AuthResultCodeEnum`、`CommonResultCodeEnum`）

```java
// AuthResultCodeEnum.java
public enum AuthResultCodeEnum implements ResultCode {
    USER_ALREADY_EXISTS(2001, "用户已存在"),
    USER_NOT_FOUND(2002, "用户不存在"),
    USER_CREATE_FAILED(2003, "用户创建失败");

    // 枚举实现
}
```

## 4. 工具类使用规范

### 4.1 字符串工具类
- **规则**: 统一使用 `cn.hutool.core.util.StrUtil`
- **禁止**: 使用 `org.springframework.util.StringUtils`

```java
// 正确用法
if (StrUtil.isNotBlank(username)) {
    // 处理逻辑
}

// 错误用法 - 禁止使用
if (StringUtils.hasText(username)) {
    // 处理逻辑
}
```

### 4.2 对象转换工具类
- **规则**: 使用 `BeanConvertUtil` 进行对象转换
- **方法选择**:
  - 单对象转换: `BeanConvertUtil.to(source, targetClass)`
  - 集合转换: `BeanConvertUtil.toList(sourceList, targetClass)`

```java
// 单对象转换
UserVO userVO = BeanConvertUtil.to(user, UserVO.class);

// 集合转换
List<UserVO> userVOs = BeanConvertUtil.toList(users, UserVO.class);
```

### 4.3 分页转换工具类
- **规则**: 使用 `PageConvertUtil` 转换MyBatis-Plus的分页结果

```java
// 创建分页对象
IPage<User> page = PageConvertUtil.toPage(pageDTO);

// 分页结果转换
IPage<User> userPage = page(page, queryWrapper);
List<UserVO> userVOs = BeanConvertUtil.toList(userPage.getRecords(), UserVO.class);
PageVO<UserVO> result = PageConvertUtil.convert(userPage, userVOs);
```

## 5. 事务管理规范

### 5.1 事务注解使用
- **规则**: 所有涉及数据修改的方法必须添加 `@Transactional` 注解
- **回滚策略**: 使用 `rollbackFor = Exception.class` 确保所有异常都回滚

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Boolean batchDeleteUsers(List<Long> userIds) {
    AssertUtils.notEmpty(userIds, CommonResultCodeEnum.VALIDATE_FAILED);

    // 检查用户是否存在
    List<User> users = listByIds(userIds);
    AssertUtils.isTrue(users.size() == userIds.size(), AuthResultCodeEnum.USER_NOT_FOUND);

    // 删除用户角色关联
    userRoleService.remove(new LambdaQueryWrapper<UserRole>()
            .in(UserRole::getUserId, userIds));

    // 删除用户
    return removeByIds(userIds);
}
```

## 6. 数据库和实体规范

### 6.1 数据库字段命名
- **规则**: 使用 `snake_case` 命名规范
- **示例**: `user_id`, `created_at`, `updated_at`

### 6.2 实体类字段命名
- **规则**: 使用 `camelCase` 命名规范
- **时间字段**: 统一使用 `createdAt`, `updatedAt`

```java
@TableName("sys_user")
public class User {
    @TableId
    private Long id;

    private String username;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
```

### 6.3 DTO字段命名一致性
- **规则**: DTO中的时间查询字段应与实体字段保持一致
- **查询字段**: 使用 `createdAtStart`, `createdAtEnd` 等

```java
public class UserQueryDTO {
    private String username;
    private String createdAtStart;
    private String createdAtEnd;
}
```

## 7. Controller层规范

### 7.1 参数校验注解
- **规则**: 使用标准的Bean Validation注解
- **常用注解**: `@Valid`, `@NotNull`, `@NotEmpty`, `@NotBlank`

```java
@PostMapping
public Result<Boolean> createUser(
        @Valid @RequestBody UserCreateDTO userDTO) {
    Boolean result = userService.createUser(userDTO);
    return Result.success(result);
}

@GetMapping("/{userId}")
public Result<UserVO> getUser(
        @PathVariable("userId") @NotNull Long userId) {
    UserVO user = userService.getUserById(userId);
    return Result.success(user);
}
```

### 7.2 Swagger文档注解
- **规则**: 为所有Controller方法添加Swagger注解，只允许添加以下三个
- **必需注解**: `@Operation`, `@Parameter`, `@Tag`

```java
@Tag(name = "用户管理", description = "用户相关API")
@RestController
@RequestMapping("/users")
public class UserController {

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public Result<Boolean> createUser(
            @Parameter(description = "用户创建参数") @Valid @RequestBody UserCreateDTO userDTO) {
        // 实现逻辑
    }
}
```

## 8. 代码重构和优化规范

### 8.1 方法提取原则
- **规则**: 将重复的业务逻辑提取为独立方法
- **命名**: 方法名应清晰表达功能意图

```java
// 提取权限验证逻辑
private void validatePermissionsExist(List<Long> permissionIds) {
    if (permissionIds == null || permissionIds.isEmpty()) {
        return;
    }

    List<Permission> permissions = permissionService.listByIds(permissionIds);
    AssertUtils.isTrue(permissions.size() == permissionIds.size(),
        AuthResultCodeEnum.PERMISSION_NOT_EXISTS);
}

// 提取系统角色保护逻辑
private void checkSystemRoleProtection(Role role, AuthResultCodeEnum errorCode) {
    if ("ROLE_ROOT".equals(role.getCode()) || "ROLE_ADMIN".equals(role.getCode())) {
        AssertUtils.fail(errorCode);
    }
}
```

### 8.2 异步处理规范
- **规则**: 对于日志记录等非核心业务使用 `@Async` 注解
- **目的**: 提高主业务流程性能

```java
@Async
@Override
public void logUserOperation(Long userId, String operation) {
    // 异步记录用户操作日志
    OperationLog log = new OperationLog();
    log.setUserId(userId);
    log.setOperation(operation);
    log.setCreatedAt(LocalDateTime.now());

    operationLogService.save(log);
}
```

## 9. 开发流程规范

### 9.1 文档创建规范
- **规则**: 不要主动创建总结文档，除非明确要求
- **例外**: 在明确要求时才创建技术文档或总结报告

### 9.2 代码审查要点
- 检查是否遵循所有编码规范
- 验证错误处理和参数校验的完整性
- 确认事务边界的正确性
- 检查工具类使用的一致性

## 10. 性能优化规范

### 10.1 查询优化
- **规则**: 优先使用MyBatis-Plus的基础方法
- **批量操作**: 使用 `saveBatch()`, `removeByIds()` 等批量方法

```java
// 批量保存
List<User> users = userDTOs.stream()
        .map(dto -> BeanConvertUtil.to(dto, User.class))
        .toList();
boolean saved = saveBatch(users);

// 批量删除
boolean removed = removeByIds(userIds);
```

### 10.2 缓存使用规范
- **规则**: 合理使用Redis缓存提高性能
- **清理策略**: 数据更新时及时清理相关缓存

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Boolean updateUser(Long userId, UserUpdateDTO updateDTO) {
    // 更新用户信息
    User user = BeanConvertUtil.to(updateDTO, User.class);
    user.setId(userId);
    boolean updated = updateById(user);

    // 清理缓存
    if (updated) {
        redisTemplate.delete("user:" + userId);
    }

    return updated;
}
```

---

**注意**: 本规范文档会根据项目发展持续更新，请确保始终遵循最新版本的规范要求。