# SIAE 项目 API 接口详细文档

## 📋 目录

- [1. 文档概述](#1-文档概述)
- [2. 通用规范](#2-通用规范)
- [3. 认证服务 API](#3-认证服务-api)
- [4. 用户服务 API](#4-用户服务-api)
- [5. 内容服务 API](#5-内容服务-api)
- [6. 消息服务 API](#6-消息服务-api)
- [7. 错误处理](#7-错误处理)

---

## 1. 文档概述

### 1.1 项目信息
- **项目名称**: SIAE (Student Innovation and Entrepreneurship Association)
- **架构**: Spring Cloud 微服务架构
- **认证方式**: JWT Token
- **数据格式**: JSON
- **字符编码**: UTF-8

### 1.2 服务列表
| 服务名称 | 端口 | 上下文路径 | 主要功能 |
|----------|------|------------|----------|
| 认证服务 | 8000 | `/api/v1/auth` | 用户认证、权限管理、角色管理 |
| 用户服务 | 8020 | `/api/v1/user` | 用户信息、成员管理、班级管理 |
| 内容服务 | 8010 | `/api/v1/content` | 内容发布、分类标签、审核管理 |
| 消息服务 | 8030 | `/api/v1/message` | 邮件通知、验证码发送 |

---

## 2. 通用规范

### 2.1 统一响应格式

所有API接口都使用统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 具体的业务数据
  }
}
```

**字段说明**:
- `code`: 状态码，200表示成功，其他表示失败
- `message`: 响应消息
- `data`: 业务数据，可以是对象、数组或null

### 2.2 分页响应格式

分页查询接口使用统一的分页响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "records": [
      // 数据列表
    ]
  }
}
```

**PageVO字段说明**:
- `total`: 总记录数
- `pageNum`: 当前页码（从1开始）
- `pageSize`: 每页记录数
- `records`: 当前页数据列表

### 2.3 分页请求格式

分页查询请求使用统一的分页参数：

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    // 查询条件
  },
  "keyword": "搜索关键词"
}
```

**PageDTO字段说明**:
- `pageNum`: 页码，必填，最小值1
- `pageSize`: 每页条数，必填，最小值1
- `params`: 查询条件对象，可选
- `keyword`: 关键词搜索，可选

### 2.4 JWT认证

#### 请求头格式
```
Authorization: Bearer <access_token>
```

#### Token结构
```json
{
  "userId": 1,
  "username": "admin",
  "authorities": ["auth:user:create", "auth:role:view"],
  "exp": 1640995200,
  "iat": 1640908800
}
```

### 2.5 HTTP状态码

| 状态码 | 说明 | 使用场景 |
|--------|------|----------|
| 200 | 成功 | 请求成功处理 |
| 400 | 参数错误 | 请求参数校验失败 |
| 401 | 未授权 | 未登录或Token过期 |
| 403 | 权限不足 | 没有访问权限 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 500 | 服务器错误 | 系统内部错误 |

---

## 3. 认证服务 API

**基础URL**: `http://localhost:8000/api/v1/auth`

### 3.1 用户登录

**接口**: `POST /login`  
**权限**: 无需认证（公开接口）  
**描述**: 用户登录获取访问令牌

#### 请求参数

**LoginDTO**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| username | String | 是 | 3-20字符，只能包含字母、数字、下划线 | 用户名 |
| password | String | 是 | 6-20字符 | 密码 |

#### 响应示例

**成功响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "admin",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200
  }
}
```

**LoginVO字段说明**:
- `userId`: 用户ID
- `username`: 用户名
- `accessToken`: 访问令牌
- `refreshToken`: 刷新令牌
- `tokenType`: 令牌类型，固定为"Bearer"
- `expiresIn`: 令牌过期时间（秒）

**错误响应**:
```json
{
  "code": 2003,
  "message": "密码错误",
  "data": null
}
```

### 3.2 用户注册

**接口**: `POST /register`  
**权限**: 无需认证（公开接口）  
**描述**: 用户注册新账户

#### 请求参数

**RegisterDTO**:
```json
{
  "username": "newuser",
  "password": "123456",
  "confirmPassword": "123456",
  "email": "user@example.com",
  "phone": "13800138000",
  "nickname": "新用户",
  "realName": "张三",
  "status": 1
}
```

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| username | String | 是 | 3-20字符，只能包含字母、数字、下划线 | 用户名 |
| password | String | 是 | 6-20字符 | 密码 |
| confirmPassword | String | 是 | 必须与password一致 | 确认密码 |
| email | String | 否 | 邮箱格式 | 邮箱地址 |
| phone | String | 否 | 手机号格式 | 手机号码 |
| nickname | String | 否 | - | 昵称 |
| realName | String | 否 | - | 真实姓名 |
| status | Integer | 否 | 默认1 | 状态：1启用，0禁用 |

#### 响应示例

**成功响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 2,
    "username": "newuser",
    "message": "注册成功，请登录"
  }
}
```

### 3.3 刷新令牌

**接口**: `POST /refresh-token`  
**权限**: 无需认证（公开接口）  
**描述**: 使用刷新令牌获取新的访问令牌

#### 请求参数

**TokenRefreshDTO**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 响应示例

**成功响应**:
```json
{
  "code": 200,
  "message": "令牌刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200
  }
}
```

### 3.4 用户登出

**接口**: `POST /logout`  
**权限**: 需要认证  
**描述**: 用户登出，使当前令牌失效

#### 请求头
```
Authorization: Bearer <access_token>
```

#### 响应示例

**成功响应**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": true
}
```

### 3.5 权限管理

#### 3.5.1 创建权限

**接口**: `POST /permissions`
**权限**: `auth:permission:create`
**描述**: 创建新的权限

**请求参数 - PermissionCreateDTO**:
```json
{
  "name": "用户创建",
  "code": "auth:user:create",
  "parentId": 1,
  "type": 1,
  "description": "创建用户的权限"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 权限名称 |
| code | String | 是 | 权限编码，唯一 |
| parentId | Long | 否 | 父权限ID |
| type | Integer | 是 | 权限类型：0菜单，1按钮 |
| description | String | 否 | 权限描述 |

**成功响应**:
```json
{
  "code": 200,
  "message": "权限创建成功",
  "data": {
    "id": 10,
    "name": "用户创建",
    "code": "auth:user:create",
    "parentId": 1,
    "type": 1,
    "description": "创建用户的权限",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00"
  }
}
```

#### 3.5.2 查询权限树

**接口**: `GET /permissions/tree`
**权限**: `auth:permission:view`
**描述**: 获取权限树形结构

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "name": "系统管理",
      "code": "system",
      "parentId": null,
      "type": 0,
      "children": [
        {
          "id": 2,
          "name": "用户管理",
          "code": "system:user",
          "parentId": 1,
          "type": 0,
          "children": [
            {
              "id": 3,
              "name": "用户创建",
              "code": "auth:user:create",
              "parentId": 2,
              "type": 1,
              "children": []
            }
          ]
        }
      ]
    }
  ]
}
```

### 3.6 角色管理

#### 3.6.1 创建角色

**接口**: `POST /roles`
**权限**: `auth:role:create`
**描述**: 创建新的角色

**请求参数 - RoleCreateDTO**:
```json
{
  "name": "内容管理员",
  "code": "content_admin",
  "description": "负责内容审核和管理",
  "permissionIds": [1, 2, 3, 4]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 角色名称 |
| code | String | 是 | 角色编码，唯一 |
| description | String | 否 | 角色描述 |
| permissionIds | Long[] | 否 | 权限ID列表 |

**成功响应**:
```json
{
  "code": 200,
  "message": "角色创建成功",
  "data": {
    "id": 5,
    "name": "内容管理员",
    "code": "content_admin",
    "description": "负责内容审核和管理",
    "status": 1,
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00"
  }
}
```

#### 3.6.2 分页查询角色

**接口**: `POST /roles/page`
**权限**: `auth:role:view`
**描述**: 分页查询角色列表

**请求参数**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    "name": "管理员",
    "code": "admin",
    "status": 1,
    "createdAtStart": "2024-01-01 00:00:00",
    "createdAtEnd": "2024-12-31 23:59:59"
  }
}
```

**RoleQueryDTO字段说明**:
- `name`: 角色名称（模糊查询）
- `code`: 角色编码（模糊查询）
- `status`: 状态：0禁用，1启用
- `createdAtStart`: 创建时间范围开始
- `createdAtEnd`: 创建时间范围结束

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 5,
    "pageNum": 1,
    "pageSize": 10,
    "records": [
      {
        "id": 1,
        "name": "超级管理员",
        "code": "ROLE_ROOT",
        "description": "系统超级管理员",
        "status": 1,
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ]
  }
}
```

