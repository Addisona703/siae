# SIAE 项目 API 接口清单

## 📋 目录

- [认证服务 (siae-auth)](#认证服务-siae-auth)
- [用户服务 (siae-user)](#用户服务-siae-user)
- [内容服务 (siae-content)](#内容服务-siae-content)
- [消息服务 (siae-message)](#消息服务-siae-message)
- [接口统计](#接口统计)

---

## 🔐 认证服务 (siae-auth)

**服务端口**: 8000
**上下文路径**: `/api/v1/auth`
**Swagger UI**: http://localhost:8000/api/v1/auth/swagger-ui.html

### 认证管理 (AuthController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/login` | 用户登录 | 无 (公开接口) |
| POST | `/register` | 用户注册 | 无 (公开接口) |
| POST | `/refresh-token` | 刷新访问令牌 | 无 (公开接口) |
| POST | `/logout` | 用户登出 | 需要认证 |

### 权限管理 (PermissionController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/permissions` | 创建权限 | `auth:permission:add` |
| POST | `/permissions/page` | 分页查询权限列表 | `auth:permission:query` |
| GET | `/permissions/tree` | 查询权限树结构 | `auth:permission:query` |
| PUT | `/permissions/tree/batch` | 批量更新权限树结构 | `auth:permission:edit` |
| GET | `/permissions/{permissionId}` | 获取权限详情 | `auth:permission:query` |
| PUT | `/permissions/{permissionId}` | 更新权限 | `auth:permission:edit` |
| DELETE | `/permissions/{permissionId}` | 删除权限 | `auth:permission:delete` |

### 角色管理 (RoleController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/roles` | 创建角色 | `auth:role:add` |
| POST | `/roles/page` | 分页查询角色列表 | `auth:role:query` |
| GET | `/roles` | 获取所有角色 | `auth:role:query` |
| GET | `/roles/{roleId}` | 获取角色详情 | `auth:role:query` |
| PUT | `/roles/{roleId}` | 更新角色 | `auth:role:edit` |
| DELETE | `/roles/{roleId}` | 删除角色 | `auth:role:delete` |
| POST | `/roles/{roleId}/permissions` | 分配角色权限 | `auth:role:edit` |
| GET | `/roles/{roleId}/permissions` | 获取角色权限 | `auth:role:query` |
| DELETE | `/roles/{roleId}/permissions` | 移除角色权限 | `auth:role:edit` |

### 用户角色管理 (UserRoleController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/users/{userId}/role` | 为用户分配单个角色 | `auth:user:role:assign` |
| POST | `/users/roles/batch` | 批量分配用户角色 | `auth:user:role:assign` |
| POST | `/users/roles/page` | 分页查询用户角色 | `auth:user:role:query` |
| PUT | `/users/roles/{userRoleId}` | 更新用户角色关联 | `auth:user:role:update` |

### 用户权限管理 (UserPermissionController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| GET | `/user-permission/list/{userId}` | 分页查询用户权限 | `auth:user:permission:query` |
| POST | `/user-permission/assign` | 分配用户权限（覆盖模式） | `auth:user:permission:assign` |
| POST | `/user-permission/append` | 追加用户权限（增量模式） | `auth:user:permission:assign` |
| DELETE | `/user-permission/remove/all/{userId}` | 移除用户所有权限 | `auth:user:permission:remove` |
| DELETE | `/user-permission/remove` | 移除用户指定权限 | `auth:user:permission:remove` |

### 日志管理 (LogController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/logs/login` | 获取登录日志 | `auth:log:query` |
| POST | `/logs/login/fail` | 获取登录失败日志 | `auth:log:query` |

---

## 👥 用户服务 (siae-user)

**服务端口**: 8020
**上下文路径**: `/api/v1/users`
**Swagger UI**: http://localhost:8020/api/v1/user/swagger-ui.html

### 用户管理 (UserController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/create` | 创建用户 | `user:create` |
| PUT | `/update` | 更新用户 | `user:update` |
| DELETE | `/{id}` | 删除用户 | `user:delete` |
| GET | `/{id}` | 查询用户详情 | `user:view` |
| POST | `/page` | 分页查询用户列表 | `user:list` |

### 用户详情管理 (UserProfileController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/user-profiles` | 创建用户详情 | `user:profile:create` |
| PUT | `/user-profiles` | 更新用户详情 | `user:profile:update` |
| DELETE | `/user-profiles/{id}` | 删除用户详情 | `user:profile:delete` |
| GET | `/user-profiles/{id}` | 查询用户详情 | `user:profile:view` |

### 正式成员管理 (MemberController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| PUT | `/members` | 更新正式成员 | `user:member:update` |
| GET | `/members/{id}` | 查询正式成员详情 | `user:member:view` |
| POST | `/members/list` | 动态条件查询正式成员列表 | `user:member:list` |
| POST | `/members/page` | 分页查询正式成员列表 | `user:member:list` |

### 候选成员管理 (MemberCandidateController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/candidates` | 添加候选成员 | `user:candidate:create` |
| PUT | `/candidates` | 更新候选成员 | `user:candidate:update` |
| DELETE | `/candidates/{id}` | 删除候选成员 | `user:candidate:delete` |
| GET | `/candidates/{id}` | 查询候选成员详情 | `user:candidate:view` |
| POST | `/candidates/page` | 分页查询候选成员列表 | `user:candidate:list` |

### 班级管理 (ClassInfoController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/classes` | 创建班级 | `user:class:create` |
| PUT | `/classes` | 更新班级 | `user:class:update` |
| DELETE | `/classes/{id}` | 删除班级 | `user:class:delete` |
| GET | `/classes/{id}` | 查询班级详情 | `user:class:view` |
| POST | `/classes/page` | 分页查询班级列表 | `user:class:list` |
| GET | `/classes/college/{collegeId}` | 根据学院ID查询班级列表 | `user:class:view` |
| GET | `/classes/major/{majorId}` | 根据专业ID查询班级列表 | `user:class:view` |
| GET | `/classes/year/{year}` | 根据入学年份查询班级列表 | `user:class:view` |

### 奖项类型管理 (AwardTypeController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/award-types` | 创建奖项类型 | `user:award-type:create` |
| PUT | `/award-types` | 更新奖项类型 | `user:award-type:update` |
| DELETE | `/award-types/{id}` | 删除奖项类型 | `user:award-type:delete` |
| GET | `/award-types/{id}` | 查询奖项类型详情 | `user:award-type:view` |
| GET | `/award-types/name/{name}` | 根据名称获取奖项类型 | `user:award-type:view` |
| GET | `/award-types` | 获取所有奖项类型列表 | `user:award-type:list` |
| POST | `/award-types/page` | 分页查询奖项类型列表 | `user:award-type:list` |

### 奖项等级管理 (AwardLevelController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/award-levels` | 创建奖项等级 | `user:award-level:create` |
| PUT | `/award-levels` | 更新奖项等级 | `user:award-level:update` |
| DELETE | `/award-levels/{id}` | 删除奖项等级 | `user:award-level:delete` |
| GET | `/award-levels/{id}` | 查询奖项等级详情 | `user:award-level:view` |
| GET | `/award-levels/name/{name}` | 根据名称获取奖项等级 | `user:award-level:view` |
| GET | `/award-levels` | 获取所有奖项等级列表 | `user:award-level:list` |
| POST | `/award-levels/page` | 分页查询奖项等级列表 | `user:award-level:list` |

### 用户获奖记录管理 (UserAwardController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/user-awards` | 创建用户获奖记录 | `user:award:create` |
| PUT | `/user-awards` | 更新用户获奖记录 | `user:award:update` |
| DELETE | `/user-awards/{id}` | 删除用户获奖记录 | `user:award:delete` |
| GET | `/user-awards/{id}` | 查询用户获奖记录详情 | `user:award:view` |
| POST | `/user-awards/page` | 分页查询用户获奖记录列表 | `user:award:list` |

---

## 📝 内容服务 (siae-content)

**服务端口**: 8010
**上下文路径**: `/api/v1/content`
**Swagger UI**: http://localhost:8010/api/v1/content/swagger-ui.html

### 内容管理 (ContentController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/` | 发布内容 | `content:publish` |
| PUT | `/` | 编辑内容 | `content:edit` |
| DELETE | `/` | 删除内容 | `content:delete` |
| GET | `/query/{contentId}` | 查询内容详情 | `content:query` |
| GET | `/` | 查询内容列表 | `content:list:view` |
| GET | `/hot` | 查询热门内容 | 无 (公开接口) |

### 标签管理 (TagsController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/tags` | 创建标签 | `content:tag:create` |
| PUT | `/tags` | 编辑标签 | `content:tag:edit` |
| DELETE | `/tags` | 删除标签 | `content:tag:delete` |
| GET | `/tags` | 查询标签列表 | `content:tag:view` |

### 分类管理 (CategoriesController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/categories` | 创建分类 | `content:category:create` |
| PUT | `/categories` | 更新分类 | `content:category:edit` |
| DELETE | `/categories/{categoryId}` | 删除分类 | `content:category:delete` |
| GET | `/categories` | 分页查询分类列表 | `content:category:view` |
| GET | `/categories/detail/{categoryId}` | 查询分类详情 | `content:category:view` |
| POST | `/categories/toggle-enable` | 启用/禁用分类 | `content:category:toggle` |

### 审核管理 (AuditsController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| PUT | `/audits/{id}` | 处理审核 | `content:audit:handle` |
| GET | `/audits/pending` | 获取待审核列表 | `content:audit:view` |
| GET | `/audits` | 获取审核记录 | `content:audit:view` |

### 评论管理 (CommentsController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/comments/{contentId}` | 创建评论 | 无 (公开接口) |
| PUT | `/comments/{commentId}` | 更新评论 | 无 (公开接口) |
| DELETE | `/comments/{id}` | 删除评论 | 无 (公开接口) |
| GET | `/comments/{contentId}` | 查询评论列表 | 无 (公开接口) |
| POST | `/comments/page` | 分页查询评论 | 无 (公开接口) |

### 用户交互管理 (InteractionsController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/interactions/action` | 记录用户行为 | `content:interaction:record` |
| DELETE | `/interactions/action` | 取消用户行为 | `content:interaction:cancel` |

### 统计管理 (StatisticsController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| GET | `/statistics/{contentId}` | 查询内容统计数据 | `content:statistics:view` |
| PUT | `/statistics/{contentId}` | 更新内容统计信息 | `content:statistics:update` |
<!-- 
### 认证测试 (AuthTestController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| GET | `/auth-test/basic` | 基本认证测试 | 需要认证 |
| GET | `/auth-test/content-create` | 内容创建权限测试 | `CONTENT_CREATE` |
| GET | `/auth-test/content-manage` | 内容管理权限测试 | `CONTENT_MANAGE` |
| GET | `/auth-test/admin-role` | 管理员角色测试 | `ROLE_ADMIN` |
| GET | `/auth-test/multiple-permissions` | 复合权限测试 | `CONTENT_CREATE` + `CONTENT_MANAGE` |
| GET | `/auth-test/auth-info` | 认证信息查看 | 需要认证 | -->

---

## 📧 消息服务 (siae-message)

**服务端口**: 8030
**上下文路径**: `/api/v1/message`
**Swagger UI**: http://localhost:8030/api/v1/message/swagger-ui.html

### 邮件管理 (EmailController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/email/code/send` | 发送邮箱验证码 | 无 (公开接口) |
| POST | `/email/code/verify` | 验证邮箱验证码 | 无 (公开接口) |

---

## 📊 接口统计

### 按服务统计

| 服务名称 | 控制器数量 | 接口数量 | 权限保护接口 | 公开接口 |
|----------|------------|----------|--------------|----------|
| **认证服务 (siae-auth)** | 6 | 25 | 21 | 4 |
| **用户服务 (siae-user)** | 8 | 44 | 44 | 0 |
| **内容服务 (siae-content)** | 7 | 28 | 17 | 11 |
| **消息服务 (siae-message)** | 1 | 2 | 0 | 2 |
| **总计** | **22** | **99** | **82** | **17** |

### 按HTTP方法统计

| HTTP方法 | 接口数量 | 占比 |
|----------|----------|------|
| GET | 40 | 40.4% |
| POST | 36 | 36.4% |
| PUT | 12 | 12.1% |
| DELETE | 11 | 11.1% |

### 权限分类统计

| 权限类型 | 接口数量 | 说明 |
|----------|----------|------|
| 系统管理权限 | 21 | 认证、角色、权限、日志管理 |
| 用户管理权限 | 44 | 用户信息、成员、班级、奖项管理 |
| 内容管理权限 | 17 | 内容发布、标签、分类、审核管理 |
| 公开接口 | 17 | 无需权限验证的接口 |

---

## 📝 使用说明

### 1. 接口访问方式

#### 直接访问各微服务
```
认证服务: http://localhost:8000/api/v1/auth/{endpoint}
用户服务: http://localhost:8020/api/v1/user/{endpoint}
内容服务: http://localhost:8010/api/v1/content/{endpoint}
消息服务: http://localhost:8030/api/v1/message/{endpoint}
```

**示例**:
- 用户登录: `POST http://localhost:8000/api/v1/auth/login`
- 创建用户: `POST http://localhost:8020/api/v1/user/create`
- 发布内容: `POST http://localhost:8010/api/v1/content/`
- 发送邮件验证码: `POST http://localhost:8030/api/v1/message/email/code/send`

### 2. 权限验证

- **需要认证**: 请求头中需要包含有效的JWT Token
- **权限要求**: 用户必须拥有对应的权限才能访问
- **公开接口**: 无需任何认证即可访问

### 3. API文档访问

每个服务都提供独立的Swagger UI文档：

- **认证服务**: http://localhost:8000/api/v1/auth/swagger-ui.html
- **用户服务**: http://localhost:8020/api/v1/user/swagger-ui.html
- **内容服务**: http://localhost:8010/api/v1/content/swagger-ui.html
- **消息服务**: http://localhost:8030/api/v1/message/swagger-ui.html

### 4. 开发规范

#### Swagger注解规范
- **只允许使用**: `@Tag`, `@Operation`, `@Parameter`
- **禁止使用**: `@ApiResponses`, `@ApiResponse`, `@Content`, `@Schema`

#### 权限注解规范
- **必须使用**: `@SiaeAuthorize`
- **禁止使用**: `@PreAuthorize`

---

## 🔄 更新日志

| 日期 | 版本 | 更新内容 |
|------|------|----------|
| 2024-01-01 | v1.0.0 | 初始版本，包含所有微服务API接口清单 |
| 2024-01-01 | v1.1.0 | 完善权限注解，统一路径映射规范 |
| 2024-01-01 | v1.2.0 | 添加接口统计和使用说明 |
| 2025-01-01 | v2.0.0 | 全面更新接口信息，移除网关相关内容，更新统计数据 |

### v2.0.0 更新内容
- ✅ 更新了所有Controller的接口信息
- ✅ 添加了遗漏的接口（如奖项类型/等级的按名称查询）
- ✅ 修正了分类管理、审核管理、评论管理的接口路径
- ✅ 更新了接口统计数据（总计99个接口）
- ✅ 移除了网关相关的访问方式说明
- ✅ 添加了开发规范说明（Swagger注解、权限注解）
- ✅ 更新了权限分类统计

---

**最后更新**: 2025-08-01
**文档版本**: v0.1.0
**维护团队**: SIAE开发团队