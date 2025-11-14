# Auth服务

## 服务概述

Auth服务负责管理认证、授权、角色、权限以及登录日志等功能。服务基础路径：`/api/v1/auth`

---

## 数据库设计

### 1. 核心业务表（5张）
1. **role** - 角色表
2. **permission** - 权限表
3. **role_permission** - 角色权限关联表
4. **user_role** - 用户角色关联表
5. **user_permission** - 用户权限关联表

### 2. 日志表（2张）
1. **login_log** - 登录日志表
2. **register_log** - 注册日志表

### 设计说明
- **权限体系：** 采用 RBAC（基于角色的访问控制）模型
  - 用户 → 角色 → 权限（标准RBAC）
  - 用户 → 权限（直接授权，用于特殊场景）
- **权限树结构：** 权限支持树形层级结构，通过 `parent_id` 字段实现
- **日志记录：** 记录所有登录和注册操作，包括成功和失败的记录

---

## 接口设计

### 核心业务控制器

#### 一、认证控制器 (AuthController)

**基础路径：** `/api/v1/auth`

##### 1. 用户登录
- **接口：** `POST /api/v1/auth/login`
- **描述：** 用户登录，返回访问令牌和刷新令牌
- **权限：** 无（公开接口）
- **请求体：**
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

##### 2. 用户注册
- **接口：** `POST /api/v1/auth/register`
- **描述：** 用户注册，创建新用户账号
- **权限：** 无（公开接口）
- **请求体：**
```json
{
  "username": "zhangsan",
  "password": "123456",
  "studentId": "2023010101",
  "realName": "张三"
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

##### 3. 刷新访问令牌
- **接口：** `POST /api/v1/auth/refresh-token`
- **描述：** 使用刷新令牌获取新的访问令牌
- **权限：** 无（公开接口）
- **请求体：**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

##### 4. 用户登出
- **接口：** `POST /api/v1/auth/logout`
- **描述：** 用户登出，使当前令牌失效
- **权限：** 需要携带有效token
- **请求头：** `Authorization: Bearer {accessToken}`
- **响应：**
```json
{
  "code": 200,
  "message": "成功登出",
  "data": true
}
```

##### 5. 获取当前用户信息
- **接口：** `GET /api/v1/auth/me`
- **描述：** 获取当前登录用户的基础信息、角色与权限列表
- **权限：** 需要携带有效token
- **请求头：** `Authorization: Bearer {accessToken}`
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "roles": ["ROLE_ADMIN", "ROLE_USER"],
    "permissions": ["user:create", "user:update", "user:delete"]
  }
}
```

---

#### 二、日志控制器 (LogController)

**基础路径：** `/api/v1/auth/logs`

##### 1. 获取登录日志
- **接口：** `POST /api/v1/auth/logs/login`
- **描述：** 分页查询登录日志，支持按用户名、时间范围、登录状态筛选
- **权限：** `auth:log:query`
- **请求体：**
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    "username": "zhang",
    "status": 1,
    "startTime": "2024-01-01T00:00:00",
    "endTime": "2024-12-31T23:59:59"
  }
}
```
- **说明：** `status` 参数：1-成功，0-失败，不传-查询全部
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "zhangsan",
        "status": 1,
        "statusName": "成功",
        "clientIp": "192.168.1.100",
        "browser": "Chrome",
        "os": "Windows 10",
        "loginTime": "2024-11-13T10:00:00"
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

##### 2. 获取登录失败日志
- **接口：** `POST /api/v1/auth/logs/login/fail`
- **描述：** 分页查询登录失败记录，用于安全审计和风险分析
- **权限：** `auth:log:query`
- **请求体：**
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    "username": "zhang",
    "startTime": "2024-01-01T00:00:00",
    "endTime": "2024-12-31T23:59:59"
  }
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "zhangsan",
        "failReason": "密码错误",
        "clientIp": "192.168.1.100",
        "browser": "Chrome",
        "os": "Windows 10",
        "loginTime": "2024-11-13T10:00:00"
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

##### 3. 获取仪表盘统计数据
- **接口：** `GET /api/v1/auth/logs/dashboard/stats/{days}`
- **描述：** 统计指定天数内的登录人数（日活量）和注册人数
- **权限：** `auth:log:query`
- **路径参数：** `days` - 统计天数（7、30、90）
- **示例：** `GET /api/v1/auth/logs/dashboard/stats/7`
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dailyStats": [
      {
        "date": "2024-11-13",
        "loginCount": 150,
        "registerCount": 10
      }
    ],
    "totalLoginCount": 1050,
    "totalRegisterCount": 70
  }
}
```

