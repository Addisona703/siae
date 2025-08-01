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

#### 核心框架
- **后端框架**: Spring Boot 3.2.5
- **微服务框架**: Spring Cloud 2023.0.1
- **Spring Cloud Alibaba**: 2023.0.1.0
- **JDK版本**: Java 17

#### 数据存储
- **数据库**: MySQL 8.0
- **ORM框架**: MyBatis-Plus 3.5.6
- **连接池**: Druid 1.2.22
- **缓存**: Redis

#### 服务治理
- **服务注册与发现**: Nacos
- **配置管理**: Nacos Config
- **API网关**: Spring Cloud Gateway
- **分布式事务**: Seata 1.8.0

#### 安全认证
- **认证授权**: Spring Security + JWT
- **JWT库**: JJWT 0.11.5

#### 开发工具
- **API文档**: SpringDoc OpenAPI 3 (Swagger) 2.5.0
- **工具库**: Hutool 5.8.27
- **代码简化**: Lombok 1.18.30
- **构建工具**: Maven 3.9+

---

## 项目架构

### 架构图 (文本版)

```
┌─────────────────────────────────────────────────────────────┐
│                        前端应用                              │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP/HTTPS (直接访问各服务)
                      │
      ┌───────────────┼───────────────┬───────────────┐
      │               │               │               │
      ▼               ▼               ▼               ▼
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│siae-auth │    │siae-user │    │siae-     │    │siae-     │
│  :8000   │    │  :8020   │    │content   │    │message   │
│          │    │          │    │  :8010   │    │  :8030   │
└─────┬────┘    └─────┬────┘    └─────┬────┘    └─────┬────┘
      │               │               │               │
      └───────────────┼───────────────┼───────────────┘
                      │               │
                      ▼               ▼
               ┌─────────────────────────┐
               │      siae-core          │
               │   ├── siae-common       │
               │   ├── siae-security     │
               │   └── siae-web          │
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
                      │
                      ▼
               ┌─────────────────────────┐
               │       Redis             │
               │      (缓存服务)          │
               └─────────────────────────┘
```

### 模块依赖关系

```
siae-parent (父项目)
├── siae-core/ (核心模块)
│   ├── siae-common (通用工具类和常量)
│   ├── siae-security (安全认证模块)
│   └── siae-web (Web通用配置)
└── services/ (微服务模块)
    ├── siae-auth (认证服务 - 端口: 8000)
    ├── siae-user (用户服务 - 端口: 8020)
    ├── siae-content (内容服务 - 端口: 8010)
    └── siae-message (消息服务 - 端口: 8030)
```

### 服务端口分配

| 服务名称 | 端口 | 上下文路径 | 主要功能 |
|----------|------|------------|----------|
| siae-auth | 8000 | `/api/v1/auth` | 认证授权、权限管理、日志管理 |
| siae-user | 8020 | `/api/v1/user` | 用户管理、成员管理、班级管理、奖项管理 |
| siae-content | 8010 | `/api/v1/content` | 内容管理、分类标签、审核评论、统计 |
| siae-message | 8030 | `/api/v1/message` | 消息通知、邮件验证码 |

---

## 服务详解

### 1. siae-auth (认证服务)
**端口**: 8000
**上下文路径**: `/api/v1/auth`
**数据库**: auth_db
**职责**: 用户认证、权限管理、RBAC系统

**核心控制器**:
- `AuthController`: 登录、注册、刷新令牌、登出 (4个接口)
- `PermissionController`: 权限管理 (CRUD + 树形结构) (7个接口)
- `RoleController`: 角色管理 (CRUD + 权限分配) (9个接口)
- `UserRoleController`: 用户角色关联管理 (4个接口)
- `UserPermissionController`: 用户权限管理 (5个接口)
- `LogController`: 登录日志查询 (2个接口)

**数据表结构**:
- `permission`: 权限表 (支持层级结构)
- `role`: 角色表
- `user_role`: 用户角色关联表
- `role_permission`: 角色权限关联表
- `user_permission`: 用户权限关联表
- `user_auth`: 用户认证令牌表
- `login_log`: 登录日志表