---

## 4. 用户服务 API

**基础URL**: `http://localhost:8020/api/v1/user`

### 4.1 用户管理

#### 4.1.1 创建用户

**接口**: `POST /create`
**权限**: `user:create`
**描述**: 创建新用户（一体化创建，包含基本信息和详情信息）

**请求参数 - UserCreateDTO**:
```json
{
  "username": "newuser",
  "password": "123456",
  "status": 1,
  "email": "user@example.com",
  "phone": "13800138000",
  "nickname": "新用户",
  "realName": "张三",
  "avatar": "https://example.com/avatar.jpg",
  "bio": "这是个人简介",
  "gender": 1,
  "birthday": "1995-06-15",
  "idCard": "110101199506151234",
  "qq": "123456789",
  "wechat": "wechat123",
  "bgUrl": "https://example.com/bg.jpg",
  "classId": 1,
  "memberType": 1,
  "joinDate": "2024-01-01"
}
```

**UserCreateDTO字段说明**:

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| username | String | 是 | 3-20字符，字母数字下划线 | 用户名 |
| password | String | 是 | 6-20字符 | 密码 |
| status | Integer | 否 | 默认1 | 状态：1启用，0禁用 |
| email | String | 否 | 邮箱格式 | 邮箱地址 |
| phone | String | 否 | 手机号格式 | 手机号码 |
| nickname | String | 否 | - | 昵称 |
| realName | String | 否 | - | 真实姓名 |
| avatar | String | 否 | - | 头像URL |
| bio | String | 否 | - | 个人简介 |
| gender | Integer | 否 | 0未知，1男，2女 | 性别 |
| birthday | String | 否 | 日期格式 | 出生日期 |
| idCard | String | 否 | - | 身份证号 |
| qq | String | 否 | - | QQ号 |
| wechat | String | 否 | - | 微信号 |
| bgUrl | String | 否 | - | 背景图URL |
| classId | Long | 否 | - | 班级ID |
| memberType | Integer | 否 | 0非会员，1预备，2正式 | 成员类型 |
| joinDate | String | 否 | 日期格式 | 入会日期 |

