# SIAE 项目开发流程指南

## 目录

1. [项目概述](#项目概述)
2. [项目架构](#项目架构)
3. [服务详解](#服务详解)
4. [开发环境搭建](#开发环境搭建)
5. [开发规范](#开发规范)
6. [安全架构](#安全架构)
7. [API文档规范](#api文档规范)
8. [数据库设计](#数据库设计)
9. [开发工作流](#开发工作流)
10. [部署指南](#部署指南)
11. [故障排查](#故障排查)

---

## 项目概述

**SIAE (Software Industry Association E-platform)** 是一个基于Spring Cloud微服务架构的软件协会官网系统，采用前后端分离的设计模式。

### 技术栈

- **后端框架**: Spring Boot 3.2.5, Spring Cloud 2023.0.1
- **服务治理**: Spring Cloud Alibaba 2023.0.1.0, Nacos
- **网关**: Spring Cloud Gateway
- **安全框架**: Spring Security + JWT
- **数据库**: MySQL 8.0
- **ORM框架**: MyBatis Plus 3.5.6
- **API文档**: SpringDoc OpenAPI 3 (Swagger)
- **服务间通信**: OpenFeign
- **构建工具**: Maven
- **JDK版本**: Java 17

---

## 项目架构

### 架构图 (文本版)

```
┌─────────────────────────────────────────────────────────────┐
│                        前端应用                              │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP/HTTPS
┌─────────────────────▼───────────────────────────────────────┐
│                  siae-gateway                               │
│              (Spring Cloud Gateway)                        │
│                   端口: 8080                                │
└─────┬─────────┬─────────┬─────────┬─────────┬──────────────┘
      │         │         │         │         │
      ▼         ▼         ▼         ▼         ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│siae-auth │ │siae-user │ │siae-     │ │siae-     │ │   其他   │
│  :8000   │ │  :8020   │ │content   │ │message   │ │  服务    │
│          │ │          │ │  :8010   │ │  :8030   │ │          │
└─────┬────┘ └─────┬────┘ └─────┬────┘ └─────┬────┘ └──────────┘
      │            │            │            │
      └────────────┼────────────┼────────────┘
                   │            │
                   ▼            ▼
            ┌─────────────────────────┐
            │      siae-common        │
            │      siae-core          │
            │     (共享模块)           │
            └─────────────────────────┘
                   │
                   ▼
            ┌─────────────────────────┐
            │      Nacos Server       │
            │    (配置中心+注册中心)    │
            └─────────────────────────┘
                   │
                   ▼
            ┌─────────────────────────┐
            │      MySQL 数据库       │
            │  auth_db | user_db      │
            │  content_db | message_db│
            └─────────────────────────┘
```

### 模块依赖关系

```
siae-parent (父项目)
├── siae-core (核心工具类)
├── siae-common (通用组件)
├── siae-gateway (API网关)
└── services/
    ├── siae-auth (认证服务)
    ├── siae-user (用户服务)
    ├── siae-content (内容服务)
    └── siae-message (消息服务)
```

---

## 服务详解

### 1. siae-gateway (API网关)
**端口**: 8080
**职责**: 统一入口、路由转发、JWT认证、跨域处理

**核心组件**:
- `JwtAuthFilter`: JWT全局认证过滤器
- `CorsConfig`: 跨域配置
- 路由配置: 将请求转发到对应的微服务

**路由规则**:
```yaml
/api/v1/auth/**   → siae-auth:8000
/api/v1/user/**   → siae-user:8020
/api/v1/content/** → siae-content:8010
/api/v1/message/** → siae-message:8030
```

### 2. siae-auth (认证服务)
**端口**: 8000
**数据库**: auth_db
**职责**: 用户认证、权限管理、RBAC系统

**核心控制器**:
- `AuthController`: 登录、注册、刷新令牌、登出
- `PermissionController`: 权限管理 (CRUD)
- `RoleController`: 角色管理 (CRUD)
- `UserRoleController`: 用户角色关联管理
- `UserPermissionController`: 用户权限管理

**数据表结构**:
- `role`: 角色表
- `permission`: 权限表 (支持层级结构)
- `user_role`: 用户角色关联表
- `role_permission`: 角色权限关联表
- `user_permission`: 用户权限关联表
- `user_auth`: 用户认证令牌表

### 3. siae-user (用户服务)
**端口**: 8020
**数据库**: user_db
**职责**: 用户信息管理、成员管理、奖项管理

**核心控制器**:
- `UserController`: 用户基础信息管理
- `UserDetailController`: 用户详细信息管理
- `MemberController`: 正式成员管理
- `CandidateController`: 候选成员管理
- `ClassController`: 班级管理
- `AwardTypeController`: 奖项类型管理
- `AwardLevelController`: 奖项等级管理
- `UserAwardController`: 用户获奖记录管理

### 4. siae-content (内容服务)
**端口**: 8010
**数据库**: content_db
**职责**: 内容管理、分类标签、用户交互、统计审核

**核心控制器**:
- `ContentController`: 内容发布、编辑、删除、查询
- `CategoriesController`: 分类管理
- `TagsController`: 标签管理
- `InteractionsController`: 用户交互 (点赞、收藏等)
- `StatisticsController`: 统计数据管理
- `AuditsController`: 内容审核管理

### 5. siae-message (消息服务)
**端口**: 8030
**数据库**: message_db
**职责**: 邮件发送、消息通知

**核心控制器**:
- `EmailController`: 邮件发送管理

### 6. siae-core (核心模块)
**职责**: 提供核心工具类和通用组件

**核心组件**:
- `Result<T>`: 统一响应结果封装
- `JwtUtils`: JWT工具类
- `BeanConvertUtil`: Bean转换工具
- `AssertUtils`: 断言工具
- `ServiceException`: 业务异常类
- 权限常量定义: `ContentPermissions`, `UserPermissions`
- 结果码枚举: `CommonResultCodeEnum`, `AuthResultCodeEnum`, `UserResultCodeEnum`

### 7. siae-common (通用模块)
**职责**: 提供通用配置和组件

**核心组件**:
- `SecurityConfig`: Spring Security通用配置
- `MybatisPlusConfig`: MyBatis Plus配置
- `JacksonConfig`: JSON序列化配置
- `UnifiedResponseAdvice`: 统一响应体处理
- `GlobalExceptionHandler`: 全局异常处理
- `JwtAuthenticationFilter`: JWT认证过滤器
- `DefaultFeignConfig`: Feign默认配置
- 验证分组: `CreateGroup`, `UpdateGroup`, `QueryGroup`

---

## 开发环境搭建

### 1. 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Nacos Server 2.3.0+
- IDE: IntelliJ IDEA (推荐)

### 2. 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE content_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE message_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本
-- services/siae-auth/src/main/resources/sql/auth_db.sql
```

### 3. Nacos配置
启动Nacos Server，访问 http://localhost:8848/nacos
默认用户名/密码: nacos/nacos

创建配置文件 (Group: SIAE_GROUP):
- siae-auth.yaml
- siae-user.yaml
- siae-content.yaml
- siae-message.yaml
- siae-gateway.yaml

### 4. 启动顺序
1. 启动 Nacos Server
2. 启动 siae-gateway
3. 启动各个微服务 (siae-auth, siae-user, siae-content, siae-message)

---

## 开发规范

### 1. 代码结构规范
```
src/main/java/com/hngy/siae/{service}/
├── controller/          # 控制器层
├── service/            # 服务层
│   └── impl/          # 服务实现
├── mapper/            # 数据访问层
├── entity/            # 实体类
├── dto/               # 数据传输对象
│   ├── request/       # 请求DTO
│   └── response/      # 响应DTO
├── config/            # 配置类
├── filter/            # 过滤器
├── listener/          # 监听器
└── util/              # 工具类
```

### 2. 命名规范
- **类名**: 大驼峰命名法 (PascalCase)
- **方法名**: 小驼峰命名法 (camelCase)
- **常量**: 全大写下划线分隔 (UPPER_SNAKE_CASE)
- **包名**: 全小写，多个单词用点分隔

### 3. 注解规范
- 控制器类必须添加 `@Tag` 注解
- 控制器方法必须添加 `@Operation` 注解
- 参数必须添加 `@Parameter` 注解
- 响应必须添加 `@ApiResponses` 注解
- 权限控制必须添加 `@PreAuthorize` 注解

---

## 安全架构

### 1. JWT认证流程
```
1. 用户登录 → siae-auth服务验证 → 生成JWT Token
2. 客户端携带Token访问API → siae-gateway验证Token
3. Token有效 → 转发请求到对应微服务
4. 微服务通过@PreAuthorize验证具体权限
```

### 2. RBAC权限模型
```
用户(User) ←→ 角色(Role) ←→ 权限(Permission)
     ↓              ↓              ↓
  用户表          角色表          权限表
     ↓              ↓              ↓
用户角色表      角色权限表      用户权限表
```

### 3. 权限常量定义
权限码格式: `模块:资源:操作`
- 系统级: `system:user:create`
- 内容级: `content:article:publish`
- 用户级: `user:profile:update`

### 4. 权限注解使用
```java
@PreAuthorize("hasAuthority('" + USER_USER_CREATE + "')")
public Result<UserVO> createUser(@RequestBody UserDTO userDTO) {
    // 业务逻辑
}
```

---

## API文档规范

### 1. SpringDoc配置
每个服务都配置独立的API文档:
- siae-auth: http://localhost:8000/swagger-ui.html
- siae-user: http://localhost:8020/swagger-ui.html
- siae-content: http://localhost:8010/swagger-ui.html

### 2. 注解使用规范
```java
@Tag(name = "用户管理", description = "用户相关操作")
@RestController
public class UserController {

    @Operation(summary = "创建用户", description = "创建新用户账户")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping("/create")
    public Result<UserVO> createUser(
        @Parameter(description = "用户信息", required = true)
        @RequestBody UserDTO userDTO) {
        // 实现
    }
}
```

---

## 数据库设计

### 1. 命名规范
- 表名: 小写下划线分隔 (snake_case)
- 字段名: 小写下划线分隔 (snake_case)
- 主键: id (BIGINT AUTO_INCREMENT)
- 外键: {table}_id
- 时间字段: created_at, updated_at

### 2. 通用字段
```sql
id BIGINT PRIMARY KEY AUTO_INCREMENT,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

### 3. 数据库分离
- auth_db: 认证相关数据
- user_db: 用户相关数据
- content_db: 内容相关数据
- message_db: 消息相关数据

---

## 开发工作流

### 1. 新功能开发流程
1. **需求分析**: 明确功能需求和业务逻辑
2. **数据库设计**: 设计表结构，编写DDL脚本
3. **权限设计**: 定义权限常量，更新auth_db.sql
4. **实体类创建**: 创建Entity、DTO、VO类
5. **数据访问层**: 编写Mapper接口和XML
6. **服务层开发**: 编写Service接口和实现
7. **控制器开发**: 编写Controller，添加权限注解
8. **API文档**: 完善SpringDoc注解
9. **单元测试**: 编写测试用例
10. **集成测试**: 测试完整业务流程

### 2. 代码提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动
```

### 3. 分支管理
- main: 主分支，生产环境代码
- develop: 开发分支
- feature/*: 功能分支
- hotfix/*: 热修复分支

### 4. 权限开发流程
1. **定义权限常量**: 在对应的Permissions类中定义
2. **更新数据库**: 在auth_db.sql中添加权限记录
3. **添加注解**: 在Controller方法上添加@PreAuthorize
4. **角色分配**: 为不同角色分配相应权限
5. **测试验证**: 验证权限控制是否生效

---

## 部署指南

### 1. 构建命令
```bash
# 根目录执行
mvn clean package -DskipTests

# 单个服务构建
cd services/siae-auth
mvn clean package -DskipTests
```

### 2. Docker部署 (推荐)
```dockerfile
FROM openjdk:17-jre-slim
COPY target/*.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 3. 环境配置
- 开发环境: application-dev.yaml
- 测试环境: application-test.yaml
- 生产环境: application-prod.yaml

### 4. 服务启动脚本
```bash
#!/bin/bash
# 启动所有服务
echo "启动Nacos..."
# 启动Nacos命令

echo "启动网关..."
cd siae-gateway && java -jar target/siae-gateway-*.jar &

echo "启动认证服务..."
cd services/siae-auth && java -jar target/siae-auth-*.jar &

echo "启动用户服务..."
cd services/siae-user && java -jar target/siae-user-*.jar &

echo "启动内容服务..."
cd services/siae-content && java -jar target/siae-content-*.jar &

echo "启动消息服务..."
cd services/siae-message && java -jar target/siae-message-*.jar &
```

---

## 故障排查

### 1. 常见问题
- **服务启动失败**: 检查Nacos连接、数据库连接
- **JWT认证失败**: 检查Token格式、密钥配置
- **权限验证失败**: 检查权限常量、数据库权限数据
- **服务间调用失败**: 检查Feign配置、服务注册状态
- **跨域问题**: 检查Gateway的CORS配置

### 2. 日志查看
```bash
# 查看服务日志
tail -f logs/siae-auth.log

# 查看错误日志
grep "ERROR" logs/siae-auth.log

# 实时监控日志
tail -f logs/*.log | grep -E "(ERROR|WARN)"
```

### 3. 监控检查
- Nacos控制台: http://localhost:8848/nacos
- 服务健康检查: /actuator/health
- API文档: /swagger-ui.html
- 网关路由: http://localhost:8080/actuator/gateway/routes

### 4. 调试技巧
- 使用Postman测试API接口
- 检查JWT Token的有效性和权限
- 查看数据库权限数据是否正确
- 验证服务注册状态

---

## 最佳实践

### 1. 代码质量
- 遵循阿里巴巴Java开发手册
- 使用SonarQube进行代码质量检查
- 编写单元测试，覆盖率不低于80%
- 定期进行代码Review

### 2. 性能优化
- 合理使用缓存 (Redis)
- 数据库查询优化，避免N+1问题
- 异步处理耗时操作
- 合理设置连接池参数

### 3. 安全考虑
- 敏感信息加密存储
- SQL注入防护
- XSS攻击防护
- 接口限流和防刷

### 4. 监控告警
- 集成Prometheus + Grafana
- 设置关键指标监控
- 配置告警规则
- 日志聚合分析

---

## 联系方式

如有问题，请联系开发团队或查看项目文档。

**项目地址**: https://github.com/your-org/siae
**文档地址**: https://docs.siae.com
**最后更新**: 2025-01-21
**版本**: v1.0.0