**权限模型**:
- 基于RBAC的权限控制
- 支持角色权限和直接权限
- Redis缓存权限信息
- 使用@SiaeAuthorize注解进行权限验证

**Swagger文档**: http://localhost:8000/api/v1/auth/swagger-ui.html

### 2. siae-user (用户服务)
**端口**: 8020
**上下文路径**: `/api/v1/user`
**数据库**: user_db
**职责**: 用户信息管理、成员管理、奖项管理

**核心控制器**:
- `UserController`: 用户基础信息管理 (5个接口)
- `UserProfileController`: 用户详细信息管理 (4个接口)
- `MemberController`: 正式成员管理 (4个接口)
- `MemberCandidateController`: 候选成员管理 (5个接口)
- `ClassInfoController`: 班级管理 (7个接口)
- `AwardTypeController`: 奖项类型管理 (6个接口)
- `AwardLevelController`: 奖项等级管理 (6个接口)
- `UserAwardController`: 用户获奖记录管理 (5个接口)

**Swagger文档**: http://localhost:8020/api/v1/user/swagger-ui.html

### 3. siae-content (内容服务)
**端口**: 8010
**上下文路径**: `/api/v1/content`
**数据库**: content_db
**职责**: 内容管理、分类标签、用户交互、统计审核

**核心控制器**:
- `ContentController`: 内容发布、编辑、删除、查询 (5个接口)
- `CategoriesController`: 分类管理 (5个接口)
- `TagsController`: 标签管理 (4个接口)
- `CommentsController`: 评论管理 (5个接口)
- `AuditsController`: 内容审核管理 (3个接口)
- `InteractionsController`: 用户交互 (点赞、收藏等) (2个接口)
- `StatisticsController`: 统计数据管理 (2个接口)

**数据表结构** (已重构为单数表名):
- `content`: 内容主表
- `article`, `note`, `question`, `file`, `video`: 各类型内容详情表
- `category`: 分类表
- `tag`, `tag_relation`: 标签及关联表
- `comment`: 评论表
- `statistics`: 统计表
- `user_action`: 用户行为表
- `audit`: 审核记录表

**Swagger文档**: http://localhost:8010/api/v1/content/swagger-ui.html

### 4. siae-message (消息服务)
**端口**: 8030
**上下文路径**: `/api/v1/message`
**数据库**: message_db
**职责**: 邮件发送、消息通知

**核心控制器**:
- `EmailController`: 邮件发送管理 (2个接口)

**Swagger文档**: http://localhost:8030/api/v1/message/swagger-ui.html

### 5. siae-core (核心模块)
**职责**: 提供核心工具类和通用组件

#### siae-common (通用工具类)
- `Result<T>`: 统一响应结果封装
- `BeanConvertUtil`: Bean转换工具
- `AssertUtils`: 断言工具
- `ServiceException`: 业务异常类
- 权限常量定义: `AuthPermissions`, `ContentPermissions`, `UserPermissions`
- 结果码枚举: `CommonResultCodeEnum`, `AuthResultCodeEnum`, `UserResultCodeEnum`

#### siae-security (安全认证模块)
- `@SiaeAuthorize`: 自定义权限注解
- `JwtUtils`: JWT工具类
- `SecurityUtils`: 安全工具类
- `AuthenticationEntryPoint`: 认证入口点
- `AccessDeniedHandler`: 访问拒绝处理器

#### siae-web (Web通用配置)
- `GlobalExceptionHandler`: 全局异常处理
- `UnifiedResponseAdvice`: 统一响应体处理
- `PageConvertUtil`: 分页转换工具
- `WebUtils`: Web工具类
- `CorsConfig`: 跨域配置
- 验证分组: `CreateGroup`, `UpdateGroup`, `QueryGroup`

---

## 开发环境搭建

### 1. 环境要求
- **JDK**: 17+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Redis**: 6.0+ (用于缓存)
- **Nacos Server**: 2.3.0+ (服务注册与配置管理)
- **IDE**: IntelliJ IDEA (推荐)

### 2. 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE content_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE message_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**执行初始化脚本**:
- `services/siae-auth/src/main/resources/sql/auth_db.sql`
- `services/siae-user/src/main/resources/sql/user_db.sql`
- `services/siae-content/src/main/resources/sql/content_db.sql` (已包含测试数据)
- `services/siae-message/src/main/resources/sql/message_db.sql`