---

#### 三、权限控制器 (PermissionController)

**基础路径：** `/api/v1/auth/permissions`

##### 1. 创建权限
- **接口：** `POST /api/v1/auth/permissions`
- **描述：** 创建新的权限
- **权限：** `auth:permission:add`
- **请求体：**
```json
{
  "name": "用户查询",
  "code": "user:query",
  "type": 1,
  "parentId": null,
  "path": "/user/query",
  "icon": "user",
  "sortOrder": 1,
  "enabled": true,
  "description": "查询用户信息"
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "用户查询",
    "code": "user:query",
    "type": 1,
    "parentId": null,
    "enabled": true,
    "createAt": "2024-11-13T10:00:00"
  }
}
```

##### 2. 分页查询权限列表
- **接口：** `POST /api/v1/auth/permissions/page`
- **描述：** 支持条件筛选的分页权限查询
- **权限：** `auth:permission:query`
- **请求体：**
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    "name": "用户",
    "code": "user",
    "type": 1,
    "enabled": true
  }
}
```

##### 3. 查询权限树结构
- **接口：** `GET /api/v1/auth/permissions/tree`
- **描述：** 按照层级关系查询权限树形结构
- **权限：** `auth:permission:query`
- **查询参数：** `enabledOnly` - 是否只查询启用状态的权限（默认false）
- **示例：** `GET /api/v1/auth/permissions/tree?enabledOnly=true`
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "用户管理",
      "code": "user",
      "children": [
        {
          "id": 2,
          "name": "用户查询",
          "code": "user:query",
          "children": []
        }
      ]
    }
  ]
}
```

##### 4. 批量更新权限树结构
- **接口：** `PUT /api/v1/auth/permissions/tree`
- **描述：** 支持前端拖拽操作，批量更新权限的层级依赖关系和排序
- **权限：** `auth:permission:edit`
- **请求体：**
```json
[
  {
    "permissionId": 1,
    "parentId": null,
    "sortOrder": 1
  },
  {
    "permissionId": 2,
    "parentId": 1,
    "sortOrder": 1
  }
]
```

##### 5. 获取权限详情
- **接口：** `GET /api/v1/auth/permissions/{permissionId}`
- **描述：** 获取指定ID的权限详细信息
- **权限：** `auth:permission:query`
- **路径参数：** `permissionId` - 权限ID

##### 6. 更新权限
- **接口：** `PUT /api/v1/auth/permissions`
- **描述：** 更新指定权限的信息
- **权限：** `auth:permission:edit`
- **请求体：**
```json
{
  "id": 1,
  "name": "用户查询",
  "code": "user:query",
  "type": 1,
  "parentId": null,
  "enabled": true,
  "description": "更新后的描述"
}
```

##### 7. 删除权限
- **接口：** `DELETE /api/v1/auth/permissions/{permissionId}`
- **描述：** 删除指定ID的权限
- **权限：** `auth:permission:delete`
- **路径参数：** `permissionId` - 权限ID

##### 8. 批量删除权限
- **接口：** `DELETE /api/v1/auth/permissions/batch`
- **描述：** 批量删除指定ID的权限
- **权限：** `auth:permission:delete`
- **请求体：**
```json
[1, 2, 3, 5]
```

---

#### 四、角色控制器 (RoleController)

