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
| GET | `/permissions` | 获取权限列表 | `auth:permission:query` |
| GET | `/permissions/{permissionId}` | 获取权限详情 | `auth:permission:query` |

### 角色管理 (RoleController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/roles` | 创建角色 | `auth:role:add` |
| PUT | `/roles/{roleId}` | 更新角色 | `auth:role:edit` |
| DELETE | `/roles/{roleId}` | 删除角色 | `auth:role:delete` |
| GET | `/roles` | 获取所有角色 | `auth:role:query` |
| GET | `/roles/{roleId}` | 获取指定角色 | `auth:role:query` |
| POST | `/roles/{roleId}/permissions` | 分配权限 | `auth:role:edit` |

### 用户角色管理 (UserRoleController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/user-role/{userId}/roles` | 为用户分配角色 | `auth:user:role:assign` |

### 用户权限管理 (UserPermissionController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| GET | `/user-permission/list/{userId}` | 查询用户权限 | `auth:user:permission:query` |
| GET | `/user-permission/ids/{userId}` | 查询用户权限ID列表 | `auth:user:permission:query` |
| POST | `/user-permission/assign` | 分配用户权限 | `auth:user:permission:assign` |
| DELETE | `/user-permission/remove/all/{userId}` | 移除用户所有权限 | `auth:user:permission:remove` |
| DELETE | `/user-permission/remove` | 移除用户指定权限 | `auth:user:permission:remove` |
| GET | `/user-permission/check` | 检查用户是否拥有指定权限 | `auth:user:permission:query` |

### 日志管理 (LogController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| GET | `/logs/login` | 获取登录日志 | `auth:log:query` |
| GET | `/logs/login/fail` | 获取登录失败日志 | `auth:log:query` |

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
| POST | `/award-types/page` | 分页查询奖项类型列表 | `user:award-type:list` |

### 奖项等级管理 (AwardLevelController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/award-levels` | 创建奖项等级 | `user:award-level:create` |
| PUT | `/award-levels` | 更新奖项等级 | `user:award-level:update` |
| DELETE | `/award-levels/{id}` | 删除奖项等级 | `user:award-level:delete` |
| GET | `/award-levels/{id}` | 查询奖项等级详情 | `user:award-level:view` |
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
| PUT | `/{contentId}` | 编辑内容 | `content:edit` |
| DELETE | `/{contentId}` | 删除内容 | `content:delete` |
| GET | `/query/{contentId}` | 查询内容详情 | `content:query` |
| GET | `/` | 查询内容列表 | `content:list:view` |
| GET | `/hot` | 查询热门内容 | `content:hot:view` |

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
| PUT | `/categories` | 编辑分类 | `content:category:edit` |
| DELETE | `/categories` | 删除分类 | `content:category:delete` |
| GET | `/categories` | 查询分类列表 | `content:category:view` |

### 审核管理 (AuditsController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| PUT | `/audits/{id}` | 处理审核 | `content:audit:handle` |
| GET | `/audits` | 获取审核记录 | `content:audit:view` |

### 评论管理 (CommentsController)

| HTTP方法 | API路径 | 接口描述 | 权限要求 |
|----------|---------|----------|----------|
| POST | `/comments/{contentId}` | 创建评论 | `content:comment:create` |
| POST | `/comments/page` | 分页查询评论 | `content:comment:view` |

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
| **认证服务 (siae-auth)** | 6 | 18 | 14 | 4 |
| **用户服务 (siae-user)** | 8 | 40 | 40 | 0 |
| **内容服务 (siae-content)** | 7 | 25 | 19 | 6 |
| **消息服务 (siae-message)** | 1 | 2 | 0 | 2 |
| **总计** | **22** | **85** | **73** | **12** |

### 按HTTP方法统计

| HTTP方法 | 接口数量 | 占比 |
|----------|----------|------|
| GET | 35 | 41.2% |
| POST | 32 | 37.6% |
| PUT | 10 | 11.8% |
| DELETE | 8 | 9.4% |

### 权限分类统计

| 权限类型 | 接口数量 | 说明 |
|----------|----------|------|
| 系统管理权限 | 18 | 认证、角色、权限、日志管理 |
| 用户管理权限 | 40 | 用户信息、成员、班级、奖项管理 |
| 内容管理权限 | 19 | 内容发布、标签、分类、审核、评论 |
| 公开接口 | 12 | 无需权限验证的接口 |

---

## 📝 使用说明

### 1. 接口访问方式

#### 通过网关访问 (推荐)
```
http://localhost:8080/api/v1/{service}/{endpoint}
```

#### 直接访问服务
```
认证服务: http://localhost:8000/api/v1/auth/{endpoint}
用户服务: http://localhost:8020/api/v1/user/{endpoint}
内容服务: http://localhost:8010/api/v1/content/{endpoint}
消息服务: http://localhost:8030/api/v1/message/{endpoint}
```

### 2. 权限验证

- **需要认证**: 请求头中需要包含有效的JWT Token
- **权限要求**: 用户必须拥有对应的权限才能访问
- **公开接口**: 无需任何认证即可访问

### 3. API文档访问

每个服务都提供独立的Swagger UI文档：

- 认证服务: http://localhost:8000/api/v1/auth/swagger-ui.html
- 用户服务: http://localhost:8020/api/v1/user/swagger-ui.html
- 内容服务: http://localhost:8010/api/v1/content/swagger-ui.html
- 消息服务: http://localhost:8030/api/v1/message/swagger-ui.html

### 4. 统一网关文档

通过网关访问所有服务的API文档：
- 网关聚合文档: http://localhost:8080/swagger-ui.html

---

## 🔄 更新日志

| 日期 | 版本 | 更新内容 |
|------|------|----------|
| 2024-01-01 | v1.0.0 | 初始版本，包含所有微服务API接口清单 |
| 2024-01-01 | v1.1.0 | 完善权限注解，统一路径映射规范 |
| 2024-01-01 | v1.2.0 | 添加接口统计和使用说明 |

---

**最后更新**: 2024-01-01
**文档版本**: v1.2.0
**维护团队**: SIAE开发团队