### 3. Redis配置
启动Redis服务，默认端口6379，用于缓存权限信息和邮件验证码。

### 4. Nacos配置
启动Nacos Server，访问 http://localhost:8848/nacos
默认用户名/密码: nacos/nacos

创建配置文件 (Group: SIAE_GROUP):
- siae-auth.yaml
- siae-user.yaml
- siae-content.yaml
- siae-message.yaml

### 5. 启动顺序
1. 启动 MySQL 和 Redis
2. 启动 Nacos Server
3. 启动各个微服务 (推荐顺序):
   - siae-auth (认证服务)
   - siae-user (用户服务)
   - siae-content (内容服务)
   - siae-message (消息服务)

### 6. 验证启动
访问各服务的Swagger文档验证启动成功:
- 认证服务: http://localhost:8000/api/v1/auth/swagger-ui.html
- 用户服务: http://localhost:8020/api/v1/user/swagger-ui.html
- 内容服务: http://localhost:8010/api/v1/content/swagger-ui.html
- 消息服务: http://localhost:8030/api/v1/message/swagger-ui.html

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

#### Swagger文档注解 (严格遵循规则7.2)
**只允许使用以下三个Swagger注解**:
- `@Tag`: 控制器类级别注解，描述API模块
- `@Operation`: 方法级别注解，包含summary和description
- `@Parameter`: 参数级别注解，描述请求参数

**禁止使用的注解**: `@ApiResponses`, `@ApiResponse`, `@Content`, `@Schema`等

#### 权限认证注解
- 权限控制必须使用 `@SiaeAuthorize` 注解
- 禁止使用 `@PreAuthorize` 注解

#### JavaDoc注释规范
- 所有类必须添加 `@author KEYKB` 注解
- 方法级别的JavaDoc不需要添加 `@author` 字段
- 保持简洁精炼的注释描述

---

## 安全架构

### 1. JWT认证流程
```
1. 用户登录 → siae-auth服务验证 → 生成JWT Token
2. 客户端携带Token直接访问各微服务API
3. 各微服务通过@SiaeAuthorize验证Token和具体权限
4. Redis缓存权限信息，提高验证性能
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
@SiaeAuthorize("hasAuthority('" + AUTH_USER_CREATE + "')")
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
1. **定义权限常量**: 在AuthPermissions类中定义权限常量
2. **更新数据库**: 在auth_db.sql中添加权限记录
3. **添加注解**: 在Controller方法上添加@SiaeAuthorize注解
4. **角色分配**: 通过角色管理接口为不同角色分配相应权限
5. **测试验证**: 验证权限控制是否生效，检查Redis缓存

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

---

## 📚 API文档

### Swagger UI 访问地址

| 服务名称 | Swagger UI 地址 | 说明 |
|----------|----------------|------|
| 认证服务 | http://localhost:8000/api/v1/auth/swagger-ui.html | 认证、权限、角色管理 |
| 用户服务 | http://localhost:8020/api/v1/user/swagger-ui.html | 用户、成员、班级、奖项管理 |
| 内容服务 | http://localhost:8010/api/v1/content/swagger-ui.html | 内容、分类、标签、审核管理 |
| 消息服务 | http://localhost:8030/api/v1/message/swagger-ui.html | 邮件、消息通知 |

### API接口清单
详细的API接口清单请查看: [Controller-List.md](markdown/Controller-List.md)

---

## 🔗 重要链接

- **项目文档**: [markdown/](markdown/)
- **API接口清单**: [Controller-List.md](markdown/Controller-List.md)
- **编码规范**: [.augment/rules/siae-rule.md](.augment/rules/siae-rule.md)
- **数据库设计**: 各服务的 `src/main/resources/sql/` 目录

---

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

**最后更新**: 2025-08-01
**文档版本**: v0.1.0
**维护团队**: SIAE开发团队
- 日志聚合分析

---

## 联系方式

如有问题，请联系开发团队或查看项目文档。

**项目地址**: https://github.com/your-org/siae
**文档地址**: https://docs.siae.com
**最后更新**: 2025-08-01
**版本**: v0.1.0