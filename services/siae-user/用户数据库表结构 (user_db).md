# **用户数据库表结构 (user\_db)**

### **1\. 核心用户表**

#### **1.1. user \- 用户主表**

* **描述**: 存储用户最核心的登录认证信息。

| 字段名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Comment) |
| :---- | :---- | :---- | :---- |
| id | BIGINT UNSIGNED | **PK**, AUTO\_INCREMENT | 主键，自增 |
| username | VARCHAR(64) | NOT NULL, **UNIQUE** | 登录名/用户名 |
| password | VARCHAR(255) | NOT NULL | 加密密码 (请使用BCrypt) |
| status | TINYINT | DEFAULT 1 | 状态：0禁用，1启用 |
| avatar | VARCHAR(512) | NULLABLE | 头像URL |
| is\_deleted | TINYINT | DEFAULT 0 | 是否逻辑删除：0否，1是 |
| created\_at | DATETIME | DEFAULT CURRENT\_TIMESTAMP | 创建时间 |
| updated\_at | DATETIME | ON UPDATE CURRENT\_TIMESTAMP | 更新时间 |

#### **1.2. user\_profile \- 用户详情表**

* **描述**: 存储用户的扩展信息，与 user 表一对一关联。

| 字段名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Comment) |
| :---- | :---- | :---- | :---- |
| user\_id | BIGINT UNSIGNED | **PK**, **FK** \-\> user(id) | 外键，关联user表 |
| nickname | VARCHAR(64) | NULLABLE | 昵称 |
| real\_name | VARCHAR(64) | NULLABLE | 真实姓名 |
| bio | TEXT | NULLABLE | 个人简介 |
| bg\_url | VARCHAR(512) | NULLABLE | 主页背景图片URL |
| email | VARCHAR(128) | NULLABLE, INDEX | 邮箱 |
| phone | VARCHAR(20) | NULLABLE, INDEX | 手机 |
| qq | VARCHAR(20) | NULLABLE | QQ号 (用于联系) |
| wechat | VARCHAR(64) | NULLABLE | 微信号 (用于联系) |
| id\_card | VARCHAR(18) | NULLABLE | 身份证号 |
| gender | TINYINT | DEFAULT 0 | 性别：0未知，1男，2女 |
| birthday | DATE | NULLABLE | 出生日期 |
| created\_at | DATETIME | DEFAULT CURRENT\_TIMESTAMP | 创建时间 |
| updated\_at | DATETIME | ON UPDATE CURRENT\_TIMESTAMP | 更新时间 |

### **2\. 数据字典表 (Lookup Tables)**

#### **2.1. college \- 学院字典表**

| 字段名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Comment) |
| :---- | :---- | :---- | :---- |
| id | BIGINT UNSIGNED | **PK**, AUTO\_INCREMENT |  |
| name | VARCHAR(64) | NOT NULL, **UNIQUE** | 学院名称 |
| code | VARCHAR(32) | NOT NULL, **UNIQUE** | 学院编码 |

#### **2.2. major \- 专业字典表**

| 字段名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Comment) |
| :---- | :---- | :---- | :---- |
| id | BIGINT UNSIGNED | **PK**, AUTO\_INCREMENT |  |
| college\_id | BIGINT UNSIGNED | NOT NULL, **FK** \-\> college(id) | 所属学院ID |
| name | VARCHAR(64) | NOT NULL, **UNIQUE** | 专业名称 |
| code | VARCHAR(32) | NOT NULL, **UNIQUE** | 专业编码 |
| abbr | VARCHAR(16) | NULLABLE | 专业简称 |

#### **2.3. department \- 部门字典表**

| 字段名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Comment) |
| :---- | :---- | :---- | :---- |
| id | BIGINT UNSIGNED | **PK**, AUTO\_INCREMENT |  |
| name | VARCHAR(64) | NOT NULL, **UNIQUE** | 部门名称 |

#### **2.4. position \- 职位字典表**

