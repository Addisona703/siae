# SIAE 认证服务权限定义

## 📋 权限概述

SIAE认证服务采用基于角色的访问控制（RBAC）模型，通过权限编码进行细粒度的权限控制。本文档基于 `AuthPermissions` 类中的实际权限常量，详细说明了系统中所有权限的定义、分组和使用场景。

## 🔐 权限编码规范

### 编码格式
```
auth:{模块名}:{操作名}
```

### 示例
- `auth:permission:query` - 权限查询
- `auth:role:add` - 角色添加
- `auth:user:role:assign` - 用户角色分配

## 📚 权限分类

### 1. 权限管理权限 (auth:permission:*)

| 权限编码 | 权限名称 | 权限描述 | 使用场景 |
|----------|----------|----------|----------|
| `auth:permission:query` | 权限查询 | 查看权限信息和列表 | 权限列表页面、权限详情页面 |
| `auth:permission:add` | 权限添加 | 创建新权限 | 权限创建表单 |
| `auth:permission:edit` | 权限编辑 | 修改权限信息 | 权限编辑表单 |
| `auth:permission:delete` | 权限删除 | 删除权限 | 权限删除按钮 |

### 2. 角色管理权限 (auth:role:*)

| 权限编码 | 权限名称 | 权限描述 | 使用场景 |
|----------|----------|----------|----------|
| `auth:role:query` | 角色查询 | 查看角色信息和列表 | 角色列表页面、角色详情页面 |
| `auth:role:add` | 角色添加 | 创建新角色 | 角色创建表单 |
| `auth:role:edit` | 角色编辑 | 修改角色信息 | 角色编辑表单 |
| `auth:role:delete` | 角色删除 | 删除角色 | 角色删除按钮 |

### 3. 用户角色管理权限 (auth:user:role:*)

| 权限编码 | 权限名称 | 权限描述 | 使用场景 |
|----------|----------|----------|----------|
| `auth:user:role:query` | 用户角色查询 | 查看用户角色关联信息 | 用户角色列表页面 |
| `auth:user:role:assign` | 分配用户角色 | 为用户分配角色 | 用户角色分配页面 |
| `auth:user:role:update` | 更新用户角色 | 更新用户角色关联 | 用户角色编辑页面 |
| `auth:user:role:remove` | 移除用户角色 | 移除用户的角色 | 用户角色管理页面 |

### 4. 用户权限管理权限 (auth:user:permission:*)

| 权限编码 | 权限名称 | 权限描述 | 使用场景 |
|----------|----------|----------|----------|
| `auth:user:permission:query` | 用户权限查询 | 查看用户权限关联信息 | 用户权限列表页面 |
| `auth:user:permission:assign` | 分配用户权限 | 为用户直接分配权限 | 用户权限分配页面 |
| `auth:user:permission:remove` | 移除用户权限 | 移除用户的直接权限 | 用户权限管理页面 |

### 5. 日志管理权限 (auth:log:*)

| 权限编码 | 权限名称 | 权限描述 | 使用场景 |
|----------|----------|----------|----------|
| `auth:log:query` | 日志查询 | 查看系统日志信息 | 日志查询页面、登录日志查看 |
| `auth:log:export` | 日志导出 | 导出日志数据 | 日志导出功能 |

## 🎯 权限层级关系

### 菜单权限层级

```
认证管理
├── 权限管理
│   ├── 权限查询 (auth:permission:query)
│   ├── 权限添加 (auth:permission:add)
│   ├── 权限编辑 (auth:permission:edit)
│   └── 权限删除 (auth:permission:delete)
├── 角色管理
│   ├── 角色查询 (auth:role:query)
│   ├── 角色添加 (auth:role:add)
│   ├── 角色编辑 (auth:role:edit)
│   └── 角色删除 (auth:role:delete)
├── 用户角色管理
│   ├── 用户角色查询 (auth:user:role:query)
│   ├── 用户角色分配 (auth:user:role:assign)
│   ├── 用户角色更新 (auth:user:role:update)
│   └── 用户角色移除 (auth:user:role:remove)
├── 用户权限管理
│   ├── 用户权限查询 (auth:user:permission:query)
│   ├── 用户权限分配 (auth:user:permission:assign)
│   └── 用户权限移除 (auth:user:permission:remove)
└── 日志管理
    ├── 日志查询 (auth:log:query)
    └── 日志导出 (auth:log:export)
```

## 👥 预定义角色

### 1. 超级管理员 (ROLE_SUPER_ADMIN)
- **描述**: 系统超级管理员，拥有所有权限
- **权限**: 所有 `auth:*` 权限
- **使用场景**: 系统初始化、紧急维护

### 2. 系统管理员 (ROLE_ADMIN)
- **描述**: 系统管理员，负责权限和角色管理
- **权限**: 
  - `auth:permission:*` (权限管理)
  - `auth:role:*` (角色管理)
  - `auth:user:role:*` (用户角色管理)
  - `auth:log:query` (日志查询)
- **使用场景**: 日常权限管理、角色分配

### 3. 权限管理员 (ROLE_PERMISSION_ADMIN)
- **描述**: 权限管理员，负责权限配置
- **权限**:
  - `auth:permission:*` (权限管理)
  - `auth:user:permission:*` (用户权限管理)
