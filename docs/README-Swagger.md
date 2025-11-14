# SIAE 统一Swagger配置使用指南

## 📋 概述

本文档介绍了SIAE项目中统一的Swagger/OpenAPI配置方案，通过将Swagger配置集中到`siae-common`模块中，实现了多微服务的API文档统一管理。

## 🏗️ 架构设计

### Java配置类结构
```
siae-common/src/main/java/com/hngy/siae/common/config/
├── OpenApiConfig.java              # 主配置类，定义OpenAPI和分组
├── SwaggerUIConfig.java            # UI自定义配置，全局响应示例
├── SwaggerProperties.java          # 配置属性类，支持运行时配置
├── SwaggerConstants.java           # 配置常量类，集中管理所有常量
└── SwaggerAutoConfiguration.java   # 自动配置类，控制启用/禁用
```

### 核心特性
- ✅ **纯Java配置**：完全基于Java代码的配置，无需外部配置文件
- ✅ **多服务分组**：支持认证、用户、内容、消息等服务分组显示
- ✅ **统一认证**：集成JWT Bearer Token和API Key认证
- ✅ **全局响应**：自动添加通用错误响应示例
- ✅ **常量管理**：集中管理所有配置常量，便于维护
- ✅ **自动配置**：支持条件化启用/禁用
- ✅ **Spring Boot 3.x兼容**：基于OpenAPI 3.0规范

## 🚀 快速开始

### 1. 添加依赖

各微服务的`pom.xml`中添加`siae-common`依赖：

```xml
<dependency>
    <groupId>com.hngy</groupId>
    <artifactId>siae-common</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 移除重复配置

删除各服务中的OpenAPI配置类：
- `services/siae-auth/src/main/java/com/hngy/siae/auth/config/OpenApiConfig.java`
- `services/siae-user/src/main/java/com/hngy/siae/user/config/OpenApiConfig.java`
- `services/siae-content/src/main/java/com/hngy/siae/content/config/OpenApiConfig.java`

### 3. 配置应用属性（可选）

在各服务的`application.yaml`中添加基础配置（如需要）：

```yaml
# SpringDoc基础配置（可选，有默认值）
springdoc:
  api-docs:
    enabled: true          # 启用API文档，默认true
    path: /v3/api-docs     # API文档路径，默认值
  swagger-ui:
    enabled: true          # 启用Swagger UI，默认true
    path: /swagger-ui.html # Swagger UI路径，默认值
    tags-sorter: alpha     # 标签排序，默认值
    operations-sorter: alpha # 操作排序，默认值

# 服务基础信息（自动检测，通常无需配置）
spring:
  application:
    name: siae-auth        # 服务名称，用于自动配置
server:
  port: 8000              # 服务端口，用于构建服务器URL
  servlet:
    context-path: /api/v1/auth # 上下文路径，用于构建服务器URL