**成功响应**:
```json
{
  "code": 200,
  "message": "用户创建成功",
  "data": {
    "id": 10,
    "username": "newuser",
    "nickname": "新用户",
    "email": "user@example.com",
    "phone": "13800138000",
    "realName": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "status": 1,
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00",
    "bio": "这是个人简介",
    "gender": 1,
    "genderName": "男",
    "birthday": "1995-06-15",
    "idCard": "110101199506151234",
    "qq": "123456789",
    "wechat": "wechat123",
    "bgUrl": "https://example.com/bg.jpg",
    "classId": 1,
    "className": "计算机科学与技术2021级1班",
    "memberType": 1,
    "memberTypeName": "预备成员",
    "joinDate": "2024-01-01"
  }
}
```

#### 4.1.2 分页查询用户

**接口**: `POST /page`
**权限**: `user:view`
**描述**: 分页查询用户列表

**请求参数**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com",
    "phone": "138",
    "realName": "张",
    "status": 1,
    "classId": 1,
    "memberType": 1,
    "createdAtStart": "2024-01-01 00:00:00",
    "createdAtEnd": "2024-12-31 23:59:59"
  }
}
```

**UserQueryDTO字段说明**:
- `username`: 用户名（模糊查询）
- `nickname`: 昵称（模糊查询）
- `email`: 邮箱（模糊查询）
- `phone`: 手机号（模糊查询）
- `realName`: 真实姓名（模糊查询）
- `status`: 状态筛选
- `classId`: 班级ID筛选
- `memberType`: 成员类型筛选
- `createdAtStart`: 创建时间范围开始
- `createdAtEnd`: 创建时间范围结束

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 10,
    "records": [
      {
        "id": 1,
        "username": "admin",
        "nickname": "管理员",
        "email": "admin@example.com",
        "phone": "13800138000",
        "realName": "管理员",
        "avatar": "https://example.com/avatar.jpg",
        "status": 1,
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ]
  }
}
```