- **使用场景**: 权限配置、权限分配

### 4. 审计员 (ROLE_AUDITOR)
- **描述**: 系统审计员，只能查看日志和权限信息
- **权限**:
  - `auth:permission:query` (权限查询)
  - `auth:role:query` (角色查询)
  - `auth:user:role:query` (用户角色查询)
  - `auth:user:permission:query` (用户权限查询)
  - `auth:log:*` (日志管理)
- **使用场景**: 安全审计、合规检查

### 5. 普通用户 (ROLE_USER)
- **描述**: 系统普通用户，基础权限
- **权限**: 无特殊权限，仅能访问公开接口
- **使用场景**: 普通用户基础功能

## 🔧 权限配置

### 1. 权限注解使用

在Controller方法上使用 `@SiaeAuthorize` 注解：

```java
@SiaeAuthorize("hasAuthority('" + AUTH_PERMISSION_QUERY + "')")
@GetMapping("/permissions")
public Result<List<PermissionVO>> getPermissions() {
    // 方法实现
}

@SiaeAuthorize("hasAuthority('" + AUTH_ROLE_ADD + "')")
@PostMapping("/roles")
public Result<RoleVO> createRole(@RequestBody RoleCreateDTO dto) {
    // 方法实现
}
```

### 2. 权限常量引用

```java
// 在AuthPermissions类中定义的权限常量
public static final String AUTH_PERMISSION_QUERY = "auth:permission:query";
public static final String AUTH_PERMISSION_ADD = "auth:permission:add";
public static final String AUTH_PERMISSION_EDIT = "auth:permission:edit";
public static final String AUTH_PERMISSION_DELETE = "auth:permission:delete";

public static final String AUTH_ROLE_QUERY = "auth:role:query";
public static final String AUTH_ROLE_ADD = "auth:role:add";
public static final String AUTH_ROLE_EDIT = "auth:role:edit";
public static final String AUTH_ROLE_DELETE = "auth:role:delete";

public static final String AUTH_USER_ROLE_QUERY = "auth:user:role:query";
public static final String AUTH_USER_ROLE_ASSIGN = "auth:user:role:assign";
public static final String AUTH_USER_ROLE_UPDATE = "auth:user:role:update";
public static final String AUTH_USER_ROLE_REMOVE = "auth:user:role:remove";

public static final String AUTH_USER_PERMISSION_QUERY = "auth:user:permission:query";
public static final String AUTH_USER_PERMISSION_ASSIGN = "auth:user:permission:assign";
public static final String AUTH_USER_PERMISSION_REMOVE = "auth:user:permission:remove";

public static final String AUTH_LOG_QUERY = "auth:log:query";
public static final String AUTH_LOG_EXPORT = "auth:log:export";
```

### 3. 复合权限控制

```java
// 需要多个权限之一
@SiaeAuthorize("hasAuthority('" + AUTH_PERMISSION_QUERY + "') or hasAuthority('" + AUTH_ROLE_QUERY + "')")

// 需要同时拥有多个权限
@SiaeAuthorize("hasAuthority('" + AUTH_PERMISSION_EDIT + "') and hasAuthority('" + AUTH_PERMISSION_QUERY + "')")

// 角色和权限组合
@SiaeAuthorize("hasRole('ADMIN') or hasAuthority('" + AUTH_PERMISSION_QUERY + "')")
```

## 📝 RBAC权限模型实现

### 1. 权限计算规则

系统采用以下权限计算规则：
```
用户最终权限 = 角色权限 ∪ 直接权限
```

- **角色权限**: 用户通过角色获得的权限（间接权限）
- **直接权限**: 直接为用户分配的权限（直接权限，优先级更高）

### 2. 权限缓存机制

- **Redis缓存**: 用户权限信息缓存到Redis，提高查询性能
- **缓存键格式**: 
  - 权限缓存: `auth:perms:{userId}`
  - 角色缓存: `auth:roles:{userId}`
- **缓存过期**: 与JWT令牌过期时间保持一致

### 3. 权限验证流程

1. **JWT解析**: 从请求头中解析JWT令牌获取用户ID
2. **权限查询**: 从Redis缓存中查询用户权限列表
3. **权限验证**: 验证用户是否拥有所需权限
4. **访问控制**: 根据验证结果允许或拒绝访问

## 📋 权限管理最佳实践

### 1. 权限设计原则

- **最小权限原则**: 用户只获得完成工作所需的最小权限
- **职责分离**: 不同角色承担不同职责，避免权限过度集中
- **权限继承**: 合理利用角色权限和直接权限的组合
- **定期审查**: 定期审查和清理不必要的权限

### 2. 安全注意事项

- **权限验证**: 在每个需要权限控制的接口上添加权限验证
- **参数校验**: 对权限相关的参数进行严格校验
- **日志记录**: 记录权限相关的操作日志，便于审计
- **异常处理**: 权限不足时返回明确的错误信息

### 3. 性能优化

- **缓存策略**: 合理使用Redis缓存，减少数据库查询
- **批量操作**: 支持批量权限分配，提高操作效率
- **索引优化**: 在权限相关表上建立合适的索引
- **异步处理**: 权限变更后异步更新缓存

---

**最后更新**: 2024-01-01  
**文档版本**: v1.0.0  
**维护人员**: SIAE开发团队