| 字段名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Comment) |
| :---- | :---- | :---- | :---- |
| id | BIGINT UNSIGNED | **PK**, AUTO\_INCREMENT |  |
| name | VARCHAR(64) | NOT NULL, **UNIQUE** | 职位名称 |

#### **2.5. award\_level \- 奖项等级字典表**

| 字段名 (Column) | 类型 (Type)       | 约束 (Constraints)        | 描述 (Comment) |
| ------------ | --------------- | ----------------------- | ------------ |
| id           | BIGINT UNSIGNED | **PK**, AUTO\_INCREMENT |              |
| name         | VARCHAR(64)     | NOT NULL, **UNIQUE**    | 奖项等级名称       |
| order\_id    | INT             | NOT NULL, DEFAULT 0     | 排序权重，数字越小优先  |

#### **2.6. award\_type \- 奖项类型字典表**

| 字段名 (Column) | 类型 (Type)       | 约束 (Constraints)        | 描述 (Comment) |
| ------------ | --------------- | ----------------------- | ------------ |
| id           | BIGINT UNSIGNED | **PK**, AUTO\_INCREMENT |              |
| name         | VARCHAR(64)     | NOT NULL, **UNIQUE**    | 奖项类型名称       |
| order\_id    | INT             | NOT NULL, DEFAULT 0     | 排序权重，数字越小优先  |

### **3\. 核心业务表**

#### **3.1. class \- 班级表**

| 字段名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Comment) |
| :---- | :---- | :---- | :---- |
| id | BIGINT | **PK**, AUTO\_INCREMENT | 班级ID |
| college\_id | BIGINT UNSIGNED | NOT NULL, **FK** \-\> college(id) | 关联学院ID |
| major\_id | BIGINT UNSIGNED | NOT NULL, **FK** \-\> major(id) | 关联专业ID |
| year | INT | NOT NULL | 入学年份 |
| class\_no | INT | NOT NULL | 班号 |
| is\_deleted | TINYINT | DEFAULT 0 | 是否删除：0否，1是 |
| created\_at | DATETIME | DEFAULT CURRENT\_TIMESTAMP | 创建时间 |
| updated\_at | DATETIME | ON UPDATE CURRENT\_TIMESTAMP | 更新时间 |

#### **3.2. member \- 成员表**

| 字段名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Comment) |
| :---- | :---- | :---- | :---- |
| id | BIGINT | **PK**, AUTO\_INCREMENT | 成员ID |
| user\_id | BIGINT UNSIGNED | NOT NULL, **UNIQUE**, **FK** \-\> user(id) | 关联用户ID |
| student\_id | VARCHAR(32) | NOT NULL, **UNIQUE** | 学号 |
| department\_id | BIGINT UNSIGNED | NOT NULL, **FK** \-\> department(id) | 关联部门ID |
| position\_id | BIGINT UNSIGNED | NOT NULL, **FK** \-\> position(id) | 关联职位ID |
| join\_date | DATE | NOT NULL | 加入日期 |
| status | TINYINT | DEFAULT 1 | 状态：1在校，2离校，3毕业 |
| is\_deleted | TINYINT | DEFAULT 0 | 是否删除：0否，1是 |
| created\_at | DATETIME | DEFAULT CURRENT\_TIMESTAMP | 创建时间 |
| updated\_at | DATETIME | ON UPDATE CURRENT\_TIMESTAMP | 更新时间 |

#### **3.3. member\_candidate \- 候选成员表**

| 字段名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Comment) |
| :---- | :---- | :---- | :---- |
| id | BIGINT | **PK**, AUTO\_INCREMENT | 候选成员ID |
| user\_id | BIGINT UNSIGNED | NOT NULL, **UNIQUE**, **FK** \-\> user(id) | 关联用户ID |
| student\_id | VARCHAR(32) | NOT NULL | 学号 |
| department\_id | BIGINT UNSIGNED | NOT NULL, **FK** \-\> department(id) | 关联意向部门ID |
| status | TINYINT | DEFAULT 0 | 状态：0待审核，1通过，2拒绝 |
| is\_deleted | TINYINT | DEFAULT 0 | 是否删除：0否，1是 |
| created\_at | DATETIME | DEFAULT CURRENT\_TIMESTAMP | 创建时间 |
| updated\_at | DATETIME | ON UPDATE CURRENT\_TIMESTAMP | 更新时间 |