### 4.2 班级管理

#### 4.2.1 创建班级

**接口**: `POST /classes`
**权限**: `user:class:create`
**描述**: 创建新的班级信息

**请求参数 - ClassInfoCreateDTO**:
```json
{
  "collegeId": 1,
  "majorId": 1,
  "year": 2024,
  "classNo": 1
}
```

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| collegeId | Long | 是 | - | 学院ID |
| majorId | Long | 是 | - | 专业ID |
| year | Integer | 是 | 最小值2000 | 入学年份 |
| classNo | Integer | 是 | 最小值1 | 班号 |

**成功响应**:
```json
{
  "code": 200,
  "message": "班级创建成功",
  "data": {
    "id": 10,
    "collegeId": 1,
    "collegeName": "计算机学院",
    "majorId": 1,
    "majorName": "计算机科学与技术",
    "year": 2024,
    "classNo": 1,
    "className": "计算机科学与技术2024级1班",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00"
  }
}
```

---

## 5. 内容服务 API

**基础URL**: `http://localhost:8010/api/v1/content`

### 5.1 内容管理

#### 5.1.1 发布内容

**接口**: `POST /`
**权限**: `content:publish`
**描述**: 发布新的内容（支持文章、笔记、提问、文件、视频等类型）

**请求参数 - ContentCreateDTO**:
```json
{
  "title": "Spring Boot 入门教程",
  "type": 0,
  "description": "详细介绍Spring Boot的基础知识和实战应用",
  "categoryId": 1,
  "tagIds": [1, 2, 3],
  "content": "这里是文章的详细内容...",
  "coverUrl": "https://example.com/cover.jpg",
  "status": 1
}
```

**ContentCreateDTO字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 内容标题 |
| type | Integer | 是 | 内容类型：0文章，1笔记，2提问，3文件，4视频 |
| description | String | 否 | 内容摘要 |
| categoryId | Long | 否 | 分类ID |
| tagIds | Long[] | 否 | 标签ID列表 |
| content | String | 是 | 内容详情 |
| coverUrl | String | 否 | 封面图片URL |
| status | Integer | 否 | 状态：0草稿，1待审核，2已发布 |

**成功响应**:
```json
{
  "code": 200,
  "message": "内容发布成功",
  "data": {
    "id": 100,
    "title": "Spring Boot 入门教程",
    "type": 0,
    "typeName": "文章",
    "description": "详细介绍Spring Boot的基础知识和实战应用",
    "categoryId": 1,
    "categoryName": "技术分享",
    "uploadedBy": 1,
    "authorName": "张三",
    "status": 1,
    "statusName": "待审核",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00",
    "viewCount": 0,
    "likeCount": 0,
    "commentCount": 0
  }
}
```

#### 5.1.2 查询内容详情

**接口**: `GET /query/{contentId}`
**权限**: `content:query`
**描述**: 根据内容ID获取内容详细信息

**路径参数**:
- `contentId`: 内容ID（必填）

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 100,
    "title": "Spring Boot 入门教程",
    "type": 0,
    "typeName": "文章",
    "description": "详细介绍Spring Boot的基础知识和实战应用",
    "categoryId": 1,
    "categoryName": "技术分享",
    "uploadedBy": 1,
    "authorName": "张三",
    "status": 2,
    "statusName": "已发布",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00",
    "content": "这里是文章的详细内容...",
    "coverUrl": "https://example.com/cover.jpg",
    "tags": [
      {
        "id": 1,
        "name": "Spring Boot",
        "description": "Spring Boot相关"
      }
    ],
    "statistics": {
      "viewCount": 150,
      "likeCount": 25,
      "favoriteCount": 10,
      "commentCount": 8
    }
  }
}
```

### 5.2 分类管理

#### 5.2.1 创建分类

**接口**: `POST /categories`
**权限**: `content:category:create`
**描述**: 创建新的内容分类

**请求参数 - CategoryCreateDTO**:
```json
{
  "name": "技术分享",
  "code": "tech_share",
  "parentId": 1
}
```

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| name | String | 是 | 最大50字符 | 分类名称 |
| code | String | 是 | 最大50字符 | 分类编码，唯一 |
| parentId | Long | 否 | - | 父分类ID |

**成功响应**:
```json
{
  "code": 200,
  "message": "分类创建成功",
  "data": {
    "id": 10,
    "name": "技术分享",
    "code": "tech_share",
    "parentId": 1,
    "status": "ENABLED",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00"
  }
}
```

#### 5.2.2 分页查询分类

**接口**: `GET /categories`
**权限**: `content:category:view`
**描述**: 分页查询分类列表

**请求参数**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    "keyword": "技术",
    "name": "技术分享",
    "code": "tech",
    "status": "ENABLED",
    "parentId": 1,
    "createdAtStart": "2024-01-01 00:00:00",
    "createdAtEnd": "2024-12-31 23:59:59"
  }
}
```

