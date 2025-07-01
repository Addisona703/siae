# **Auth模块数据库表结构文档**

本文档详细描述了 auth\_db 数据库中所有表的结构、字段定义和用途。

### **1\. role (角色表)**

**描述**：存储系统中的所有角色定义，如超级管理员、普通用户等。

| 字段名 | 数据类型 | 主键/索引 | 是否可空 | 默认值 | 说明 |
| :---- | :---- | :---- | :---- | :---- | :---- |
| id | BIGINT | PK | 非空 | 自增 | 角色ID |
| name | VARCHAR(64) |  | 非空 |  | 角色名称 |
| code | VARCHAR(64) | UK | 非空 |  | 角色编码, 用于程序判断 |
| description | VARCHAR(255) |  | 可空 | NULL | 角色描述 |
| status | TINYINT |  | 可空 | 1 | 状态：0禁用，1启用 |
| created\_at | DATETIME |  | 可空 | CURRENT\_TIMESTAMP | 创建时间 |
| updated\_at | DATETIME |  | 可空 | CURRENT\_TIMESTAMP | 更新时间 |

### **2\. permission (权限表)**

**描述**：存储系统中所有的权限点，如菜单访问、按钮操作等，通过 parent\_id 形成层级结构。

| 字段名 | 数据类型 | 主键/索引 | 是否可空 | 默认值 | 说明 |
| :---- | :---- | :---- | :---- | :---- | :---- |
| id | BIGINT | PK | 非空 | 自增 | 权限ID |
| parent\_id | BIGINT | IDX | 可空 | NULL | 父权限ID, NULL表示顶级菜单 |
| name | VARCHAR(64) |  | 非空 |  | 权限名称 |
| code | VARCHAR(100) | UK | 非空 |  | 权限编码, e.g., "sys:user:add" |
| type | VARCHAR(32) |  | 非空 |  | 权限类型：menu菜单、button按钮 |
| path | VARCHAR(255) |  | 可空 | NULL | 路由地址 (当type为menu时) |
| component | VARCHAR(255) |  | 可空 | NULL | 组件路径 (当type为menu时) |
| icon | VARCHAR(64) |  | 可空 | NULL | 菜单图标 (当type为menu时) |
| sort\_order | INT |  | 可空 | 0 | 排序值, 值越小越靠前 |
| status | TINYINT |  | 可空 | 1 | 状态：0禁用，1启用 |
| created\_at | DATETIME |  | 可空 | CURRENT\_TIMESTAMP | 创建时间 |
| updated\_at | DATETIME |  | 可空 | CURRENT\_TIMESTAMP | 更新时间 |

### **3\. user\_role (用户角色关联表)**

**描述**：存储用户与角色的多对多关系。一个用户可以拥有多个角色。

| 字段名 | 数据类型 | 主键/索引 | 是否可空 | 默认值 | 说明 |
| :---- | :---- | :---- | :---- | :---- | :---- |
| id | BIGINT | PK | 非空 | 自增 | 主键ID |
| user\_id | BIGINT | UK | 非空 |  | 用户ID (关联user\_db.user.id) |
| role\_id | BIGINT | UK, FK | 非空 |  | 角色ID |
| created\_at | DATETIME |  | 可空 | CURRENT\_TIMESTAMP | 创建时间 |

### **4\. role\_permission (角色权限关联表)**

**描述**：存储角色与权限的多对多关系。一个角色可以被授予多个权限。

| 字段名 | 数据类型 | 主键/索引 | 是否可空 | 默认值 | 说明 |
| :---- | :---- | :---- | :---- | :---- | :---- |
| id | BIGINT | PK | 非空 | 自增 | 主键ID |
| role\_id | BIGINT | UK, FK | 非空 |  | 角色ID |
| permission\_id | BIGINT | UK, FK | 非空 |  | 权限ID |
| created\_at | DATETIME |  | 可空 | CURRENT\_TIMESTAMP | 创建时间 |

### **5\. user\_auth (用户认证表)**

**描述**：存储用户的认证令牌信息，用于支持令牌的刷新和主动注销（强制下线）。

| 字段名 | 数据类型 | 主键/索引 | 是否可空 | 默认值 | 说明 |
| :---- | :---- | :---- | :---- | :---- | :---- |
| id | BIGINT | PK | 非空 | 自增 | 认证ID |
| user\_id | BIGINT | IDX | 非空 |  | 用户ID |
| access\_token | VARCHAR(512) | IDX | 非空 |  | 访问令牌 |
| refresh\_token | VARCHAR(512) | IDX | 非空 |  | 刷新令牌 |
| token\_type | VARCHAR(32) |  | 可空 | 'Bearer' | 令牌类型 |
| expires\_at | DATETIME |  | 非空 |  | 访问令牌过期时间 |
| created\_at | DATETIME |  | 可空 | CURRENT\_TIMESTAMP | 创建时间 |
| updated\_at | DATETIME |  | 可空 | CURRENT\_TIMESTAMP | 更新时间 |

### **6\. login\_log (登录日志表)**

**描述**：记录用户的登录历史，包括成功和失败的尝试，用于安全审计和行为分析。

| 字段名 | 数据类型 | 主键/索引 | 是否可空 | 默认值 | 说明 |
| :---- | :---- | :---- | :---- | :---- | :---- |
| id | BIGINT | PK | 非空 | 自增 | 访问ID |
| user\_id | BIGINT | IDX | 可空 | NULL | 用户ID (登录成功时记录) |
| username | VARCHAR(64) | IDX | 非空 |  | 登录账号 |
| login\_ip | VARCHAR(64) |  | 可空 | '' | 登录IP |
| login\_location | VARCHAR(255) |  | 可空 | '' | 登录地点 |
| browser | VARCHAR(50) |  | 可空 | '' | 浏览器类型 |
| os | VARCHAR(50) |  | 可空 | '' | 操作系统 |
| status | TINYINT |  | 可空 | 0 | 登录状态（0失败 1成功） |
| msg | VARCHAR(255) |  | 可空 | '' | 提示消息 |
| login\_time | DATETIME | IDX | 可空 | CURRENT\_TIMESTAMP | 登录时间 |