#### **3.4. user\_award \- 用户获奖记录表**

| 字段名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Comment) |
| :---- | :---- | :---- | :---- |
| id | BIGINT UNSIGNED | **PK**, AUTO\_INCREMENT | 主键，自增 |
| user\_id | BIGINT UNSIGNED | NOT NULL, **FK** \-\> user(id) | 用户ID |
| award\_title | VARCHAR(255) | NOT NULL | 奖项名称 |
| award\_level\_id | BIGINT UNSIGNED | NOT NULL, **FK** \-\> award\_level(id) | 关联奖项等级ID |
| award\_type\_id | BIGINT UNSIGNED | NOT NULL, **FK** \-\> award\_type(id) | 关联奖项类型ID |
| awarded\_by | VARCHAR(255) | NOT NULL | 颁发单位 |
| awarded\_at | DATE | NOT NULL | 获奖时间 |
| description | TEXT | NULLABLE | 获奖描述（选填） |
| certificate\_url | VARCHAR(512) | NULLABLE | 奖状或证明材料的URL |
| team\_members | TEXT | NULLABLE | 团队成员信息 |
| is\_deleted | TINYINT | DEFAULT 0 | 是否删除：0否，1是 |
| created\_at | DATETIME | DEFAULT CURRENT\_TIMESTAMP | 记录创建时间 |
| updated\_at | DATETIME | ON UPDATE CURRENT\_TIMESTAMP | 记录更新时间 |

#### **3.5. class\_user \- 班级与用户关联表**

| 字段名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Comment) |
| :---- | :---- | :---- | :---- |
| id | BIGINT | **PK**, AUTO\_INCREMENT | 主键ID |
| class\_id | BIGINT | NOT NULL, **FK** \-\> class(id) | 班级ID |
| user\_id | BIGINT UNSIGNED | NOT NULL, **FK** \-\> user(id) | 用户ID |
| status | TINYINT | DEFAULT 1 | 状态：1在读，2转班，3毕业 |
| is\_deleted | TINYINT | DEFAULT 0 | 是否删除：0否，1是 |
| created\_at | DATETIME | DEFAULT CURRENT\_TIMESTAMP | 创建时间 |
| updated\_at | DATETIME | ON UPDATE CURRENT\_TIMESTAMP | 更新时间 |

### **4\. 新增：认证与集成表 (Auth & Integration Tables)**

#### **4.1. user\_third\_party\_auth \- 第三方认证表**

* **描述**: 存储用户与第三方平台（微信、QQ、GitHub等）的绑定关系。这个表是实现社交登录和绑定的核心。

| 字段名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Comment) |
| :---- | :---- | :---- | :---- |
| id | BIGINT UNSIGNED | **PK**, AUTO\_INCREMENT | 主键 |
| user\_id | BIGINT UNSIGNED | NOT NULL, **FK** \-\> user(id) | 关联到我们系统内的用户ID |
| provider | VARCHAR(50) | NOT NULL | 第三方平台名称 (如: 'wechat', 'qq') |
| provider\_user\_id | VARCHAR(255) | NOT NULL | 用户在第三方平台的唯一ID (如 openid) |
| nickname\_on\_provider | VARCHAR(255) | NULLABLE | 用户在第三方平台的昵称 (冗余) |
| avatar\_on\_provider | VARCHAR(512) | NULLABLE | 用户在第三方平台的头像URL (冗余) |
| access\_token | VARCHAR(512) | NULLABLE | 第三方平台的访问令牌 (加密存储) |
| created\_at | DATETIME | DEFAULT CURRENT\_TIMESTAMP | 绑定时间 |
| updated\_at | DATETIME | ON UPDATE CURRENT\_TIMESTAMP | 更新时间 |
| **UNIQUE** |  | provider, provider\_user\_id | 确保同一个用户在一个平台只能被绑定一次 |