```

## 📚 Java配置详解

### OpenApiConfig.java
主配置类，负责：
- 创建全局OpenAPI实例，自动检测服务类型
- 定义5个服务分组（GroupedOpenApi）
- 配置JWT和API Key双重认证方案
- 动态构建服务器信息（本地、生产、网关）

### SwaggerUIConfig.java
UI自定义配置类，负责：
- 添加6种标准HTTP状态码响应示例
- 自动检测带@PreAuthorize注解的接口添加认证响应
- 自动设置操作ID
- 全局标签和操作排序

### SwaggerConstants.java
配置常量类，集中管理：
- 所有服务名称、分组名称、显示名称
- 路径匹配规则、包扫描路径
- 认证方案名称和描述
- 响应示例JSON字符串
- 服务器URL和描述信息

### SwaggerProperties.java
配置属性类，支持：
- 运行时配置管理
- 程序化配置调整
- 配置状态查询

### SwaggerAutoConfiguration.java
自动配置类，负责：
- 条件化启用Swagger配置
- 统一导入所有配置类
- 支持通过springdoc.api-docs.enabled控制

## 🎯 服务分组

系统自动创建以下API分组：

| 分组 | 显示名称 | 路径匹配 | 包扫描 |
|------|----------|----------|--------|
| 01-认证服务 | 🔐 认证服务API | `/api/v1/auth/**` | `com.hngy.siae.auth.controller` |
| 02-用户服务 | 👥 用户服务API | `/api/v1/user/**` | `com.hngy.siae.user.controller` |
| 03-内容服务 | 📝 内容服务API | `/api/v1/content/**` | `com.hngy.siae.content.controller` |
| 04-消息服务 | 📨 消息服务API | `/api/v1/message/**` | `com.hngy.siae.message.controller` |
| 05-系统管理 | ⚙️ 系统管理API | `/permissions/**` | `com.hngy.siae.auth.controller` |

## 🔐 认证配置

### JWT认证
```yaml
siae:
  swagger:
    security:
      jwt-enabled: true
      jwt-scheme-name: "JWT"
      jwt-description: "JWT认证，请在请求头中添加：Authorization: Bearer {token}"
```

### API Key认证
```yaml
siae:
  swagger:
    security:
      api-key-enabled: true
      api-key-scheme-name: "ApiKey"
      api-key-header-name: "X-API-KEY"
      api-key-description: "API密钥认证，用于服务间调用"
```

## 📱 访问地址

### 各服务独立访问
- 认证服务: http://localhost:8000/api/v1/auth/swagger-ui.html
- 用户服务: http://localhost:8020/api/v1/user/swagger-ui.html
- 内容服务: http://localhost:8010/api/v1/content/swagger-ui.html
- 消息服务: http://localhost:8030/api/v1/message/swagger-ui.html

### 网关聚合访问
- 统一入口: http://localhost:8080/swagger-ui.html

## 🛠️ 自定义配置

### 个性化API信息
```yaml
siae:
  swagger:
    api-info:
      title: "自定义服务API"
      description: "自定义服务描述"
      version: "v2.0.0"
    contact:
      name: "开发团队"
      email: "dev@example.com"
      url: "https://github.com/example"
```

### 自定义服务器信息
```yaml
siae:
  swagger:
    servers:
      - url: "http://localhost:8080"
        description: "本地环境"
      - url: "https://api.example.com"
        description: "生产环境"
```

### UI个性化配置
```yaml
siae:
  swagger:
    ui:
      tags-sorter: "alpha"           # 标签排序方式
      operations-sorter: "method"    # 操作排序方式
      show-request-duration: true    # 显示请求耗时
      default-models-expand-depth: 2 # 模型展开深度
```

## 🔧 开发指南

### 控制器注解规范
```java
@Tag(name = "用户管理", description = "用户相关操作")
@RestController
@RequestMapping("/users")
public class UserController {

    @Operation(summary = "创建用户", description = "创建新用户账户")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public Result<UserVO> createUser(@RequestBody UserDTO userDTO) {
        // 实现逻辑
    }
}
```

### 自动响应增强
系统会自动为需要认证的接口添加：
- 401 未授权响应
- 403 权限不足响应
- 500 服务器错误响应

## 📝 最佳实践

1. **统一注解使用**：使用`@Tag`、`@Operation`、`@ApiResponse`等注解
2. **权限注解检测**：系统自动检测`@PreAuthorize`注解添加认证响应
3. **响应示例完整**：提供完整的成功和错误响应示例
4. **分组合理规划**：按业务模块合理划分API分组
5. **配置外部化**：通过配置文件管理个性化设置

## 🚨 注意事项

1. **依赖版本**：确保使用Spring Boot 3.x和OpenAPI 3.0
2. **包扫描路径**：确保controller包路径正确配置
3. **认证配置**：根据实际认证方案调整安全配置
4. **网关集成**：网关需要配置相应的路由规则

## 🔄 升级指南

从旧版本升级到统一配置：

1. 备份现有配置文件
2. 移除各服务的OpenAPI配置类
3. 添加siae-common依赖
4. 更新application.yaml配置
5. 测试API文档访问

## 🐛 故障排除

### 常见问题

#### 1. Swagger UI无法访问
**问题**: 访问swagger-ui.html返回404
**解决方案**:
- 检查SpringDoc依赖是否正确添加
- 确认`springdoc.swagger-ui.enabled=true`
- 验证context-path配置

#### 2. API分组不显示
**问题**: 某个服务的API分组不显示
**解决方案**:
- 检查包扫描路径是否正确
- 确认Controller类上有`@RestController`注解
- 验证路径匹配规则

#### 3. 认证配置不生效
**问题**: JWT认证配置不显示
**解决方案**:
- 检查Security配置是否启用
- 确认`@PreAuthorize`注解是否正确
- 验证SecurityScheme配置

#### 4. 响应示例不显示
**问题**: 全局响应示例不显示
**解决方案**:
- 检查SwaggerUIConfig是否被扫描
- 确认OpenApiCustomizer配置
- 验证MediaType配置

### 调试技巧

1. **启用调试日志**:
```yaml
logging:
  level:
    org.springdoc: DEBUG
    io.swagger: DEBUG
```

2. **检查OpenAPI JSON**:
访问 `/v3/api-docs` 查看生成的OpenAPI规范

3. **验证配置加载**:
```java
@Autowired
private SwaggerProperties swaggerProperties;

@PostConstruct
public void checkConfig() {
    log.info("Swagger enabled: {}", swaggerProperties.isEnabled());
}
```

## 📊 性能优化

### 生产环境配置
```yaml
# 生产环境建议禁用Swagger
spring:
  profiles:
    active: prod

---
spring:
  profiles: prod
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
siae:
  swagger:
    enabled: false
```

### 缓存优化
```yaml
# 启用OpenAPI缓存
springdoc:
  cache:
    disabled: false
  api-docs:
    resolve-schema-properties: true
```

## 🔗 相关链接

- [SpringDoc官方文档](https://springdoc.org/)
- [OpenAPI 3.0规范](https://swagger.io/specification/)
- [Swagger UI配置](https://swagger.io/docs/open-source-tools/swagger-ui/usage/configuration/)
- [Spring Security集成](https://springdoc.org/#spring-security-support)

---

**维护团队**: SIAE开发团队
**最后更新**: 2024-01-01
**版本**: v1.0.0