**基础路径：** `/api/v1/auth/roles`

##### 1. 创建角色
- **接口：** `POST /api/v1/auth/roles`
- **描述：** 创建新的系统角色
- **权限：** `auth:role:add`
- **请求体：**
```json
{
  "name": "管理员",
  "code": "ROLE_ADMIN",
  "description": "系统管理员角色",
  "enabled": true,
  "sortOrder": 1
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "管理员",
    "code": "ROLE_ADMIN",
    "description": "系统管理员角色",
    "enabled": true,
    "createAt": "2024-11-13T10:00:00"
  }
}
```

##### 2. 更新角色
- **接口：** `PUT /api/v1/auth/roles/{roleId}`
- **描述：** 更新指定角色信息
- **权限：** `auth:role:edit`
- **路径参数：** `roleId` - 角色ID
- **请求体：** 同创建角色

##### 3. 删除角色
- **接口：** `DELETE /api/v1/auth/roles/{roleId}`
- **描述：** 删除指定的系统角色
- **权限：** `auth:role:delete`
- **路径参数：** `roleId` - 角色ID

##### 4. 批量删除角色
- **接口：** `DELETE /api/v1/auth/roles/batch`
- **描述：** 批量删除指定ID的角色
- **权限：** `auth:role:delete`
- **请求体：**
```json
[1, 2, 3]
```

##### 5. 分页查询角色列表
- **接口：** `POST /api/v1/auth/roles/page`
- **描述：** 支持条件筛选的分页角色查询
- **权限：** `auth:role:query`
- **请求体：**
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    "name": "管理",
    "code": "ADMIN",
    "enabled": true
  }
}
```

##### 6. 获取所有角色
- **接口：** `GET /api/v1/auth/roles`
- **描述：** 获取系统中所有角色（不分页）
- **权限：** `auth:role:query`
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "管理员",
      "code": "ROLE_ADMIN",
      "enabled": true
    }
  ]
}
```

##### 7. 获取指定角色
- **接口：** `GET /api/v1/auth/roles/{roleId}`
- **描述：** 根据ID获取角色详情
- **权限：** `auth:role:query`
- **路径参数：** `roleId` - 角色ID

