# SIAE - 学生创新创业协会智能管理平台

<div align="center">

**基于 Spring Cloud 微服务架构的现代化协会管理系统**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**[特性介绍](#-核心特性) • [快速开始](#-快速开始) • [架构设计](#-架构设计) • [API文档](#-api文档)**

</div>

---

## 📋 目录

- [项目简介](#-项目简介)
- [核心特性](#-核心特性)
- [技术栈](#-技术栈)
- [系统架构](#-系统架构)
- [服务清单](#-服务清单)
- [快速开始](#-快速开始)
- [架构设计](#-架构设计)
- [业务逻辑](#-业务逻辑)
- [API文档](#-api文档)
- [数据库设计](#-数据库设计)
- [开发指南](#-开发指南)
- [部署指南](#-部署指南)
- [贡献指南](#-贡献指南)

---

## 📖 项目简介

**SIAE (Student Innovation and Entrepreneurship Association Platform)** 是一个面向学生创新创业协会的综合智能管理平台，采用 **Spring Cloud 微服务架构**，融合了现代化的技术栈和创新的业务设计。

### 🎯 项目定位

- **功能完善**：覆盖协会运营全流程（用户管理、内容发布、考勤打卡、资源管理）
- **技术先进**：拥抱最新技术栈（Spring Boot 3、Java 17、AI大模型）
- **架构清晰**：模块化设计，服务拆分合理，可扩展性强
- **用户友好**：实时推送、流式响应、分片上传等现代化交互体验
- **智能化**：集成本地大模型（Ollama），打造AI智能助手

### 🌟 项目亮点

1. **AI智能助手** - 基于Ollama本地大模型，支持智能问答、数据查询、工具调用
2. **OAuth三方登录** - 支持QQ/GitHub/Gitee登录，单点登录（SSO）
3. **实时通信** - SSE推送通知、AI流式响应、ChatGPT式打字机效果
4. **SpEL规则引擎** - 灵活的权限控制和考勤规则配置
5. **分片上传** - 支持大文件断点续传，预签名URL前端直传
6. **多模态AI** - 支持图片理解，文字+图片混合对话
7. **完善的考勤系统** - 支持多班次、活动考勤、人脸识别、异常检测

---

## ✨ 核心特性

### 🔐 认证授权
- JWT Token 认证机制
- OAuth 2.0 三方登录（QQ/GitHub/Gitee）
- 单点登录（SSO）
- RBAC权限模型（角色-权限）
- SpEL表达式权限控制
- 操作日志审计

### 👥 用户管理
- 用户信息CRUD
- 成员生命周期管理（待审核→候选→正式）
- 个人简历系统
- 获奖记录管理
- 部门/职位/专业管理
- 统计分析

### 📝 内容管理
- 多类型内容发布（文章/笔记/提问/文件/视频）
- 内容审核流程（策略模式）
- 分类标签管理
- 评论系统
- 收藏功能
- 互动统计（点赞/浏览/分享）
- 热门内容推荐

### 🔔 通知推送
- SSE实时推送
- 邮件通知（FreeMarker模板）
- 广播通知
- 未读数量统计
- RabbitMQ消息队列集成

### 📁 媒体服务
- MinIO对象存储
- 分片上传（断点续传）
- 预签名URL（前端直传）
- 文件元数据管理
- 存储配额管理

### 📅 考勤管理
- 签到签退管理
- 活动考勤
- 请假申请与审核
- 考勤异常检测（迟到/早退/缺勤）
- 多班次管理
- SpEL规则引擎
- 人脸识别打卡
- 统计报表导出

### 🤖 AI智能助手
- Ollama本地大模型集成
- 思考模式（Thinking Mode）
- 多模态能力（图片理解）
- 工具调用（Function Calling）
- 流式响应（SSE）
- 会话持久化
- 10+内置工具函数

---

## 🛠 技术栈

### 核心框架
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.4.1 | 核心框架 |
| Spring Cloud | 2024.0.0 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.2 | 微服务组件 |
| Java | 17 | 开发语言 |

### 服务治理
| 技术 | 说明 |
|------|------|
| Nacos | 服务注册与配置中心 |
| Spring Cloud Gateway | API网关 |
| OpenFeign | 服务间调用 |
| LoadBalancer | 负载均衡 |

### 数据存储
| 技术 | 版本 | 说明 |
|------|------|------|
| MySQL | 8.0+ | 关系型数据库 |
| MyBatis-Plus | 3.5.6 | ORM框架 |
| Druid | 1.2.22 | 数据库连接池 |
| Redis | 6.0+ | 缓存与会话存储 |
| MinIO | latest | 对象存储 |

### 消息队列
| 技术 | 说明 |
|------|------|
| RabbitMQ | 消息中间件 |
| Spring AMQP | 消息队列集成 |

### 安全认证
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Security | 6.x | 安全框架 |
| JJWT | 0.11.5 | JWT Token |
| OAuth 2.0 Client | 内置 | 三方登录 |

### AI能力
| 技术 | 说明 |
|------|------|
| Ollama | 本地大模型引擎 |
| qwen3:8b / gemma3:4b | 支持的模型 |

### 开发工具
| 技术 | 版本 | 说明 |
|------|------|------|
| SpringDoc OpenAPI | 2.5.0 | API文档（Swagger） |
| Lombok | 1.18.30 | 代码简化 |
| Hutool | 5.8.27 | 工具库 |
| Maven | 3.9+ | 项目构建 |

---

## 🏗 系统架构

### 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         前端应用层                               │
│                  (Vue/React + Axios + SSE)                      │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP/HTTPS
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway (Port: 80)                        │
│              Spring Cloud Gateway + JWT Filter                  │
│        路由转发 / 认证鉴权 / 限流熔断 / 日志追踪                   │
└──────┬──────┬──────┬──────┬──────┬──────┬──────┬───────────────┘
       │      │      │      │      │      │      │
       ▼      ▼      ▼      ▼      ▼      ▼      ▼
┌──────────┐┌──────────┐┌──────────┐┌──────────┐┌──────────┐
│ siae-auth││siae-user ││siae-     ││siae-     ││siae-     │
│  :8000   ││  :8020   ││content   ││notifica- ││media     │
│          ││          ││  :8010   ││tion      ││  :8040   │
│ 认证授权  ││ 用户管理  ││ 内容管理  ││  :8030   ││ 媒体服务  │
│ OAuth    ││ 成员管理  ││ 审核评论  ││ 实时推送  ││ 文件上传  │
│ RBAC     ││ 简历管理  ││ 互动统计  ││ 邮件通知  ││ MinIO    │
└──────────┘└──────────┘└──────────┘└──────────┘└──────────┘

┌──────────┐┌──────────┐┌──────────────────────────────────────┐
│siae-     ││siae-ai   ││ siae-resource-management             │
│attendance││  :8060   ││  :xxxx                               │
│  :8050   ││          ││                                      │
│ 考勤管理  ││ AI助手   ││ 资源管理（图书/设备）                  │
│ 请假审批  ││ Ollama   ││ 借还管理                             │
│ 人脸识别  ││ 工具调用  ││                                      │
└──────────┘└──────────┘└──────────────────────────────────────┘
       │            │                    │
       └────────────┼────────────────────┘
                    │
       ┌────────────┴────────────┬──────────────────┐
       ▼                         ▼                  ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────┐
│  Nacos Server   │   │  RabbitMQ       │   │   Redis     │
│  :8848          │   │  :5672          │   │   :6379     │
│  服务注册        │   │  消息队列        │   │   缓存      │
│  配置中心        │   │  事件驱动        │   │   会话      │
└─────────────────┘   └─────────────────┘   └─────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│                        MySQL 8.0+                            │
│  auth_db | user_db | content_db | notification_db |         │
│  media_db | attendance_db | ai_db | resource_management     │
└─────────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│                        MinIO                                 │
│                  对象存储（图片/视频/文件）                     │
└─────────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────┐
│                     Ollama Engine                            │
│               本地大模型（qwen3:8b / gemma3:4b）               │
└─────────────────────────────────────────────────────────────┘
```

### 模块依赖关系

```
siae-parent (根项目)
│
├── packages/ (基础设施包)
│   ├── siae-core (核心工具类、统一响应、异常体系)
│   ├── siae-web-starter (Web通用配置、异常处理、分页)
│   ├── siae-security-starter (认证鉴权、SpEL权限控制)
│   ├── siae-messaging-starter (RabbitMQ集成)
│   └── siae-feign-starter (Feign客户端配置)
│
├── api/ (服务间调用API)
│   ├── api-user (用户服务Feign接口)
│   ├── api-content (内容服务Feign接口)
│   ├── api-media (媒体服务Feign接口)
│   └── api-ai (AI服务Feign接口)
│
├── siae-gateway/ (API网关)
│   └── 路由转发、JWT认证、限流熔断
│
└── services/ (微服务)
    ├── siae-auth (认证服务)
    ├── siae-user (用户服务)
    ├── siae-content (内容服务)
    ├── siae-notification (通知服务)
    ├── siae-media (媒体服务)
    ├── siae-attendance (考勤服务)
    ├── siae-ai (AI服务)
    └── resourceManagement (资源管理)
```

---

## 📦 服务清单

| 服务名称 | 端口 | 上下文路径 | 数据库 | 主要功能 | Swagger文档 |
|---------|------|-----------|--------|---------|------------|
| **siae-gateway** | 80 | / | - | API网关、路由转发 | - |
| **siae-auth** | 8000 | /api/v1/auth | auth_db | 认证授权、OAuth登录、RBAC | [查看文档](http://localhost:8000/api/v1/auth/swagger-ui.html) |
| **siae-content** | 8010 | /api/v1/content | content_db | 内容管理、审核、评论 | [查看文档](http://localhost:8010/api/v1/content/swagger-ui.html) |
| **siae-user** | 8020 | /api/v1/user | user_db | 用户、成员、获奖管理 | [查看文档](http://localhost:8020/api/v1/user/swagger-ui.html) |
| **siae-notification** | 8030 | /api/v1/notification | notification_db | 通知推送、邮件发送 | [查看文档](http://localhost:8030/api/v1/notification/swagger-ui.html) |
| **siae-media** | 8040 | /api/v1/media | media_db | 文件上传、对象存储 | [查看文档](http://localhost:8040/api/v1/media/swagger-ui.html) |
| **siae-attendance** | 8050 | /api/v1/attendance | attendance_db | 考勤、请假、异常检测 | [查看文档](http://localhost:8050/api/v1/attendance/swagger-ui.html) |
| **siae-ai** | 8060 | /api/v1/ai | ai_db | AI对话、工具调用 | [查看文档](http://localhost:8060/api/v1/ai/swagger-ui.html) |
| **resourceManagement** | - | - | resource_management | 资源管理（开发中） | - |

---

## 🚀 快速开始

### 环境要求

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| **JDK** | 17+ | 必需 |
| **Maven** | 3.9+ | 必需 |
| **MySQL** | 8.0+ | 必需 |
| **Redis** | 6.0+ | 必需 |
| **Nacos** | 2.3.0+ | 必需 |
| **RabbitMQ** | 3.12+ | 必需 |
| **MinIO** | latest | 可选（媒体服务需要） |
| **Ollama** | latest | 可选（AI服务需要） |

### 1️⃣ 克隆项目

```bash
git clone https://github.com/your-org/siae.git
cd siae
```

### 2️⃣ 初始化数据库

```bash
# 创建数据库（使用MySQL客户端）
mysql -u root -p

CREATE DATABASE auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE content_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE notification_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE media_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE attendance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE ai_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE resource_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入SQL脚本
USE auth_db;
SOURCE services/siae-auth/src/main/resources/sql/auth_db.sql;

USE user_db;
SOURCE services/siae-user/src/main/resources/sql/user_db.sql;

USE content_db;
SOURCE services/siae-content/src/main/resources/sql/content_db.sql;

USE notification_db;
SOURCE services/siae-notification/src/main/resources/sql/notification_db.sql;

USE media_db;
SOURCE services/siae-media/src/main/resources/sql/media_db.sql;

USE attendance_db;
SOURCE services/siae-attendance/src/main/resources/sql/attendance_db.sql;

USE ai_db;
SOURCE services/siae-ai/src/main/resources/sql/ai_db.sql;
```

### 3️⃣ 启动基础设施

```bash
# 启动Redis
redis-server

# 启动Nacos（下载后）
cd nacos/bin
./startup.sh -m standalone  # Linux/Mac
startup.cmd -m standalone   # Windows

# 访问Nacos控制台: http://localhost:8848/nacos
# 默认账号/密码: nacos/nacos

# 启动RabbitMQ
rabbitmq-server

# 访问RabbitMQ管理界面: http://localhost:15672
# 默认账号/密码: guest/guest

# 启动MinIO（可选）
minio server /data --console-address ":9001"

# 访问MinIO控制台: http://localhost:9001

# 启动Ollama（可选）
ollama serve

# 下载模型
ollama pull qwen3:8b
```

### 4️⃣ 配置Nacos

在Nacos控制台创建以下配置文件（命名空间：`public`，Group：`DEFAULT_GROUP`）：

- `siae-auth-dev.yaml`
- `siae-user-dev.yaml`
- `siae-content-dev.yaml`
- `siae-notification-dev.yaml`
- `siae-media-dev.yaml`
- `siae-attendance-dev.yaml`
- `siae-ai-dev.yaml`

配置内容参考各服务的 `src/main/resources/application-dev.yaml`

### 5️⃣ 构建项目

```bash
# 根目录执行
mvn clean install -DskipTests
```

### 6️⃣ 启动服务

**推荐启动顺序**：

```bash
# 1. 启动网关
cd siae-gateway
mvn spring-boot:run

# 2. 启动认证服务
cd services/siae-auth
mvn spring-boot:run

# 3. 启动用户服务
cd services/siae-user
mvn spring-boot:run

# 4. 启动内容服务
cd services/siae-content
mvn spring-boot:run

# 5. 启动通知服务
cd services/siae-notification
mvn spring-boot:run

# 6. 启动媒体服务
cd services/siae-media
mvn spring-boot:run

# 7. 启动考勤服务
cd services/siae-attendance
mvn spring-boot:run

# 8. 启动AI服务
cd services/siae-ai
mvn spring-boot:run
```

或使用提供的启动脚本：

```bash
# Windows
env-start.bat

# Linux/Mac
chmod +x env-start.sh
./env-start.sh
```

### 7️⃣ 验证启动

访问Swagger文档验证服务启动成功：

- 认证服务：http://localhost:8000/api/v1/auth/swagger-ui.html
- 用户服务：http://localhost:8020/api/v1/user/swagger-ui.html
- 内容服务：http://localhost:8010/api/v1/content/swagger-ui.html
- 通知服务：http://localhost:8030/api/v1/notification/swagger-ui.html
- 媒体服务：http://localhost:8040/api/v1/media/swagger-ui.html
- 考勤服务：http://localhost:8050/api/v1/attendance/swagger-ui.html
- AI服务：http://localhost:8060/api/v1/ai/swagger-ui.html

### 8️⃣ 测试接口

```bash
# 注册用户
curl -X POST http://localhost:80/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com",
    "phoneNumber": "13800138000"
  }'

# 登录获取Token
curl -X POST http://localhost:80/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "testuser",
    "password": "123456"
  }'

# 使用Token访问受保护接口
curl -X GET http://localhost:80/api/v1/user/current \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 🏛 架构设计

### 设计理念

本项目遵循以下设计原则：

1. **单一职责原则**：每个服务专注于特定领域
2. **开闭原则**：易扩展，策略模式、工厂模式广泛应用
3. **依赖倒置原则**：面向接口编程，基础设施包提供抽象
4. **服务自治**：每个服务独立数据库、独立部署
5. **API优先**：Swagger文档完善，契约清晰
6. **领域驱动设计**：按业务领域拆分服务

### 分层架构

每个服务内部采用经典三层架构：

```
┌─────────────────────────────────────────┐
│         Controller Layer (API层)        │
│  - 参数校验                              │
│  - 权限控制 (@SiaeAuthorize)            │
│  - 统一响应封装                          │
│  - Swagger文档                           │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Service Layer (业务层)           │
│  - 业务逻辑                              │
│  - 事务管理                              │
│  - 服务间调用 (Feign)                    │
│  - 缓存控制 (Redis)                      │
│  - 消息发送 (RabbitMQ)                   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Mapper Layer (数据访问层)           │
│  - MyBatis-Plus CRUD                     │
│  - 自定义SQL (XML)                       │
│  - 分页查询                              │
└─────────────────────────────────────────┘
```

### 基础设施包设计

#### 1. siae-core（核心包）

提供最基础的能力，所有服务依赖：

```java
// 统一响应封装
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
}

// 统一异常
public class BusinessException extends RuntimeException {
    private Integer code;
    private String message;
}

// JWT工具
public class JwtUtils {
    public static String generateToken(Long userId);
    public static Claims parseToken(String token);
}
```

#### 2. siae-web-starter（Web启动器）

Web层通用能力：

- **全局异常处理**：`@RestControllerAdvice`
- **统一响应封装**：`@UnifiedResponse`
- **分页工具**：`PageDTO` / `PageVO` 转换
- **Jackson配置**：日期格式化、枚举序列化
- **Swagger自动配置**

#### 3. siae-security-starter（安全启动器）

自定义权限控制框架：

```java
// 权限注解
@SiaeAuthorize("hasRole('ADMIN') and hasAuthority('USER_DELETE')")
public Result<Void> deleteUser(Long id) { ... }

// 支持的SpEL表达式
- isAuthenticated()         // 是否已认证
- hasRole(role)            // 是否拥有角色
- hasAuthority(permission) // 是否拥有权限
- isOwner(resourceId)      // 是否资源所有者
```

**实现原理**：

1. `JwtAuthenticationFilter` 解析JWT，设置 `SecurityContext`
2. `SiaeAuthorizeAspect` AOP拦截 `@SiaeAuthorize` 注解
3. `SiaeSecurityExpressionRoot` 提供SpEL上下文
4. 权限验证失败抛出 `AccessDeniedException`

#### 4. siae-messaging-starter（消息启动器）

RabbitMQ封装，简化消息发送：

```java
@Autowired
private MessageSender messageSender;

// 发送消息
messageSender.send("exchange", "routingKey", message);
```

#### 5. siae-feign-starter（Feign启动器）

服务间调用配置：

- 负载均衡
- 超时重试
- 日志记录
- 降级处理

### 服务间通信

#### 1. 同步调用（Feign）

```java
// API模块定义接口
@FeignClient(name = "siae-user", path = "/api/v1/user")
public interface UserFeignClient {
    @GetMapping("/feign/user/{id}")
    Result<UserVO> getUserById(@PathVariable Long id);
}

// 其他服务调用
@Autowired
private UserFeignClient userFeignClient;

UserVO user = userFeignClient.getUserById(userId).getData();
```

#### 2. 异步通信（RabbitMQ）

```java
// 发送消息
rabbitTemplate.convertAndSend("content.audit", auditEvent);

// 消费消息
@RabbitListener(queues = "notification.content.audit")
public void handleAuditEvent(AuditEvent event) {
    // 发送通知
}
```

### 数据一致性

#### 1. 分布式事务（Seata）

对于强一致性要求的场景，使用Seata保证数据一致性：

```java
@GlobalTransactional
public void createMember(MemberDTO memberDTO) {
    // 1. 创建用户账号（user服务）
    // 2. 创建成员信息（member服务）
    // 3. 分配默认角色（auth服务）
}
```

#### 2. 最终一致性（消息队列）

大部分场景使用消息队列保证最终一致性：

```java
// 内容审核通过后
@Transactional
public void approveContent(Long contentId) {
    // 1. 更新审核状态
    contentMapper.updateAuditStatus(contentId, APPROVED);

    // 2. 发布事件（事务提交后）
    applicationEventPublisher.publishEvent(
        new ContentApprovedEvent(contentId)
    );
}

// 事务监听器
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleContentApproved(ContentApprovedEvent event) {
    // 发送MQ消息通知作者
    rabbitTemplate.convertAndSend("notification.content.approved", event);
}
```

---

## 💼 业务逻辑

### 1. 认证授权流程

#### 登录流程

```
1. 用户提交账号密码
   ↓
2. 验证账号密码
   ↓
3. 加载用户角色权限
   ↓
4. 生成JWT Token (AccessToken + RefreshToken)
   ↓
5. 缓存用户信息到Redis
   ↓
6. 返回Token给客户端
```

#### OAuth三方登录流程

```
1. 前端跳转到OAuth授权页面
   ↓
2. 用户授权后回调到后端
   ↓
3. 后端获取授权码，换取Access Token
   ↓
4. 调用第三方API获取用户信息
   ↓
5. 查询是否已绑定账号
   ├─ 已绑定 → 直接登录
   └─ 未绑定 → 创建临时Token，要求完善信息
      ↓
      完善信息后创建账号 → 绑定OAuth账号 → 登录
```

#### 权限验证流程

```
1. 请求到达Gateway
   ↓
2. Gateway提取JWT Token
   ↓
3. 验证Token有效性（签名、过期时间）
   ↓
4. 将用户ID、角色等信息放入Header转发
   ↓
5. 目标服务JwtAuthenticationFilter解析Header
   ↓
6. 设置SecurityContext
   ↓
7. @SiaeAuthorize AOP拦截
   ↓
8. 评估SpEL表达式
   ├─ 通过 → 执行业务逻辑
   └─ 失败 → 抛出AccessDeniedException
```

### 2. 成员生命周期管理

```
┌───────────┐
│ 注册用户   │ (普通用户，无协会权限)
└─────┬─────┘
      │ 申请加入协会
      ▼
┌───────────┐
│ 待审核     │ (提交申请，等待审批)
└─────┬─────┘
      │ 审核通过
      ▼
┌───────────┐
│ 候选成员   │ (享有部分权限，接受考察)
└─────┬─────┘
      │ 表现良好，管理员转正
      ▼
┌───────────┐
│ 正式成员   │ (完整权限，可参与所有活动)
└───────────┘
```

**关键业务逻辑**：

- **申请审核**：管理员审核申请，可拒绝并说明原因
- **转正流程**：候选成员满足条件后由管理员转正
- **数据清理**：用户删除30天后自动清理数据（RabbitMQ延迟队列）

### 3. 内容审核流程

```
┌─────────────┐
│ 用户发布内容 │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 自动审核     │ (敏感词检测、图片检测)
└──────┬──────┘
       │
       ├─ 通过 → 直接发布
       └─ 未通过/需人工审核 → 进入待审核队列
          ↓
       ┌─────────────┐
       │ 人工审核     │
       └──────┬──────┘
              │
              ├─ 通过 → 发布 + 通知作者
              └─ 拒绝 → 标记拒绝 + 通知作者原因
```

**策略模式实现**：

```java
// 审核策略接口
public interface AuditStrategy {
    AuditResult audit(Content content);
}

// 文章审核策略
@Component("ARTICLE")
public class ArticleAuditStrategy implements AuditStrategy {
    public AuditResult audit(Content content) {
        // 文章特定审核逻辑
    }
}

// 视频审核策略
@Component("VIDEO")
public class VideoAuditStrategy implements AuditStrategy {
    public AuditResult audit(Content content) {
        // 视频特定审核逻辑
    }
}

// 策略上下文
@Service
public class AuditService {
    @Autowired
    private Map<String, AuditStrategy> strategyMap;

    public void audit(Long contentId) {
        Content content = getContent(contentId);
        AuditStrategy strategy = strategyMap.get(content.getType());
        strategy.audit(content);
    }
}
```

### 4. 考勤业务逻辑

#### 签到流程

```
1. 用户请求签到
   ↓
2. 加载考勤规则（Redis缓存）
   ↓
3. SpEL规则引擎验证
   ├─ 时间窗口检查 (#isWithinTimeWindow())
   ├─ 位置验证 (#isWithinLocation())
   └─ 重复检查 (#hasSignedToday())
   ↓
4. 人脸识别（可选）
   ↓
5. 记录签到记录
   ↓
6. 判断考勤状态
   ├─ 正常
   ├─ 迟到
   └─ 早退
   ↓
7. 创建考勤异常（如果需要）
```

#### SpEL规则引擎

自定义SpEL方法，支持灵活的业务规则配置：

```java
// 自定义SpEL方法
public class AttendanceSpelMethods {

    // 是否在时间窗口内
    public static boolean isWithinTimeWindow(LocalTime now,
                                             LocalTime start,
                                             LocalTime end) {
        return !now.isBefore(start) && !now.isAfter(end);
    }

    // 是否在指定位置范围内
    public static boolean isWithinLocation(Double lat1, Double lon1,
                                           Double lat2, Double lon2,
                                           Double radius) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= radius;
    }

    // 是否工作日
    public static boolean isWorkday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY
            && dayOfWeek != DayOfWeek.SUNDAY;
    }
}

// 规则配置示例
{
  "signInRule": "#isWithinTimeWindow(#now, #startTime, #endTime) and #isWithinLocation(#userLat, #userLon, #officeLat, #officeLon, 100)",
  "mustBeWorkday": "#isWorkday(#date)"
}
```

### 5. AI工具调用流程

```
1. 用户发送消息 "最近有哪些优秀成员？"
   ↓
2. AI模型判断需要调用工具 queryMembers
   ↓
3. 后端解析工具调用请求
   {
     "name": "queryMembers",
     "arguments": {
       "membershipType": "FULL",
       "sortBy": "AWARD_COUNT",
       "limit": 10
     }
   }
   ↓
4. 执行工具函数
   - 调用UserFeignClient获取成员数据
   - 格式化为工具结果
   ↓
5. 将工具结果返回给AI
   ↓
6. AI生成最终回复
   "根据查询，以下是最近表现优秀的成员：
    1. 张三 - 获奖5次
    2. 李四 - 获奖3次
    ..."
   ↓
7. 流式返回给用户
```

**内置工具函数**：

| 工具名称 | 功能 | 调用服务 |
|---------|------|---------|
| `queryMembers` | 查询成员信息 | UserFeignClient |
| `getMemberStatistics` | 成员统计 | UserFeignClient |
| `queryMemberAwards` | 获奖记录查询 | UserFeignClient |
| `getAwardStatistics` | 获奖统计 | UserFeignClient |
| `searchContent` | 内容搜索 | ContentFeignClient |
| `getHotContent` | 热门内容 | ContentFeignClient |
| `getLatestContent` | 最新内容 | ContentFeignClient |
| `getWeather` | 天气查询 | 外部API |

### 6. 媒体分片上传流程

```
1. 前端计算文件MD5，请求初始化上传
   POST /api/v1/media/upload/init
   {
     "fileName": "video.mp4",
     "fileSize": 100MB,
     "fileMd5": "xxx",
     "mimeType": "video/mp4"
   }
   ↓
2. 后端判断文件大小
   ├─ <10MB → 普通上传，返回预签名URL
   └─ ≥10MB → 分片上传
      ├─ 查询是否已上传（秒传）
      ├─ 创建MinIO上传任务
      ├─ 生成分片预签名URL（15分钟有效）
      └─ 创建上传会话（Redis，24小时过期）
   ↓
3. 前端根据预签名URL直传MinIO
   ├─ 上传分片1
   ├─ 上传分片2
   └─ ...
   ↓
4. 所有分片上传完成后，请求完成上传
   POST /api/v1/media/upload/complete
   {
     "uploadId": "xxx",
     "parts": [{"partNumber": 1, "etag": "xxx"}, ...]
   }
   ↓
5. 后端异步合并分片
   ├─ MinIO合并分片
   ├─ 保存文件元数据到数据库
   ├─ 删除上传会话
   └─ 更新上传状态
   ↓
6. 前端轮询查询上传状态
   GET /api/v1/media/upload/status/{uploadId}
   ↓
7. 合并完成，返回文件URL
```

**技术特点**：

- **秒传**：MD5查重，已存在文件直接返回
- **断点续传**：上传会话记录已上传分片
- **前端直传**：预签名URL，减轻服务器压力
- **异步处理**：大文件合并异步执行
- **安全控制**：MIME类型白名单、文件大小限制

---

## 📚 API文档

### Swagger文档访问

| 服务 | Swagger UI地址 |
|-----|---------------|
| 认证服务 | http://localhost:8000/api/v1/auth/swagger-ui.html |
| 用户服务 | http://localhost:8020/api/v1/user/swagger-ui.html |
| 内容服务 | http://localhost:8010/api/v1/content/swagger-ui.html |
| 通知服务 | http://localhost:8030/api/v1/notification/swagger-ui.html |
| 媒体服务 | http://localhost:8040/api/v1/media/swagger-ui.html |
| 考勤服务 | http://localhost:8050/api/v1/attendance/swagger-ui.html |
| AI服务 | http://localhost:8060/api/v1/ai/swagger-ui.html |

### 接口规范

#### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

#### 统一错误码

| 错误码 | 说明 |
|-------|-----|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 500 | 服务器错误 |

#### 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

---

## 🗄 数据库设计

### 数据库清单

| 数据库名称 | 服务 | 表数量 | 说明 |
|-----------|------|-------|------|
| auth_db | siae-auth | 7 | 认证授权相关 |
| user_db | siae-user | 10+ | 用户成员相关 |
| content_db | siae-content | 12+ | 内容管理相关 |
| notification_db | siae-notification | 2 | 通知消息相关 |
| media_db | siae-media | 3 | 媒体文件相关 |
| attendance_db | siae-attendance | 6+ | 考勤管理相关 |
| ai_db | siae-ai | 2 | AI会话相关 |
| resource_management | resourceManagement | 待完善 | 资源管理相关 |

### 核心表结构

#### auth_db

```sql
-- 角色表
role (id, name, code, description, created_at, updated_at)

-- 权限表
permission (id, name, code, type, parent_id, sort, created_at, updated_at)

-- 用户角色关联
user_role (id, user_id, role_id, created_at)

-- 角色权限关联
role_permission (id, role_id, permission_id, created_at)

-- OAuth账号绑定
oauth_account (id, user_id, provider, provider_user_id, created_at, updated_at)

-- 登录日志
login_log (id, user_id, ip, location, device, status, created_at)

-- 操作日志
operation_log (id, user_id, module, operation, method, params, result, created_at)
```

#### user_db

```sql
-- 用户表
user (id, username, email, phone_number, avatar, status, created_at, updated_at)

-- 成员信息
membership (id, user_id, student_id, real_name, membership_type, status, join_date, ...)

-- 简历信息
user_resume (id, user_id, education, skills, projects, ...)

-- 获奖记录
user_award (id, user_id, award_type_id, award_level_id, award_date, ...)

-- 部门/职位/专业
department (id, name, code, ...)
position (id, name, code, ...)
major (id, name, code, ...)
```

#### content_db

```sql
-- 内容主表
content (id, user_id, title, type, status, audit_status, created_at, updated_at)

-- 内容详情表（按类型）
content_detail_article (content_id, content, ...)
content_detail_note (content_id, content, ...)
content_detail_question (content_id, content, ...)
content_detail_video (content_id, video_url, ...)

-- 分类/标签
category (id, name, parent_id, ...)
tag (id, name, ...)
content_tag (content_id, tag_id)

-- 评论
comment (id, content_id, user_id, parent_id, content, ...)

-- 收藏
favorite (id, user_id, name, ...)
favorite_item (id, favorite_id, content_id, ...)

-- 互动记录
interaction (id, content_id, user_id, type, created_at)

-- 审核记录
audit_record (id, content_id, auditor_id, status, reason, created_at)
```

#### attendance_db

```sql
-- 考勤记录
attendance_record (id, user_id, type, status, sign_in_time, sign_out_time, ...)

-- 考勤规则
attendance_rule (id, name, rule_expression, ...)

-- 班次配置
shift (id, name, start_time, end_time, ...)

-- 请假申请
leave_request (id, user_id, type, start_date, end_date, status, ...)

-- 考勤异常
attendance_anomaly (id, attendance_id, type, reason, ...)
```

#### ai_db

```sql
-- 会话记录
chat_session (id, user_id, title, model, created_at, updated_at)

-- 消息记录
chat_message (id, session_id, role, content, tokens, created_at)
```

### 数据库设计规范

1. **命名规范**：
   - 表名：小写下划线 (snake_case)
   - 主键：统一使用 `id`
   - 外键：`{table}_id`
   - 时间字段：`created_at`, `updated_at`

2. **通用字段**：
   ```sql
   id BIGINT PRIMARY KEY AUTO_INCREMENT,
   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
   updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   ```

3. **软删除**：
   ```sql
   deleted TINYINT DEFAULT 0,
   deleted_at DATETIME
   ```

4. **索引设计**：
   - 主键索引：`id`
   - 外键索引：`user_id`, `content_id` 等
   - 唯一索引：`username`, `email`, `student_id` 等
   - 复合索引：查询优化

---

## 👨‍💻 开发指南

### 代码结构

```
src/main/java/com/hngy/siae/{service}/
├── controller/          # 控制器层
│   ├── UserController.java
│   └── ...
├── service/            # 服务层
│   ├── UserService.java
│   └── impl/
│       └── UserServiceImpl.java
├── mapper/             # 数据访问层
│   └── UserMapper.java
├── domain/             # 领域模型
│   ├── entity/         # 实体类
│   ├── dto/            # 数据传输对象
│   │   ├── request/
│   │   └── response/
│   └── vo/             # 视图对象
├── config/             # 配置类
├── filter/             # 过滤器
├── listener/           # 监听器
├── util/               # 工具类
└── constant/           # 常量定义

src/main/resources/
├── mapper/             # MyBatis XML
├── sql/                # 数据库脚本
├── application.yaml    # 主配置
└── application-dev.yaml # 开发环境配置
```

### 编码规范

#### 1. 命名规范

- **类名**：大驼峰 `UserController`, `UserService`
- **方法名**：小驼峰 `getUserById`, `createUser`
- **常量**：全大写下划线 `MAX_SIZE`, `DEFAULT_PAGE_SIZE`
- **变量**：小驼峰 `userId`, `userName`

#### 2. 注解规范

**Controller层**：

```java
@Tag(name = "用户管理", description = "用户相关操作")
@RestController
@RequestMapping("/user")
public class UserController {

    @Operation(summary = "获取用户信息", description = "根据ID获取用户详细信息")
    @SiaeAuthorize("hasAuthority('USER_VIEW')")
    @GetMapping("/{id}")
    public Result<UserVO> getUser(
        @Parameter(description = "用户ID", required = true)
        @PathVariable Long id
    ) {
        // ...
    }
}
```

**Service层**：

```java
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

    @Override
    public UserVO getUserById(Long id) {
        // ...
    }
}
```

**Mapper层**：

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
}
```

#### 3. 注释规范

```java
/**
 * 用户服务实现类
 *
 * @author KEYKB
 */
@Service
public class UserServiceImpl implements UserService {

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    public UserVO getUserById(Long id) {
        // ...
    }
}
```

#### 4. 异常处理

```java
// 参数校验
AssertUtils.notNull(userId, "用户ID不能为空");
AssertUtils.isTrue(age > 0, "年龄必须大于0");

// 业务异常
throw new BusinessException(ResultCode.USER_NOT_FOUND);
throw new BusinessException("用户不存在");
```

### 新增功能开发流程

#### 1. 数据库设计

```sql
-- 1. 设计表结构
CREATE TABLE example (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 创建索引
CREATE INDEX idx_name ON example(name);

-- 3. 插入测试数据
INSERT INTO example (name) VALUES ('test');
```

#### 2. 创建实体类

```java
@Data
@TableName("example")
public class Example {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 3. 创建DTO/VO

```java
// 请求DTO
@Data
public class ExampleCreateDTO {
    @NotBlank(message = "名称不能为空")
    private String name;
}

// 响应VO
@Data
public class ExampleVO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
```

#### 4. 创建Mapper

```java
@Mapper
public interface ExampleMapper extends BaseMapper<Example> {
}
```

#### 5. 创建Service

```java
public interface ExampleService {
    ExampleVO create(ExampleCreateDTO dto);
    ExampleVO getById(Long id);
    PageVO<ExampleVO> list(PageDTO pageDTO);
}

@Service
@Transactional
public class ExampleServiceImpl implements ExampleService {

    @Autowired
    private ExampleMapper exampleMapper;

    @Override
    public ExampleVO create(ExampleCreateDTO dto) {
        Example example = BeanUtil.copyProperties(dto, Example.class);
        exampleMapper.insert(example);
        return BeanUtil.copyProperties(example, ExampleVO.class);
    }
}
```

#### 6. 创建Controller

```java
@Tag(name = "示例管理")
@RestController
@RequestMapping("/example")
public class ExampleController {

    @Autowired
    private ExampleService exampleService;

    @Operation(summary = "创建示例")
    @SiaeAuthorize("hasAuthority('EXAMPLE_CREATE')")
    @PostMapping
    public Result<ExampleVO> create(@Valid @RequestBody ExampleCreateDTO dto) {
        return Result.success(exampleService.create(dto));
    }
}
```

#### 7. 权限配置

```java
// 1. 定义权限常量
public interface ExamplePermissions {
    String EXAMPLE_CREATE = "example:create";
    String EXAMPLE_VIEW = "example:view";
}

// 2. 在auth_db中插入权限记录
INSERT INTO permission (name, code, type)
VALUES ('创建示例', 'example:create', 'BUTTON');
```

#### 8. 单元测试

```java
@SpringBootTest
public class ExampleServiceTest {

    @Autowired
    private ExampleService exampleService;

    @Test
    public void testCreate() {
        ExampleCreateDTO dto = new ExampleCreateDTO();
        dto.setName("test");

        ExampleVO vo = exampleService.create(dto);
        assertNotNull(vo.getId());
        assertEquals("test", vo.getName());
    }
}
```

### Git提交规范

```bash
# 格式
<type>(<scope>): <subject>

# type类型
feat:     新功能
fix:      修复bug
docs:     文档更新
style:    代码格式调整（不影响功能）
refactor: 重构
test:     测试相关
chore:    构建工具或依赖更新

# 示例
git commit -m "feat(user): 新增用户简历管理功能"
git commit -m "fix(auth): 修复OAuth登录回调失败问题"
git commit -m "docs(readme): 更新快速开始指南"
```

---

## 🚢 部署指南

### Docker部署

#### 1. 构建镜像

```dockerfile
# Dockerfile
FROM openjdk:17-jre-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

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

#### 2. Docker Compose

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
    volumes:
      - mysql-data:/var/lib/mysql
    ports:
      - "3306:3306"

  redis:
    image: redis:7
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"

  nacos:
    image: nacos/nacos-server:v2.3.0
    environment:
      MODE: standalone
    ports:
      - "8848:8848"

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: admin
      MINIO_ROOT_PASSWORD: admin123
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio-data:/data

  siae-gateway:
    image: siae-gateway:latest
    ports:
      - "80:80"
    depends_on:
      - nacos

  siae-auth:
    image: siae-auth:latest
    ports:
      - "8000:8000"
    depends_on:
      - mysql
      - redis
      - nacos

  # 其他服务...

volumes:
  mysql-data:
  minio-data:
```

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f siae-auth

# 停止服务
docker-compose down
```

### Kubernetes部署

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: siae-auth
spec:
  replicas: 3
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

```bash
# 部署
kubectl apply -f deployment.yaml

# 查看状态
kubectl get pods
kubectl get svc

# 查看日志
kubectl logs -f siae-auth-xxxxx
```

### 生产环境配置

```yaml
# application-prod.yaml
spring:
  datasource:
    url: jdbc:mysql://prod-mysql:3306/auth_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  redis:
    host: prod-redis
    password: ${REDIS_PASSWORD}

  rabbitmq:
    host: prod-rabbitmq
    username: ${MQ_USERNAME}
    password: ${MQ_PASSWORD}

  cloud:
    nacos:
      discovery:
        server-addr: prod-nacos:8848
      config:
        server-addr: prod-nacos:8848

logging:
  level:
    root: INFO
  file:
    name: /var/log/siae/auth.log
```

### 性能优化

1. **数据库连接池**：
```yaml
spring:
  datasource:
    druid:
      initial-size: 10
      max-active: 100
      min-idle: 10
      max-wait: 60000
```

2. **Redis缓存**：
```java
@Cacheable(value = "user", key = "#id")
public UserVO getUserById(Long id) { ... }
```

3. **异步处理**：
```java
@Async
public void sendNotification(Long userId) { ... }
```

4. **限流配置**：
```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: siae-auth
        uri: lb://siae-auth
        predicates:
        - Path=/api/v1/auth/**
        filters:
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 10
            redis-rate-limiter.burstCapacity: 20
```

---

## 🤝 贡献指南

### 如何贡献

1. **Fork项目**
2. **创建特性分支** (`git checkout -b feature/AmazingFeature`)
3. **提交更改** (`git commit -m 'feat: Add some AmazingFeature'`)
4. **推送到分支** (`git push origin feature/AmazingFeature`)
5. **创建Pull Request**

### 代码审查标准

- [ ] 代码符合项目编码规范
- [ ] 单元测试覆盖率不低于80%
- [ ] 通过所有CI检查
- [ ] API文档完善
- [ ] 提交信息符合规范

### 问题反馈

- 提交Issue前请先搜索是否已有相同问题
- 使用Issue模板提供详细信息
- 包含复现步骤和环境信息

---

## 📄 许可证

本项目采用 [MIT License](LICENSE) 开源协议

---

## 📞 联系方式

- **项目地址**: https://github.com/your-org/siae
- **问题反馈**: https://github.com/your-org/siae/issues
- **作者**: KEYKB
- **邮箱**: your-email@example.com

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Cloud](https://spring.io/projects/spring-cloud)
- [MyBatis-Plus](https://baomidou.com/)
- [Hutool](https://hutool.cn/)
- [Ollama](https://ollama.ai/)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，欢迎Star支持！**

Made with ❤️ by KEYKB

</div>