**CategoryQueryDTO字段说明**:
- `keyword`: 关键词搜索（搜索名称或编码）
- `name`: 分类名称（精确匹配）
- `code`: 分类编码（精确匹配）
- `status`: 分类状态（ENABLED/DISABLED/DELETED）
- `parentId`: 父分类ID
- `createdAtStart`: 创建时间范围开始
- `createdAtEnd`: 创建时间范围结束

**成功响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 20,
    "pageNum": 1,
    "pageSize": 10,
    "records": [
      {
        "id": 1,
        "name": "技术分享",
        "code": "tech_share",
        "parentId": null,
        "status": "ENABLED",
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ]
  }
}
```

### 5.3 标签管理

#### 5.3.1 创建标签

**接口**: `POST /tags`
**权限**: `content:tag:create`
**描述**: 创建新的内容标签

**请求参数 - TagCreateDTO**:
```json
{
  "name": "Spring Boot",
  "description": "Spring Boot框架相关内容"
}
```

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| name | String | 是 | 最大50字符 | 标签名称 |
| description | String | 是 | 最大200字符 | 标签描述 |

**成功响应**:
```json
{
  "code": 200,
  "message": "标签创建成功",
  "data": {
    "id": 10,
    "name": "Spring Boot",
    "description": "Spring Boot框架相关内容",
    "createTime": "2025-01-01T10:00:00",
    "updateTime": "2025-01-01T10:00:00"
  }
}
```

---

## 6. 消息服务 API

**基础URL**: `http://localhost:8030/api/v1/message`

### 6.1 邮件服务

#### 6.1.1 发送验证码

**接口**: `POST /email/code/send`
**权限**: 无需认证（公开接口）
**描述**: 发送邮件验证码