##### 8. 获取角色权限列表
- **接口：** `GET /api/v1/auth/roles/{roleId}/permissions`
- **描述：** 获取指定角色的权限列表
- **权限：** `auth:permission:query`
- **路径参数：** `roleId` - 角色ID
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "用户查询",
      "code": "user:query"
    }
  ]
}
```

##### 9. 更新角色权限
- **接口：** `PUT /api/v1/auth/roles/{roleId}/permissions`
- **描述：** 完全替换角色的权限列表
- **权限：** `auth:role:edit`
- **路径参数：** `roleId` - 角色ID
- **请求体：**
```json
{
  "permissionIds": [1, 2, 3, 5]
}
```

##### 10. 追加角色权限
- **接口：** `POST /api/v1/auth/roles/{roleId}/permissions`
- **描述：** 为指定角色追加权限（不会移除现有权限）
- **权限：** `auth:role:edit`
- **路径参数：** `roleId` - 角色ID
- **请求体：**
```json
{
  "permissionIds": [6, 7, 8]
}
```

---

#### 五、用户权限控制器 (UserPermissionController)

**基础路径：** `/api/v1/auth/user-permission`

##### 1. 分页查询用户权限
- **接口：** `GET /api/v1/auth/user-permission/list/{userId}`
- **描述：** 根据用户ID分页查询该用户拥有的权限信息
- **权限：** `auth:user:permission:query`
- **路径参数：** `userId` - 用户ID
- **查询参数：** 分页参数（pageNum, pageSize）
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "permissionId": 1,
        "permissionName": "用户查询",
        "permissionCode": "user:query"
      }
    ],
    "total": 10,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

##### 2. 分配用户权限（覆盖模式）
- **接口：** `POST /api/v1/auth/user-permission/assign`
- **描述：** 为用户分配权限，会先清除原有权限再分配新权限
- **权限：** `auth:user:permission:assign`
- **请求体：**
```json
{
  "userId": 1,
  "permissionIds": [1, 2, 3, 5]
}
```

##### 3. 追加用户权限（增量模式）
- **接口：** `POST /api/v1/auth/user-permission/append`
- **描述：** 为用户追加权限，不会影响原有权限
- **权限：** `auth:user:permission:assign`
- **请求体：**
```json
{
  "userId": 1,
  "permissionIds": [6, 7, 8]
}
```

##### 4. 移除用户所有权限
- **接口：** `DELETE /api/v1/auth/user-permission/remove/all/{userId}`
- **描述：** 清除指定用户的所有权限
- **权限：** `auth:user:permission:remove`
- **路径参数：** `userId` - 用户ID

##### 5. 移除用户指定权限
- **接口：** `DELETE /api/v1/auth/user-permission/remove`
- **描述：** 移除用户的指定权限
- **权限：** `auth:user:permission:remove`
- **请求体：**
```json
{
  "userId": 1,
  "permissionIds": [1, 2]
}
```

---

#### 六、用户角色控制器 (UserRoleController)

**基础路径：** `/api/v1/auth/users`

##### 1. 为用户分配单个角色
- **接口：** `POST /api/v1/auth/users/{userId}/role`
- **描述：** 为指定用户分配一个角色
- **权限：** `auth:user:role:assign`
- **路径参数：** `userId` - 用户ID
- **请求体：**
```json
{
  "roleId": 1
}
```

##### 2. 批量分配角色给用户
- **接口：** `POST /api/v1/auth/users/roles/{roleId}`
- **描述：** 给多个用户批量分配同一个角色
- **权限：** `auth:user:role:assign`
- **路径参数：** `roleId` - 角色ID
- **请求体：**
```json
{
  "userIds": [1, 2, 3, 5]
}
```

##### 3. 获取用户角色关联列表
- **接口：** `POST /api/v1/auth/users/roles/list`
- **描述：** 分页查询用户角色关联关系，支持模糊查询
- **权限：** `auth:user:role:query`
- **请求体：**
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    "userId": 1,
    "roleId": 1,
    "username": "zhang"
  }
}
```

##### 4. 更新用户角色关联
- **接口：** `PUT /api/v1/auth/users/roles/{userRoleId}`
- **描述：** 更新指定用户角色关联记录
- **权限：** `auth:user:role:update`
- **路径参数：** `userRoleId` - 用户角色关联ID
- **请求体：**
```json
{
  "roleId": 2
}
```

---

## 数据模型说明

### 权限类型 (type)
- `1` - 菜单
- `2` - 按钮
- `3` - 接口

### 登录状态 (status)
- `0` - 失败
- `1` - 成功

### 启用状态 (enabled)
- `true` - 启用
- `false` - 禁用

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（未登录或token失效） |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 权限体系说明

### RBAC模型

本服务采用标准的RBAC（基于角色的访问控制）模型：

1. **用户 → 角色 → 权限**（标准RBAC）
   - 用户通过角色获得权限
   - 一个用户可以拥有多个角色
   - 一个角色可以拥有多个权限

2. **用户 → 权限**（直接授权）
   - 用于特殊场景，直接为用户分配权限
   - 不通过角色，适用于临时授权或特殊权限

### 权限判断逻辑

用户的最终权限 = 角色权限 + 直接授权权限

### 权限树结构

- 权限支持树形层级结构
- 通过 `parent_id` 字段实现父子关系
- 支持拖拽调整层级和排序

---

## 认证流程说明

### 标准登录流程

1. **登录** - 用户提供用户名和密码
   - 调用 `POST /api/v1/auth/login`
   - 验证成功后返回 `accessToken` 和 `refreshToken`
   - 记录登录日志

2. **访问受保护资源** - 携带访问令牌
   - 请求头添加：`Authorization: Bearer {accessToken}`
   - 后端验证token有效性和权限
   - 返回相应资源

