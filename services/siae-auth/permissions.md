# SIAE 系统权限定义文档

## 📋 目录

- [权限命名规范](#权限命名规范)
- [认证模块权限](#认证模块权限)
- [内容模块权限](#内容模块权限)
- [用户模块权限](#用户模块权限)
- [权限使用示例](#权限使用示例)

## 🔧 权限命名规范

### 权限编码规范
- **格式**: `模块:资源:操作`
- **示例**: `user:profile:view`、`content:article:create`

### 常量命名规范
- **格式**: `模块_资源_操作`（全大写，下划线分隔）
- **示例**: `USER_PROFILE_VIEW`、`CONTENT_ARTICLE_CREATE`

### 操作类型说明
| 操作 | 说明 | 示例 |
|------|------|------|
| create | 创建/新增 | `user:profile:create` |
| update | 更新/修改 | `user:profile:update` |
| delete | 删除 | `user:profile:delete` |
| view | 查看详情 | `user:profile:view` |
| list | 列表查询 | `user:profile:list` |
| query | 通用查询 | `content:article:query` |
| publish | 发布 | `content:article:publish` |
| edit | 编辑 | `content:article:edit` |
| handle | 处理 | `content:audit:handle` |
| approve | 审核通过 | `content:audit:approve` |
| reject | 审核拒绝 | `content:audit:reject` |
| toggle | 状态切换 | `content:category:toggle` |

---

## 🔐 认证模块权限 (AuthPermissions)

> **文件位置**: `siae-core/src/main/java/com/hngy/siae/core/permissions/AuthPermissions.java`

### 系统管理权限

#### 用户管理
| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `AUTH_USER_QUERY` | `auth:user:query` | 查询用户 |
| `AUTH_USER_ADD` | `auth:user:add` | 新增用户 |
| `AUTH_USER_EDIT` | `auth:user:edit` | 修改用户 |
| `AUTH_USER_DELETE` | `auth:user:delete` | 删除用户 |

#### 角色管理
| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `AUTH_ROLE_QUERY` | `auth:role:query` | 查询角色 |
| `AUTH_ROLE_ADD` | `auth:role:add` | 新增角色 |
| `AUTH_ROLE_EDIT` | `auth:role:edit` | 修改角色 |
| `AUTH_ROLE_DELETE` | `auth:role:delete` | 删除角色 |

#### 权限管理
| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `AUTH_PERMISSION_QUERY` | `auth:permission:query` | 查询权限 |
| `AUTH_PERMISSION_ADD` | `auth:permission:add` | 新增权限 |
| `AUTH_PERMISSION_EDIT` | `auth:permission:edit` | 修改权限 |
| `AUTH_PERMISSION_DELETE` | `auth:permission:delete` | 删除权限 |

#### 日志管理
| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `AUTH_LOG_QUERY` | `auth:log:query` | 查询登录日志 |
| `AUTH_LOG_EXPORT` | `auth:log:export` | 导出登录日志 |

#### 用户角色关联管理
| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `AUTH_USER_ROLE_ASSIGN` | `auth:user:role:assign` | 分配用户角色 |
| `AUTH_USER_ROLE_QUERY` | `auth:user:role:query` | 查询用户角色 |
| `AUTH_USER_ROLE_REMOVE` | `auth:user:role:remove` | 移除用户角色 |

#### 用户权限关联管理
| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `AUTH_USER_PERMISSION_ASSIGN` | `auth:user:permission:assign` | 分配用户权限 |
| `AUTH_USER_PERMISSION_QUERY` | `auth:user:permission:query` | 查询用户权限 |
| `AUTH_USER_PERMISSION_REMOVE` | `auth:user:permission:remove` | 移除用户权限 |

---

## 📝 内容模块权限 (ContentPermissions)

> **文件位置**: `siae-core/src/main/java/com/hngy/siae/core/permissions/ContentPermissions.java`

### 内容管理权限

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `CONTENT_PUBLISH` | `content:publish` | 发布内容 |
| `CONTENT_EDIT` | `content:edit` | 编辑内容 |
| `CONTENT_DELETE` | `content:delete` | 删除内容 |
| `CONTENT_QUERY` | `content:query` | 查询内容 |
| `CONTENT_LIST_VIEW` | `content:list:view` | 查询内容列表 |
| `CONTENT_HOT_VIEW` | `content:hot:view` | 查询热门内容 |

### 分类管理权限

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `CONTENT_CATEGORY_CREATE` | `content:category:create` | 创建分类 |
| `CONTENT_CATEGORY_EDIT` | `content:category:edit` | 编辑分类 |
| `CONTENT_CATEGORY_DELETE` | `content:category:delete` | 删除分类 |
| `CONTENT_CATEGORY_VIEW` | `content:category:view` | 查询分类 |
| `CONTENT_CATEGORY_TOGGLE` | `content:category:toggle` | 启用/禁用分类 |

### 标签管理权限

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `CONTENT_TAG_CREATE` | `content:tag:create` | 创建标签 |
| `CONTENT_TAG_EDIT` | `content:tag:edit` | 编辑标签 |
| `CONTENT_TAG_DELETE` | `content:tag:delete` | 删除标签 |
| `CONTENT_TAG_VIEW` | `content:tag:view` | 查询标签 |

### 用户交互权限

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `CONTENT_INTERACTION_RECORD` | `content:interaction:record` | 记录用户行为（点赞、收藏、浏览等） |
| `CONTENT_INTERACTION_CANCEL` | `content:interaction:cancel` | 取消用户行为 |

### 统计查询权限

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `CONTENT_STATISTICS_VIEW` | `content:statistics:view` | 查看内容统计 |
| `CONTENT_STATISTICS_UPDATE` | `content:statistics:update` | 更新内容统计 |

### 审核管理权限

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `CONTENT_AUDIT_HANDLE` | `content:audit:handle` | 处理内容审核 |
| `CONTENT_AUDIT_VIEW` | `content:audit:view` | 查看审核列表 |
| `CONTENT_AUDIT_APPROVE` | `content:audit:approve` | 审核通过 |
| `CONTENT_AUDIT_REJECT` | `content:audit:reject` | 审核拒绝 |

### 评论管理权限（预留）

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `CONTENT_COMMENT_CREATE` | `content:comment:create` | 创建评论 |
| `CONTENT_COMMENT_EDIT` | `content:comment:edit` | 编辑评论 |
| `CONTENT_COMMENT_DELETE` | `content:comment:delete` | 删除评论 |
| `CONTENT_COMMENT_VIEW` | `content:comment:view` | 查询评论 |

---

## 👥 用户模块权限 (UserPermissions)

> **文件位置**: `siae-core/src/main/java/com/hngy/siae/core/permissions/UserPermissions.java`

### 用户管理权限

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `USER_CREATE` | `user:create` | 创建用户 |
| `USER_UPDATE` | `user:update` | 更新用户 |
| `USER_DELETE` | `user:delete` | 删除用户 |
| `USER_VIEW` | `user:view` | 查询用户 |
| `USER_LIST` | `user:list` | 分页查询用户列表 |

### 用户详情管理权限

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `USER_PROFILE_CREATE` | `user:profile:create` | 创建用户详情 |
| `USER_PROFILE_UPDATE` | `user:profile:update` | 更新用户详情 |
| `USER_PROFILE_DELETE` | `user:profile:delete` | 删除用户详情 |
| `USER_PROFILE_VIEW` | `user:profile:view` | 查询用户详情 |

### 正式成员管理权限

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `USER_MEMBER_UPDATE` | `user:member:update` | 更新正式成员 |
| `USER_MEMBER_VIEW` | `user:member:view` | 查询正式成员 |
| `USER_MEMBER_LIST` | `user:member:list` | 分页查询正式成员列表 |

### 候选成员管理权限

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `USER_CANDIDATE_CREATE` | `user:candidate:create` | 添加候选成员 |
| `USER_CANDIDATE_UPDATE` | `user:candidate:update` | 更新候选成员 |
| `USER_CANDIDATE_DELETE` | `user:candidate:delete` | 删除候选成员 |
| `USER_CANDIDATE_VIEW` | `user:candidate:view` | 查询候选成员 |
| `USER_CANDIDATE_LIST` | `user:candidate:list` | 分页查询候选成员列表 |

### 班级管理权限

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `USER_CLASS_CREATE` | `user:class:create` | 创建班级 |
| `USER_CLASS_UPDATE` | `user:class:update` | 更新班级 |
| `USER_CLASS_DELETE` | `user:class:delete` | 删除班级 |
| `USER_CLASS_VIEW` | `user:class:view` | 查询班级 |
| `USER_CLASS_LIST` | `user:class:list` | 分页查询班级列表 |

### 获奖记录管理权限

| 权限常量 | 权限编码 | 权限描述 |
|----------|----------|----------|
| `USER_AWARD_CREATE` | `user:award:create` | 创建获奖记录 |
| `USER_AWARD_UPDATE` | `user:award:update` | 更新获奖记录 |
| `USER_AWARD_DELETE` | `user:award:delete` | 删除获奖记录 |
| `USER_AWARD_VIEW` | `user:award:view` | 查询获奖记录 |
| `USER_AWARD_LIST` | `user:award:list` | 分页查询获奖记录列表 |

---

sql中插入数据：
```sql
```


## 💡 权限使用示例

### 1. 在控制器中使用权限注解

```java
@RestController
@RequestMapping("/api/v1/content")
public class ContentController {

    // 使用权限常量进行权限控制
    @PostMapping("/publish")
    @PreAuthorize("hasAuthority('" + ContentPermissions.SYSTEM_CONTENT_PUBLISH + "')")
    public Result<ContentVO> publishContent(@RequestBody ContentDTO contentDTO) {
        // 发布内容的业务逻辑
        return Result.success();
    }

    // 使用权限编码进行权限控制
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('content:list:view')")
    public Result<PageVO<ContentVO>> getContentList(@RequestParam int page, @RequestParam int size) {
        // 查询内容列表的业务逻辑
        return Result.success();
    }

    // 复合权限控制
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('content:edit') or hasRole('ADMIN')")
    public Result<ContentVO> updateContent(@PathVariable Long id, @RequestBody ContentDTO contentDTO) {
        // 更新内容的业务逻辑
        return Result.success();
    }
}
```

### 2. 在服务层中进行权限检查

```java
@Service
public class ContentService {

    @Autowired
    private RedisPermissionService redisPermissionService;

    public void deleteContent(Long contentId, Long userId) {
        // 检查用户是否有删除权限
        List<String> userPermissions = redisPermissionService.getAllUserAuthorities(userId);

        if (!userPermissions.contains(ContentPermissions.SYSTEM_CONTENT_DELETE)) {
            throw new ServiceException("权限不足，无法删除内容");
        }

        // 执行删除逻辑
        // ...
    }
}
```

### 3. 前端权限控制示例

```javascript
// 权限常量定义（与后端保持一致）
const PERMISSIONS = {
    // 内容管理权限
    CONTENT_PUBLISH: 'content:publish',
    CONTENT_EDIT: 'content:edit',
    CONTENT_DELETE: 'content:delete',
    CONTENT_QUERY: 'content:query',

    // 用户管理权限
    USER_CREATE: 'user:user:create',
    USER_UPDATE: 'user:user:update',
    USER_DELETE: 'user:user:delete',
    USER_VIEW: 'user:user:view'
};

// 权限检查函数
function hasPermission(permission) {
    const userPermissions = getUserPermissions(); // 从本地存储或API获取用户权限
    return userPermissions.includes(permission);
}

// 在Vue组件中使用
export default {
    computed: {
        canPublishContent() {
            return hasPermission(PERMISSIONS.CONTENT_PUBLISH);
        },
        canEditContent() {
            return hasPermission(PERMISSIONS.CONTENT_EDIT);
        }
    },

    template: `
        <div>
            <button v-if="canPublishContent" @click="publishContent">发布内容</button>
            <button v-if="canEditContent" @click="editContent">编辑内容</button>
        </div>
    `
};
```

### 4. 权限初始化脚本示例

```sql
-- 插入内容管理权限
INSERT INTO permission (name, code, type, parent_id, sort_order) VALUES
('内容管理', 'content', 'menu', NULL, 1),
('发布内容', 'content:publish', 'button', 1, 1),
('编辑内容', 'content:edit', 'button', 1, 2),
('删除内容', 'content:delete', 'button', 1, 3),
('查询内容', 'content:query', 'button', 1, 4);

-- 插入用户管理权限
INSERT INTO permission (name, code, type, parent_id, sort_order) VALUES
('用户管理', 'user', 'menu', NULL, 2),
('创建用户', 'user:user:create', 'button', 2, 1),
('更新用户', 'user:user:update', 'button', 2, 2),
('删除用户', 'user:user:delete', 'button', 2, 3),
('查询用户', 'user:user:view', 'button', 2, 4);

-- 为管理员角色分配权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission WHERE code IN (
    'content:publish',
    'content:edit',
    'content:delete',
    'content:query',
    'user:user:create',
    'user:user:update',
    'user:user:delete',
    'user:user:view'
);
```

---

## 📊 权限统计

### 权限数量统计

| 模块 | 权限数量 | 状态 |
|------|----------|------|
| 认证模块 (AuthPermissions) | 20 | ✅ 已实现 |
| 内容模块 (ContentPermissions) | 18 | ✅ 已实现 |
| 用户模块 (UserPermissions) | 20 | ✅ 已实现 |
| **总计** | **58** | **全部完成** |

### 权限分类统计

| 分类 | 数量 | 说明 |
|------|------|------|
| 系统管理权限 | 20 | 用户、角色、权限、日志、用户角色关联、用户权限关联管理 |
| 内容管理权限 | 18 | 内容发布、分类、标签、审核等 |
| 用户管理权限 | 20 | 用户信息、成员、班级、获奖记录等 |

---

## 🔄 权限更新日志

| 日期 | 版本 | 更新内容 |
|------|------|----------|
| 2024-01-01 | v1.0.0 | 初始版本，定义基础权限结构 |
| 2024-01-01 | v1.1.0 | 完善内容模块权限定义 |
| 2024-01-01 | v1.2.0 | 完善用户模块权限定义 |
| 2024-01-01 | v1.3.0 | 添加权限使用示例和统计信息 |
| 2024-01-01 | v1.4.0 | 完成认证模块权限定义和控制器权限注解重构 |

---

**注意事项**:
1. 权限常量定义在 `siae-core` 模块中，确保各服务间的一致性
2. 权限编码采用 `模块:资源:操作` 的格式，便于理解和维护
3. 新增权限时需要同时更新常量定义和数据库初始化脚本
4. 建议定期审查权限设计，确保符合业务需求和安全要求