**请求参数 - EmailCodeSendDTO**:
```json
{
  "email": "user@example.com",
  "type": "register"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | String | 是 | 邮箱地址 |
| type | String | 是 | 验证码类型：register注册，reset重置密码 |

**成功响应**:
```json
{
  "code": 200,
  "message": "验证码发送成功",
  "data": {
    "email": "user@example.com",
    "expiresIn": 300,
    "message": "验证码已发送到您的邮箱，5分钟内有效"
  }
}
```

#### 6.1.2 验证邮件验证码

**接口**: `POST /email/code/verify`
**权限**: 无需认证（公开接口）
**描述**: 验证邮件验证码

**请求参数 - EmailCodeVerifyDTO**:
```json
{
  "email": "user@example.com",
  "code": "123456",
  "type": "register"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | String | 是 | 邮箱地址 |
| code | String | 是 | 验证码 |
| type | String | 是 | 验证码类型 |

**成功响应**:
```json
{
  "code": 200,
  "message": "验证码验证成功",
  "data": true
}
```

**错误响应**:
```json
{
  "code": 2104,
  "message": "验证码错误",
  "data": false
}
```

---

## 7. 错误处理

### 7.1 错误码分类

#### 7.1.1 通用错误码 (200, 400-500)

| 错误码 | 错误信息 | 说明 |
|--------|----------|------|
| 200 | 操作成功 | 请求成功 |
| 400 | 参数校验失败 | 请求参数不符合要求 |
| 401 | 未授权或登录过期 | 需要登录或Token过期 |
| 403 | 无访问权限 | 权限不足 |
| 404 | 资源未找到 | 请求的资源不存在 |
| 500 | 操作失败 | 系统内部错误 |

#### 7.1.2 认证模块错误码 (2001-2299)

| 错误码 | 错误信息 | 说明 |
|--------|----------|------|
| 2001 | 登录失败 | 登录过程中发生错误 |
| 2002 | 用户不存在 | 用户名不存在 |
| 2003 | 密码错误 | 密码不正确 |
| 2004 | 用户已禁用 | 账号被禁用 |
| 2006 | 令牌无效 | JWT Token无效 |
| 2007 | 令牌已过期 | JWT Token过期 |
| 2101 | 用户名已被使用 | 注册时用户名重复 |
| 2102 | 邮箱已被使用 | 注册时邮箱重复 |
| 2104 | 验证码错误 | 邮件验证码错误 |
| 2105 | 验证码已过期 | 邮件验证码过期 |
| 2201 | 角色不存在 | 角色ID不存在 |
| 2202 | 权限不存在 | 权限ID不存在 |

#### 7.1.3 用户模块错误码 (1001-1999)

| 错误码 | 错误信息 | 说明 |
|--------|----------|------|
| 1001 | 用户不存在 | 用户ID不存在 |
| 1002 | 用户已存在 | 用户信息重复 |
| 1003 | 用户名已被使用 | 用户名重复 |
| 1004 | 邮箱已被使用 | 邮箱重复 |
| 1005 | 手机号已被使用 | 手机号重复 |
| 1300 | 班级不存在 | 班级ID不存在 |
| 1301 | 班级已存在 | 班级信息重复 |
| 1400 | 获奖记录不存在 | 奖项记录不存在 |
| 1401 | 奖项类型不存在 | 奖项类型ID不存在 |
| 1403 | 奖项等级不存在 | 奖项等级ID不存在 |

### 7.2 错误响应格式

所有错误响应都遵循统一格式：

```json
{
  "code": 错误码,
  "message": "错误描述",
  "data": null
}
```

### 7.3 参数校验错误

当请求参数校验失败时，返回详细的校验错误信息：

```json
{
  "code": 400,
  "message": "参数校验失败",
  "data": {
    "username": "用户名不能为空",
    "password": "密码长度必须在6-20个字符之间",
    "email": "邮箱格式不正确"
  }
}
```

### 7.4 权限错误

当用户权限不足时的错误响应：

```json
{
  "code": 403,
  "message": "权限不足",
  "data": {
    "requiredPermission": "auth:user:create",
    "userPermissions": ["auth:user:view", "auth:role:view"]
  }
}
```

### 7.5 Token错误

当JWT Token无效或过期时的错误响应：

```json
{
  "code": 2007,
  "message": "令牌已过期",
  "data": {
    "expiredAt": "2025-01-01T10:00:00",
    "currentTime": "2025-01-01T12:00:00"
  }
}
```

---

## 8. 附录

### 8.1 枚举值说明

#### 8.1.1 内容类型 (ContentTypeEnum)
- `0`: 文章
- `1`: 笔记
- `2`: 提问
- `3`: 文件
- `4`: 视频

#### 8.1.2 内容状态 (ContentStatusEnum)
- `0`: 草稿
- `1`: 待审核
- `2`: 已发布
- `3`: 已删除

#### 8.1.3 分类状态 (CategoryStatusEnum)
- `ENABLED`: 启用
- `DISABLED`: 禁用
- `DELETED`: 已删除

#### 8.1.4 用户状态
- `0`: 禁用
- `1`: 启用

#### 8.1.5 性别
- `0`: 未知
- `1`: 男
- `2`: 女

#### 8.1.6 成员类型
- `0`: 非协会成员
- `1`: 预备成员
- `2`: 正式成员

### 8.2 时间格式说明

- **日期时间格式**: `yyyy-MM-dd HH:mm:ss` (如: 2025-01-01 10:30:00)
- **日期格式**: `yyyy-MM-dd` (如: 2025-01-01)
- **时区**: 使用系统本地时区

### 8.3 文件上传说明

文件上传相关接口需要使用 `multipart/form-data` 格式：

```
Content-Type: multipart/form-data
```

支持的文件类型：
- 图片: jpg, jpeg, png, gif (最大5MB)
- 文档: pdf, doc, docx, txt (最大10MB)
- 视频: mp4, avi, mov (最大100MB)

---

**文档版本**: v1.0.0
**最后更新**: 2025-01-01
**维护团队**: SIAE开发团队

> 本文档基于SIAE项目Controller-List.md生成，用于指导前端开发和API调用。如有疑问，请参考各服务的Swagger文档或联系开发团队。