3. **刷新令牌** - 访问令牌过期前刷新
   - 调用 `POST /api/v1/auth/refresh-token`
   - 使用 `refreshToken` 获取新的 `accessToken`
   - 无需重新登录

4. **登出** - 用户主动登出
   - 调用 `POST /api/v1/auth/logout`
   - 使当前token失效
   - 清理服务端会话

### 注册流程

1. **用户注册** - 填写基础信息
   - 调用 `POST /api/v1/auth/register`
   - Auth服务通过Feign调用User服务创建用户
   - 注册成功后自动登录，返回token

2. **完善信息** - 登录后完善详细信息
   - 调用User服务的更新接口
   - 填写头像、邮箱、电话等详细信息

---

## 通用数据结构说明

### 分页请求 (PageDTO)
```json
{
  "pageNum": 1,           // 页码，从1开始，必填
  "pageSize": 10,         // 每页条数，必填
  "params": {},           // 查询条件对象，可选
  "keyword": ""           // 关键字搜索，可选
}
```

### 分页响应 (PageVO)
```json
{
  "total": 100,           // 总条数
  "pageNum": 1,           // 当前页
  "pageSize": 10,         // 每页条数
  "records": []           // 当前页数据列表
}
```

### 统一响应 (Result)
```json
{
  "code": 200,            // 状态码
  "message": "success",   // 消息
  "data": {}              // 数据
}
```

---

## 接口设计原则

### 分页 vs 列表查询

1. **字典数据（不分页）**
   - 角色列表（通常数量较少）
   - 接口：`GET /api/v1/auth/roles` 返回完整列表
   - 用途：下拉选择、前端缓存

2. **业务数据（必须分页）**
   - 权限列表、日志记录、用户角色关联等
   - 接口：`POST /api/v1/auth/{resource}/page` 返回分页数据
   - 用途：列表展示、数据管理

---

## 注意事项

1. **Token管理**
   - `accessToken` 有效期较短（如1小时），用于访问受保护资源
   - `refreshToken` 有效期较长（如7天），用于刷新访问令牌
   - Token存储建议使用 HttpOnly Cookie 或 LocalStorage

2. **权限验证**
   - 所有需要权限的接口都需要在请求头中携带有效的 JWT Token
   - 使用 `@SiaeAuthorize` 注解进行权限控制
   - 超级管理员（ROLE_ROOT）默认放行所有接口

3. **密码安全**
   - 密码使用 BCrypt 加密存储
   - 前端传输时建议使用 HTTPS
   - 不要在日志中记录密码信息

4. **日志记录**
   - 所有登录和注册操作都会记录日志
   - 包括成功和失败的记录
   - 记录客户端IP、浏览器、操作系统等信息

5. **权限树操作**
   - 删除父权限时需要先删除子权限
   - 拖拽调整层级时注意循环依赖
   - 批量更新权限树时使用事务保证一致性

6. **角色和权限**
   - 角色编码建议使用 `ROLE_` 前缀（如 `ROLE_ADMIN`）
   - 权限编码建议使用 `模块:资源:操作` 格式（如 `user:query`）
   - 删除角色前需要先解除用户关联

7. **分页参数**
   - 使用 `pageNum`（页码）和 `pageSize`（每页条数）
   - 默认每页10条
   - 查询条件放在 `params` 对象中

8. **日期格式**
   - 统一使用 ISO 8601 格式：`yyyy-MM-ddTHH:mm:ss`

---

## 更新日志

### v1.0 (2024-11-13)
- ✅ 实现用户认证功能（登录、注册、登出）
- ✅ 实现Token管理（访问令牌、刷新令牌）
- ✅ 实现RBAC权限体系（角色、权限、用户角色、用户权限）
- ✅ 实现权限树结构管理
- ✅ 实现登录日志记录和查询
- ✅ 实现仪表盘统计功能
- ✅ 完善接口文档和权限配置说明
