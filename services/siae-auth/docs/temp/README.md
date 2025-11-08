# SIAE 认证服务 (siae-auth)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.2.0-brightgreen.svg)](https://spring.io/projects/spring-security)
[![Redis](https://img.shields.io/badge/Redis-7.0+-red.svg)](https://redis.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-0.12.3-orange.svg)](https://github.com/jwtk/jjwt)

## 📋 目录

- [项目概述](#项目概述)
- [技术架构](#技术架构)
- [功能特性](#功能特性)
- [项目结构](#项目结构)
- [数据库设计](#数据库设计)
- [API接口文档](#api接口文档)
- [配置说明](#配置说明)
- [部署指南](#部署指南)
- [使用示例](#使用示例)
- [常见问题](#常见问题)

## 🎯 项目概述

SIAE认证服务是软件协会官网系统的核心认证授权中心，负责用户身份验证、权限管理、角色管理等功能。基于Spring Boot 3.x和Spring Security 6.x构建，采用JWT令牌机制和Redis缓存优化，提供高性能、高可用的认证授权服务。

### 核心职责

- **用户认证**: 用户登录、注册、令牌刷新、登出
- **权限管理**: 基于RBAC模型的权限控制系统
- **角色管理**: 角色的增删改查和权限分配
- **令牌管理**: JWT令牌生成、验证和缓存优化
- **审计日志**: 登录日志记录和查询

### 服务信息

- **服务名称**: siae-auth
- **服务端口**: 8000
- **数据库**: auth_db
- **上下文路径**: /api/v1/auth

## 🏗️ 技术架构

### 核心技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | 应用框架 |
| Spring Security | 6.2.0 | 安全框架 |
| Spring Cloud | 2023.0.0 | 微服务框架 |
| MyBatis Plus | 3.5.4 | ORM框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 缓存数据库 |
| JWT | 0.12.3 | 令牌技术 |
| Nacos | 2.3.0 | 配置中心/注册中心 |

### 架构设计

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Gateway       │    │   Auth Service   │    │   Other Services│
│                 │    │                  │    │                 │
│ JWT验证          │───▶│ 用户认证          │───▶│ 业务服务        │
│ 路由转发         │    │ 权限管理           │    │                 │
│                 │    │ Redis缓存         │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌──────────────────┐
                       │   Database       │
                       │                  │
                       │ auth_db (MySQL)  │
                       │ Redis Cache      │
                       └──────────────────┘
```

## ✨ 功能特性

### 🔐 JWT认证优化

- **轻量化Token**: JWT只包含基本信息(userId, username, exp)，大小减少70-80%
- **Redis权限缓存**: 权限信息存储在Redis中，实现毫秒级查询
- **优雅降级**: Redis不可用时自动回退到传统模式
- **实时权限更新**: 权限变更无需重新登录即可生效

### 🛡️ 安全特性

- **多层安全防护**: Spring Security + JWT + Redis
- **权限细粒度控制**: 支持菜单级和按钮级权限控制
- **登录审计**: 完整的登录日志记录
- **令牌管理**: 支持令牌刷新和主动失效

### 🚀 性能优化

- **Redis缓存**: 权限信息缓存，减少数据库查询
- **连接池优化**: Druid连接池配置
- **异步日志**: 异步记录登录日志，不影响主流程

## 📁 项目结构

```
services/siae-auth/
├── src/main/java/com/hngy/siae/auth/
│   ├── SiaeAuthApplication.java          # 启动类
│   ├── config/                           # 配置类
│   │   └── SecurityConfig.java           # Spring Security配置
│   ├── controller/                       # 控制器层
│   │   ├── AuthController.java           # 认证控制器
│   │   ├── PermissionController.java     # 权限管理控制器
│   │   ├── RoleController.java           # 角色管理控制器
│   │   ├── UserRoleController.java       # 用户角色关联控制器
│   │   ├── UserPermissionController.java # 用户权限关联控制器
│   │   └── LogController.java            # 日志查询控制器
│   ├── service/                          # 服务层
│   │   ├── AuthService.java              # 认证服务接口
│   │   ├── PermissionService.java        # 权限服务接口
│   │   ├── RoleService.java              # 角色服务接口
│   │   ├── UserPermissionService.java    # 用户权限服务接口
│   │   ├── UserRoleService.java          # 用户角色服务接口
│   │   ├── LogService.java               # 日志服务接口
│   │   └── impl/                         # 服务实现类
│   ├── mapper/                           # 数据访问层
│   │   ├── PermissionMapper.java         # 权限数据访问
│   │   ├── RoleMapper.java               # 角色数据访问
│   │   ├── UserAuthMapper.java           # 用户认证数据访问
│   │   ├── UserRoleMapper.java           # 用户角色关联数据访问
│   │   ├── UserPermissionMapper.java     # 用户权限关联数据访问
│   │   ├── RolePermissionMapper.java     # 角色权限关联数据访问
│   │   └── LoginLogMapper.java           # 登录日志数据访问
│   ├── entity/                           # 实体类
│   │   ├── Permission.java               # 权限实体
│   │   ├── Role.java                     # 角色实体
│   │   ├── UserAuth.java                 # 用户认证实体
│   │   ├── UserRole.java                 # 用户角色关联实体
│   │   ├── UserPermission.java           # 用户权限关联实体
│   │   ├── RolePermission.java           # 角色权限关联实体
│   │   └── LoginLog.java                 # 登录日志实体
│   ├── dto/                              # 数据传输对象
│   │   ├── request/                      # 请求DTO
│   │   │   ├── LoginDTO.java             # 登录请求
│   │   │   ├── TokenRefreshDTO.java      # 令牌刷新请求
│   │   │   ├── PermissionCreateDTO.java  # 权限创建请求
│   │   │   ├── RoleCreateDTO.java        # 角色创建请求
│   │   │   ├── RoleUpdateDTO.java        # 角色更新请求
│   │   │   ├── RolePermissionDTO.java    # 角色权限分配请求
│   │   │   ├── UserRoleDTO.java          # 用户角色分配请求
│   │   │   └── UserPermissionDTO.java    # 用户权限分配请求
│   │   └── response/                     # 响应DTO
│   │       ├── LoginVO.java              # 登录响应
│   │       ├── TokenRefreshVO.java       # 令牌刷新响应
│   │       ├── PermissionVO.java         # 权限响应
│   │       ├── RoleVO.java               # 角色响应
│   │       ├── UserPermissionVO.java     # 用户权限响应
│   │       ├── LoginLogVO.java           # 登录日志响应
│   │       └── LoginFailVO.java          # 登录失败响应
│   ├── feign/                            # Feign客户端
│   │   ├── UserClient.java               # 用户服务客户端
│   │   └── dto/                          # Feign传输对象
│   └── filter/                           # 过滤器
│       └── ServiceCallFeignInterceptor.java # 服务调用拦截器
├── src/main/resources/
│   ├── application-dev.yaml              # 开发环境配置
│   ├── bootstrap.yaml                    # 启动配置
│   ├── sql/
│   │   └── auth_db.sql                   # 数据库初始化脚本
│   └── mapper/                           # MyBatis映射文件
├── src/test/java/                        # 测试代码
├── pom.xml                               # Maven配置
└── README.md                             # 项目文档
```

## 🗄️ 数据库设计

### 数据库概览

认证服务使用独立的 `auth_db` 数据库，采用MySQL 8.0+，字符集为 `utf8mb4_unicode_ci`。

### 核心数据表

#### 1. role (角色表)

存储系统中的所有角色定义，如超级管理员、普通用户等。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 角色ID |
| name | VARCHAR(64) | NOT NULL | 角色名称 |
| code | VARCHAR(64) | NOT NULL, UNIQUE | 角色编码，用于程序判断 |
| description | VARCHAR(255) | NULL | 角色描述 |
| status | TINYINT | DEFAULT 1 | 状态：0禁用，1启用 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引设计**:
- `uk_code`: 角色编码唯一索引

#### 2. permission (权限表)

存储系统中所有的权限点，通过 parent_id 形成层级结构。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 权限ID |
| parent_id | BIGINT | NULL | 父权限ID，NULL表示顶级菜单 |
| name | VARCHAR(64) | NOT NULL | 权限名称 |
| code | VARCHAR(100) | NOT NULL, UNIQUE | 权限编码，如"sys:user:add" |
| type | VARCHAR(32) | NOT NULL | 权限类型：menu菜单、button按钮 |
| path | VARCHAR(255) | NULL | 路由地址(当type为menu时) |
| component | VARCHAR(255) | NULL | 组件路径(当type为menu时) |
| icon | VARCHAR(64) | NULL | 菜单图标(当type为menu时) |
| sort_order | INT | DEFAULT 0 | 排序值，值越小越靠前 |
| status | TINYINT | DEFAULT 1 | 状态：0禁用，1启用 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引设计**:
- `uk_code`: 权限编码唯一索引
- `idx_parent_id`: 父权限ID索引

#### 3. user_role (用户角色关联表)

存储用户与角色的多对多关系。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | NOT NULL | 用户ID(关联user_db.user.id) |
| role_id | BIGINT | NOT NULL | 角色ID |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引设计**:
- `uk_user_role`: 用户角色唯一索引(user_id, role_id)
- `fk_user_role_role`: 外键约束，关联role表

#### 4. role_permission (角色权限关联表)

存储角色与权限的多对多关系。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| role_id | BIGINT | NOT NULL | 角色ID |
| permission_id | BIGINT | NOT NULL | 权限ID |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引设计**:
- `uk_role_permission`: 角色权限唯一索引(role_id, permission_id)
- `fk_role_permission_role`: 外键约束，关联role表
- `fk_role_permission_permission`: 外键约束，关联permission表

#### 5. user_permission (用户权限关联表)

存储用户与权限的多对多关系，用于直接给用户授予特定权限。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | NOT NULL | 用户ID(关联user_db.user.id) |
| permission_id | BIGINT | NOT NULL | 权限ID |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引设计**:
- `uk_user_permission`: 用户权限唯一索引(user_id, permission_id)
- `fk_user_permission_permission`: 外键约束，关联permission表

#### 6. user_auth (用户认证表)

存储用户的认证令牌信息，用于支持刷新和注销。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 认证ID |
| user_id | BIGINT | NOT NULL | 用户ID |
| access_token | VARCHAR(1024) | NOT NULL | 访问令牌 |
| refresh_token | VARCHAR(1024) | NOT NULL | 刷新令牌 |
| token_type | VARCHAR(32) | DEFAULT 'Bearer' | 令牌类型 |
| expires_at | DATETIME | NOT NULL | 访问令牌过期时间 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引设计**:
- `idx_user_id`: 用户ID索引
- `idx_access_token`: 访问令牌索引(前255字符)
- `idx_refresh_token`: 刷新令牌索引(前255字符)

#### 7. login_log (登录日志表)

记录用户的登录历史。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 访问ID |
| user_id | BIGINT | NULL | 用户ID(登录成功时记录) |
| username | VARCHAR(64) | NOT NULL | 登录账号 |
| login_ip | VARCHAR(64) | DEFAULT '' | 登录IP |
| login_location | VARCHAR(255) | DEFAULT '' | 登录地点 |
| browser | VARCHAR(50) | DEFAULT '' | 浏览器类型 |
| os | VARCHAR(50) | DEFAULT '' | 操作系统 |
| status | TINYINT | DEFAULT 0 | 登录状态(0失败 1成功) |
| msg | VARCHAR(255) | DEFAULT '' | 提示消息 |
| login_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 登录时间 |

**索引设计**:
- `idx_user_id`: 用户ID索引
- `idx_login_time`: 登录时间索引
- `idx_username`: 用户名索引

### 数据库关系图

```
┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
│    role     │    │   role_permission│    │   permission    │
│             │    │                 │    │                 │
│ id (PK)     │◄──┤ role_id (FK)    │    │ id (PK)         │
│ name        │    │ permission_id(FK)├───►│ parent_id       │
│ code (UK)   │    │                 │    │ name            │
│ description │    └─────────────────┘    │ code (UK)       │
│ status      │                           │ type            │
└─────────────┘                           │ path            │
       ▲                                  │ component       │
       │                                  │ icon            │
       │                                  │ sort_order      │
┌─────────────────┐                       │ status          │
│   user_role     │                       └─────────────────┘
│                 │                              ▲
│ user_id         │                              │
│ role_id (FK)    │──────────────────────────────┘
│                 │                              │
└─────────────────┘                       ┌─────────────────┐
                                          │ user_permission │
┌─────────────────┐                       │                 │
│   user_auth     │                       │ user_id         │
│                 │                       │ permission_id(FK)├─┘
│ user_id         │                       │                 │
│ access_token    │                       └─────────────────┘
│ refresh_token   │
│ expires_at      │                       ┌─────────────────┐
└─────────────────┘                       │   login_log     │
                                          │                 │
                                          │ user_id         │
                                          │ username        │
                                          │ login_ip        │
                                          │ browser         │
                                          │ os              │
                                          │ status          │
                                          │ login_time      │
                                          └─────────────────┘
```

### 初始化数据

系统预置了以下角色和权限数据：

**预置角色**:
- `ROLE_ROOT`: 超级管理员，拥有所有权限
- `ROLE_ADMIN`: 管理员，拥有大部分管理权限
- `ROLE_MEMBER`: 协会成员，拥有内容相关权限
- `ROLE_USER`: 普通用户，拥有基础查看权限

**权限模块**:
- **系统管理**: 用户管理、角色管理、权限管理
- **内容管理**: 内容发布、分类管理、标签管理、审核管理
- **用户管理**: 用户信息、成员管理、班级管理、获奖记录

## 📚 API接口文档

### 认证管理接口 (AuthController)

#### 1. 用户登录
- **接口地址**: `POST /login`
- **权限要求**: 无 (公开接口)
- **请求参数**:
```json
{
  "username": "admin",
  "password": "password"
}
```

#### 2. 用户注册
- **接口地址**: `POST /register`
- **权限要求**: 无 (公开接口)

#### 3. 刷新令牌
- **接口地址**: `POST /refresh-token`
- **权限要求**: 无 (公开接口)

#### 4. 用户登出
- **接口地址**: `POST /logout`
- **权限要求**: 需要认证

### 权限管理接口 (PermissionController)

#### 1. 创建权限
- **接口地址**: `POST /permissions`
- **权限要求**: `auth:permission:add`

#### 2. 分页查询权限列表
- **接口地址**: `POST /permissions/page`
- **权限要求**: `auth:permission:query`

#### 3. 查询权限树结构
- **接口地址**: `GET /permissions/tree`
- **权限要求**: `auth:permission:query`

#### 4. 批量更新权限树结构
- **接口地址**: `PUT /permissions/tree/batch`
- **权限要求**: `auth:permission:edit`

#### 5. 获取权限详情
- **接口地址**: `GET /permissions/{permissionId}`
- **权限要求**: `auth:permission:query`

#### 6. 更新权限
- **接口地址**: `PUT /permissions/{permissionId}`
- **权限要求**: `auth:permission:edit`

#### 7. 删除权限
- **接口地址**: `DELETE /permissions/{permissionId}`
- **权限要求**: `auth:permission:delete`

### 角色管理接口 (RoleController)

#### 1. 创建角色
- **接口地址**: `POST /roles`
- **权限要求**: `auth:role:add`

#### 2. 分页查询角色列表
- **接口地址**: `POST /roles/page`
- **权限要求**: `auth:role:query`

#### 3. 获取所有角色
- **接口地址**: `GET /roles`
- **权限要求**: `auth:role:query`

#### 4. 获取角色详情
- **接口地址**: `GET /roles/{roleId}`
- **权限要求**: `auth:role:query`

#### 5. 更新角色
- **接口地址**: `PUT /roles/{roleId}`
- **权限要求**: `auth:role:edit`

#### 6. 删除角色
- **接口地址**: `DELETE /roles/{roleId}`
- **权限要求**: `auth:role:delete`

#### 7. 分配角色权限
- **接口地址**: `POST /roles/{roleId}/permissions`
- **权限要求**: `auth:role:edit`

#### 8. 获取角色权限
- **接口地址**: `GET /roles/{roleId}/permissions`
- **权限要求**: `auth:role:query`

#### 9. 移除角色权限
- **接口地址**: `DELETE /roles/{roleId}/permissions`
- **权限要求**: `auth:role:edit`

### 用户角色管理接口 (UserRoleController)

#### 1. 为用户分配单个角色
- **接口地址**: `POST /users/{userId}/role`
- **权限要求**: `auth:user:role:assign`

#### 2. 批量分配用户角色
- **接口地址**: `POST /users/roles/batch`
- **权限要求**: `auth:user:role:assign`

#### 3. 分页查询用户角色
- **接口地址**: `POST /users/roles/page`
- **权限要求**: `auth:user:role:query`

#### 4. 更新用户角色关联
- **接口地址**: `PUT /users/roles/{userRoleId}`
- **权限要求**: `auth:user:role:update`

### 用户权限管理接口 (UserPermissionController)

#### 1. 分页查询用户权限
- **接口地址**: `GET /user-permission/list/{userId}`
- **权限要求**: `auth:user:permission:query`

#### 2. 分配用户权限（覆盖模式）
- **接口地址**: `POST /user-permission/assign`
- **权限要求**: `auth:user:permission:assign`

#### 3. 追加用户权限（增量模式）
- **接口地址**: `POST /user-permission/append`
- **权限要求**: `auth:user:permission:assign`

#### 4. 移除用户所有权限
- **接口地址**: `DELETE /user-permission/remove/all/{userId}`
- **权限要求**: `auth:user:permission:remove`

#### 5. 移除用户指定权限
- **接口地址**: `DELETE /user-permission/remove`
- **权限要求**: `auth:user:permission:remove`

### 日志查询接口 (LogController)

#### 1. 获取登录日志
- **接口地址**: `POST /logs/login`
- **权限要求**: `auth:log:query`

#### 2. 获取登录失败日志
- **接口地址**: `POST /logs/login/fail`
- **权限要求**: `auth:log:query`

### 通用响应格式

所有接口都遵循统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-01T12:00:00"
}
```

**状态码说明**:
- `200`: 操作成功
- `400`: 请求参数错误
- `401`: 未认证或令牌无效
- `403`: 权限不足
- `404`: 资源不存在
- `500`: 服务器内部错误

### 接口认证

除了登录、注册等公开接口外，其他接口都需要在请求头中携带JWT令牌：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## ⚙️ 配置说明

### 环境依赖

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | Java运行环境 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 缓存数据库 |
| Nacos | 2.3.0+ | 配置中心/注册中心 |
| Maven | 3.8+ | 项目构建工具 |

### 核心配置文件

#### 1. bootstrap.yaml (启动配置)

```yaml
spring:
  application:
    name: siae-auth
  cloud:
    nacos:
      server-addr: localhost:8848
      config:
        server-addr: ${spring.cloud.nacos.server-addr}
        group: SIAE_GROUP
        file-extension: yaml
  config:
    import: nacos:siae-auth.yaml?group=SIAE_GROUP&refresh=true
```

#### 2. application-dev.yaml (开发环境配置)

```yaml
# 服务配置
server:
  port: 8000
  servlet:
    context-path: /api/v1/auth

spring:
  application:
    name: siae-auth

  # Nacos服务发现
  cloud:
    nacos:
      discovery:
        enabled: true

  # 数据库配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/auth_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20

  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      password: # 设置Redis密码(如果有)
      database: 0
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms

  # Jackson配置
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    default-property-inclusion: non_null

# MyBatis Plus配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.hngy.siae.auth.entity

# SpringDoc API文档配置
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  group-configs:
    - group: 'siae-auth'
      paths-to-match: '/api/**'
  packages-to-scan: com.hngy.siae.auth.controller

# 日志配置
logging:
  level:
    com.hngy.siae.auth: DEBUG
    com.hngy.siae.common.filter.OptimizedJwtAuthenticationFilter: DEBUG
    com.hngy.siae.auth.service.impl.RedisPermissionCacheServiceImpl: DEBUG
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n'
```

### JWT配置

JWT相关配置在 `siae-core` 模块的 `JwtUtils` 类中：

```java
// JWT密钥(生产环境请使用更复杂的密钥)
private static final String SECRET_KEY = "your-secret-key-here";

// 访问令牌过期时间(秒) - 2小时
private final long accessTokenExpire = 7200;

// 刷新令牌过期时间(秒) - 7天
private final long refreshTokenExpire = 604800;
```

### Redis缓存配置

权限缓存相关配置：

```java
// Redis键前缀
private static final String PERMISSION_KEY_PREFIX = "auth:perms:";
private static final String ROLE_KEY_PREFIX = "auth:roles:";

// 缓存TTL与JWT过期时间一致
long tokenExpireSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
```

## 🗄️ 数据库设计

### 核心表结构

#### 1. 权限表 (permission)
- **功能**: 存储系统权限信息，支持层级结构
- **特点**: 支持菜单和按钮两种权限类型

| 字段名 | 数据类型 | 主键/索引 | 是否可空 | 默认值 | 说明 |
|--------|----------|-----------|----------|--------|------|
| id | BIGINT | PK | 非空 | 自增 | 权限ID |
| parent_id | BIGINT | IDX | 可空 | NULL | 父权限ID |
| name | VARCHAR(64) | | 非空 | | 权限名称 |
| code | VARCHAR(100) | UK | 非空 | | 权限编码 |
| type | VARCHAR(32) | | 非空 | | 权限类型 |
| status | TINYINT | IDX | 可空 | 1 | 状态 |
| created_at | DATETIME | | 可空 | CURRENT_TIMESTAMP | 创建时间 |

#### 2. 角色表 (role)
- **功能**: 存储系统角色信息

| 字段名 | 数据类型 | 主键/索引 | 是否可空 | 默认值 | 说明 |
|--------|----------|-----------|----------|--------|------|
| id | BIGINT | PK | 非空 | 自增 | 角色ID |
| name | VARCHAR(64) | | 非空 | | 角色名称 |
| code | VARCHAR(100) | UK | 非空 | | 角色编码 |
| status | TINYINT | IDX | 可空 | 1 | 状态 |
| created_at | DATETIME | | 可空 | CURRENT_TIMESTAMP | 创建时间 |

#### 3. 用户认证表 (user_auth)
- **功能**: 存储用户JWT令牌和认证信息

| 字段名 | 数据类型 | 主键/索引 | 是否可空 | 默认值 | 说明 |
|--------|----------|-----------|----------|--------|------|
| id | BIGINT | PK | 非空 | 自增 | 认证ID |
| user_id | BIGINT | UK | 非空 | | 用户ID |
| username | VARCHAR(64) | IDX | 非空 | | 用户名 |
| access_token | TEXT | | 可空 | NULL | 访问令牌 |
| refresh_token | TEXT | | 可空 | NULL | 刷新令牌 |
| expires_at | DATETIME | IDX | 可空 | NULL | 令牌过期时间 |
| created_at | DATETIME | | 可空 | CURRENT_TIMESTAMP | 创建时间 |

#### 4. 关联表结构

**用户角色关联表 (user_role)**:
- user_id + role_id (联合唯一键)
- 支持一个用户拥有多个角色

**角色权限关联表 (role_permission)**:
- role_id + permission_id (联合唯一键)
- 支持一个角色拥有多个权限

**用户权限关联表 (user_permission)**:
- user_id + permission_id (联合唯一键)
- 支持为用户直接分配权限

**登录日志表 (login_log)**:
- 记录用户登录日志，支持安全审计
- 包含客户端信息和登录状态

### RBAC权限模型

系统采用基于角色的访问控制（RBAC）模型：

1. **角色权限**: 用户通过角色获得权限（间接权限）
2. **直接权限**: 直接为用户分配权限（直接权限，优先级更高）
3. **权限继承**: 支持权限的层级结构

**权限计算规则**: `用户最终权限 = 角色权限 ∪ 直接权限`

## 🚀 部署指南

### 本地开发环境部署

#### 1. 环境准备

```bash
# 安装MySQL 8.0+
# 安装Redis 7.0+
# 安装Nacos 2.3.0+
# 安装JDK 17+
# 安装Maven 3.8+
```

#### 2. 数据库初始化

```bash
# 连接MySQL
mysql -u root -p

# 执行初始化脚本
source /path/to/siae-auth/src/main/resources/sql/auth_db.sql
```

#### 3. 启动依赖服务

```bash
# 启动Nacos
cd nacos/bin
./startup.sh -m standalone

# 启动Redis
redis-server

# 启动MySQL
systemctl start mysql
```

#### 4. 配置Nacos

在Nacos控制台中创建配置：
- **Data ID**: `siae-auth.yaml`
- **Group**: `SIAE_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `application-dev.yaml` 的内容

#### 5. 启动服务

```bash
# 克隆项目
git clone <repository-url>
cd siae

# 编译项目
mvn clean compile

# 启动认证服务
cd services/siae-auth
mvn spring-boot:run
```

#### 6. 验证部署

```bash
# 检查服务状态
curl http://localhost:8000/api/v1/auth/actuator/health

# 访问API文档
http://localhost:8000/api/v1/auth/swagger-ui.html

# 测试登录接口
curl -X POST http://localhost:8000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 生产环境部署

#### 1. Docker部署

创建 `Dockerfile`:

```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app

COPY target/siae-auth-*.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
```

构建和运行：

```bash
# 构建镜像
docker build -t siae-auth:latest .

# 运行容器
docker run -d \
  --name siae-auth \
  -p 8000:8000 \
  -e SPRING_PROFILES_ACTIVE=prod \
  siae-auth:latest
```

#### 2. Docker Compose部署

创建 `docker-compose.yml`:

```yaml
version: '3.8'

services:
  siae-auth:
    build: ../..
    ports:
      - "8000:8000"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis
      - nacos
    networks:
      - siae-network

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: your-password
      MYSQL_DATABASE: auth_db
    volumes:
      - mysql-data:/var/lib/mysql
      - ./sql/auth_db.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - siae-network

  redis:
    image: redis:7-alpine
    networks:
      - siae-network

  nacos:
    image: nacos/nacos-server:v2.3.0
    environment:
      MODE: standalone
    ports:
      - "8848:8848"
    networks:
      - siae-network

volumes:
  mysql-data:

networks:
  siae-network:
    driver: bridge
```

启动服务：

```bash
docker-compose up -d
```

#### 3. Kubernetes部署

创建部署配置文件 `k8s-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: siae-auth
spec:
  replicas: 2
  selector:
    matchLabels:
      app: siae-auth
  template:
    metadata:
      labels:
        app: siae-auth
    spec:
      containers:
      - name: siae-auth
        image: siae-auth:latest
        ports:
        - containerPort: 8000
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: NACOS_SERVER_ADDR
          value: "nacos-service:8848"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
---
apiVersion: v1
kind: Service
metadata:
  name: siae-auth-service
spec:
  selector:
    app: siae-auth
  ports:
  - protocol: TCP
    port: 8000
    targetPort: 8000
  type: LoadBalancer
```

部署到Kubernetes：

```bash
kubectl apply -f k8s-deployment.yaml
```

## 💡 使用示例

### 1. 用户认证流程

```java
// 1. 用户登录
@PostMapping("/login")
public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
    // 验证用户凭据
    // 生成JWT令牌
    // 缓存权限到Redis
    // 记录登录日志
    return Result.success(loginVO);
}

// 2. 权限验证
@GetMapping("/protected")
@PreAuthorize("hasAuthority('system:user:query')")
public Result<String> protectedResource() {
    // 业务逻辑
    return Result.success("访问成功");
}

// 3. 获取当前用户信息
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();
Long userId = (Long) auth.getDetails();
Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
```

### 2. 权限管理示例

```java
// 创建权限
PermissionCreateDTO permissionDTO = new PermissionCreateDTO();
permissionDTO.setName("用户查询");
permissionDTO.setCode("system:user:query");
permissionDTO.setType("button");
permissionDTO.setParentId(2L);

PermissionVO permission = permissionService.createPermission(permissionDTO);

// 创建角色
RoleCreateDTO roleDTO = new RoleCreateDTO();
roleDTO.setName("内容编辑");
roleDTO.setCode("ROLE_CONTENT_EDITOR");
roleDTO.setDescription("内容编辑角色");

RoleVO role = roleService.createRole(roleDTO);

// 分配权限给角色
List<Long> permissionIds = Arrays.asList(1L, 2L, 3L);
roleService.assignPermissions(role.getId(), permissionIds);

// 分配角色给用户
UserRoleDTO userRoleDTO = new UserRoleDTO();
userRoleDTO.setUserId(1L);
userRoleDTO.setRoleIds(Arrays.asList(role.getId()));
userRoleService.assignUserRoles(userRoleDTO);
```

### 3. Redis权限缓存使用

```java
// 缓存用户权限
List<String> permissions = Arrays.asList("system:user:query", "system:user:add");
redisPermissionCacheService.cacheUserPermissions(userId, permissions, 7200L, TimeUnit.SECONDS);

// 获取用户权限
List<String> cachedPermissions = redisPermissionCacheService.getUserPermissions(userId);

// 清除用户缓存
redisPermissionCacheService.clearUserCache(userId);
```

### 4. 前端集成示例

```javascript
// 登录请求
const login = async (username, password) => {
  const response = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password })
  });

  const result = await response.json();
  if (result.code === 200) {
    // 保存令牌
    localStorage.setItem('accessToken', result.data.accessToken);
    localStorage.setItem('refreshToken', result.data.refreshToken);
  }
  return result;
};

// 带认证的请求
const authenticatedRequest = async (url, options = {}) => {
  const token = localStorage.getItem('accessToken');

  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${token}`
    }
  });

  if (response.status === 401) {
    // 令牌过期，尝试刷新
    await refreshToken();
    // 重新发起请求
    return authenticatedRequest(url, options);
  }

  return response.json();
};

// 刷新令牌
const refreshToken = async () => {
  const refreshToken = localStorage.getItem('refreshToken');

  const response = await fetch('/api/v1/auth/refresh-token', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ refreshToken })
  });

  const result = await response.json();
  if (result.code === 200) {
    localStorage.setItem('accessToken', result.data.accessToken);
    localStorage.setItem('refreshToken', result.data.refreshToken);
  } else {
    // 刷新失败，跳转到登录页
    window.location.href = '/login';
  }
};
```

## ❓ 常见问题

### 1. JWT令牌相关问题

**Q: JWT令牌过大导致数据库存储失败？**

A: 这个问题已经通过JWT优化解决。新版本的JWT只包含基本信息(userId, username, exp)，权限信息存储在Redis中，大大减少了令牌大小。

**Q: 权限变更后需要重新登录才能生效？**

A: 新版本支持实时权限更新。权限信息存储在Redis中，管理员修改权限后会自动更新缓存，用户无需重新登录。

**Q: 如何手动清除用户的权限缓存？**

A: 可以调用Redis权限缓存服务的清除方法：
```java
redisPermissionCacheService.clearUserCache(userId);
```

### 2. Redis连接问题

**Q: Redis连接失败怎么办？**

A: 系统具有优雅降级机制。当Redis不可用时，会自动回退到传统的JWT认证模式，不会影响系统正常运行。

**Q: 如何监控Redis缓存状态？**

A: 可以通过以下方式监控：
```bash
# 查看权限缓存
redis-cli keys "auth:perms:*"
redis-cli keys "auth:roles:*"

# 查看缓存过期时间
redis-cli ttl "auth:perms:1"
```

### 3. 权限配置问题

**Q: 如何配置细粒度权限？**

A: 系统支持两级权限控制：
- **菜单级权限**: 控制页面访问，type为"menu"
- **按钮级权限**: 控制操作权限，type为"button"

**Q: 如何实现动态权限控制？**

A: 使用Spring Security的@PreAuthorize注解：
```java
@PreAuthorize("hasAuthority('system:user:add')")
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAuthority('content:create') and hasRole('MEMBER')")
```

### 4. 数据库问题

**Q: 数据库连接池配置建议？**

A: 推荐的Druid连接池配置：
```yaml
spring:
  datasource:
    druid:
      initial-size: 5          # 初始连接数
      min-idle: 5              # 最小空闲连接数
      max-active: 20           # 最大活跃连接数
      max-wait: 60000          # 获取连接等待超时时间
      time-between-eviction-runs-millis: 60000  # 检测空闲连接间隔
```

**Q: 如何处理数据库事务？**

A: 在服务层方法上添加@Transactional注解：
```java
@Transactional(rollbackFor = Exception.class)
public void assignUserRoles(UserRoleDTO userRoleDTO) {
    // 业务逻辑
}
```

### 5. 性能优化问题

**Q: 如何提升权限查询性能？**

A: 系统已经实现了多层优化：
- Redis缓存权限信息
- 数据库索引优化
- 连接池配置优化
- 异步日志记录

**Q: 如何监控系统性能？**

A: 可以通过以下方式监控：
- Spring Boot Actuator端点
- 数据库慢查询日志
- Redis监控命令
- 应用日志分析

### 6. 安全问题

**Q: 如何防止JWT令牌被盗用？**

A: 建议的安全措施：
- 使用HTTPS传输
- 设置合理的令牌过期时间
- 实现令牌黑名单机制
- 监控异常登录行为

**Q: 如何实现单点登录(SSO)？**

A: 可以通过以下方式实现：
- 共享JWT密钥
- 统一认证中心
- Redis共享会话
- OAuth2.0集成

### 7. 部署问题

**Q: 如何实现零停机部署？**

A: 推荐使用以下策略：
- 蓝绿部署
- 滚动更新
- 健康检查
- 优雅关闭

**Q: 如何进行服务监控？**

A: 建议的监控方案：
- Prometheus + Grafana
- ELK日志分析
- Spring Boot Admin
- 自定义健康检查

---

## 📞 技术支持

如果在使用过程中遇到问题，可以通过以下方式获取帮助：

- **项目文档**: 查看项目根目录下的相关文档
- **API文档**: 
  - 认证服务: `http://localhost:8000/api/v1/auth/swagger-ui.html`
  - 用户服务: `http://localhost:8010/api/v1/content/swagger-ui.html`
  - 内容服务: `http://localhost:8020/api/v1/user/swagger-ui.html`
- **问题反馈**: 提交Issue到项目仓库
- **技术交流**: 联系项目维护团队

---

**最后更新**: 2024-01-01
**文档版本**: v1.0.0
**项目版本**: 0.0.1-SNAPSHOT