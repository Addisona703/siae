# SIAE 项目面试准备文档 (深度版)

本文档基于对 `siae` 后端项目的深度代码分析生成，涵盖了架构演进、核心技术栈、业务亮点以及面试常见追问。

## 1. 项目架构与演进 (Architecture & Evolution)

**项目定位**: 基于 **Spring Cloud Alibaba** 的微服务综合管理平台。

### 1.1 架构重构 (Refactoring Story) - **面试杀手锏**
项目经历了一次重要的架构重构（参考 `docs/rebuilding.md`），从早期的 `siae-core` + `siae-common` 双模块架构，演进为 **三层模块化架构**。这是体现你架构设计能力的绝佳案例。

*   **重构前**: 所有公共依赖都堆在 `siae-common`，导致依赖臃肿，不需要 Web 功能的服务也被迫引入 Web 依赖。
*   **重构后**:
    1.  **`siae-core`**: 极致轻量级。只包含 DTO、枚举、异常类、工具类。无 Spring Boot 依赖。
    2.  **`siae-web-starter`**: 封装 Web 通用功能。包含统一响应 (`UnifiedResponseAdvice`)、全局异常处理 (`GlobalExceptionHandler`)、MyBatisPlus 配置。
    3.  **`siae-security-starter`**: 封装安全功能。包含 JWT 过滤器、权限服务 (`PermissionService`)。支持按需装配。

**面试话术**: "我主导/参与了项目的模块化重构。为了解决依赖臃肿和配置分散的问题，我设计了自定义 Starter 体系。利用 Spring Boot 的自动装配机制 (`AutoConfiguration`) 和条件注解 (`@Conditional`)，实现了功能的按需加载。例如，网关服务只需要轻量级的 Core 模块，而业务服务则自动加载 Web 和 Security 模块。"

---

## 2. 核心技术栈详解 (Tech Stack Deep Dive)

### 2.1 Spring Security & 认证授权 (Security)

项目采用了 **网关统一鉴权 + 微服务精细化授权** 的混合模式。

*   **网关层 (Gateway)**:
    *   **技术**: Spring WebFlux (Reactive) + Spring Security。
    *   **核心组件**: `SecurityConfig` (配置链), `GatewayAuthFilter` (全局过滤器)。
    *   **职责**: 负责第一道防线。校验 JWT 签名合法性，解析用户信息并传递给下游服务。
    *   **亮点**: 使用了响应式编程模型 (`Mono/Flux`) 处理高并发请求。

*   **服务层 (Microservices)**:
    *   **技术**: `siae-security-starter`。
    *   **核心组件**: `JwtAuthenticationFilter`, `RedisPermissionServiceImpl`。
    *   **职责**: 负责具体的业务权限校验（如 `@PreAuthorize("hasAuthority('user:read')")`）。
    *   **亮点**:
        *   **多级缓存权限**: 优先查 Redis，Redis 挂了自动降级 (`FallbackPermissionServiceImpl`)，保证服务高可用。
        *   **OAuth2 集成**: 支持 QQ, Gitee, GitHub 第三方登录，使用 Redis 存储 `state` 防止 CSRF 攻击。

### 2.2 依赖注入与 Spring 核心 (Dependency Injection)

你提到的依赖注入 (DI) 在本项目中不仅仅是简单的 `@Autowired`，而是体现在 **自定义 Starter 的设计** 上。

*   **自动装配原理**:
    *   通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册配置类。
    *   使用 `@Configuration` 定义 Bean 源。
    *   使用 `@Bean` 配合 `@ConditionalOnProperty(prefix = "siae.web", name = "enabled")` 实现配置开关。
*   **常用注解**:
    *   `@RequiredArgsConstructor`: Lombok 注解，配合 `final` 字段实现 **构造器注入**。这是 Spring 推荐的最佳实践，比字段注入 (`@Autowired`) 更利于测试和避免循环依赖。
    *   `@RestControllerAdvice`: 用于全局异常处理和统一响应封装。

### 2.3 消息队列 (RabbitMQ)

*   **应用场景**: **解耦** 和 **最终一致性**。
*   **具体案例**: **用户注销 (User Deletion)**。
    *   当用户服务删除用户时，发布 `UserDeletedEvent`。
    *   **RabbitMQ** 广播该消息。
    *   `siae-media`: 监听消息，异步删除该用户上传的所有文件（释放存储空间）。
    *   `siae-notification`: 监听消息，发送“注销成功”通知（邮件/短信）。
    *   `siae-content`: 监听消息，清理用户发布的文章/评论。
*   **面试题**: "如果消息发送失败怎么办？" -> (可回答) 结合本地消息表或 RabbitMQ 的 Confirm 机制保证可靠性。

### 2.4 分布式组件 (Nacos & Seata)

*   **Nacos**:
    *   **配置动态刷新**: 利用 `@RefreshScope` 实现配置修改后无需重启服务。
    *   **环境隔离**: 使用 Namespace/Group 隔离 Dev/Prod 环境。
*   **Seata**:
    *   **AT 模式**: 处理跨服务事务（如发布内容同时增加积分）。利用 Undo Log 实现自动回滚。

### 2.5 存储与缓存 (Storage & Redis)

*   **Redis**:
    *   **缓存**: 缓存用户信息、权限数据、文件 URL。
    *   **限流**: `RateLimitAspect` 使用 Lua 脚本实现滑动窗口限流。
*   **对象存储 (MinIO/OSS)**:
    *   **策略**: 实现了 **预签名 URL (Presigned URL)** 机制。前端上传文件时不经过后端服务器，而是直接传给 OSS，减轻后端带宽压力。后端只负责生成签名。

---

## 3. 业务亮点与面试题 (Highlights & Q&A)

### Q1: 你的项目中是如何处理全局异常的？
**答**: "我们在 `siae-web-starter` 中定义了 `GlobalExceptionHandler`，使用 `@RestControllerAdvice` 捕获所有异常。
1.  对于自定义的 `BusinessException`，返回对应的错误码和消息。
2.  对于 `MethodArgumentNotValidException` (参数校验失败)，提取字段错误信息返回。
3.  对于未知异常，统一包装为 500 错误，并打印堆栈日志（可配置开关）。
这样既保证了前端接收到的数据格式统一 (`Result<T>`)，又避免了敏感堆栈信息泄露给前端。"

### Q2: 既然有了网关鉴权，为什么微服务层还需要 Security Starter？
**答**: "这是一个**纵深防御 (Defense in Depth)** 的设计。
1.  **网关**只负责粗粒度的校验（Token 是否有效、是否过期），它不了解具体的业务逻辑（如'用户A是否有权修改文章B'）。
2.  **微服务**负责细粒度的权限控制（RBAC）。
3.  此外，如果网关被绕过（例如内网攻击），微服务层的安全机制能提供第二道保护。我们的 Starter 还能根据 `spring.application.name` 智能判断是否启用权限校验，对于内部服务可以配置为免鉴权。"

### Q3: 你的自定义 Starter 是怎么实现的？
**答**: "核心是利用 Spring Boot 的 SPI 机制。
1.  定义 `Properties` 类绑定 `application.yaml` 配置。
2.  定义 `AutoConfiguration` 类，使用 `@Bean` 创建组件。
3.  使用 `@ConditionalOnProperty` 等注解控制 Bean 的加载条件。
4.  在 `imports` 文件中注册自动配置类。
这样其他同事在使用时，只需要引入 Maven 依赖并配置简单的开关，就能立刻获得统一的 Web 和安全能力，大大提高了开发效率。"

### Q4: 用户注销涉及那么多服务，如何保证数据一致性？
**答**: "我们采用了**基于消息队列的最终一致性**方案。用户服务删除核心数据后，发送 MQ 消息。媒体、内容、通知等服务作为消费者异步处理清理逻辑。相比于分布式事务，这种方案性能更高，且降低了服务间的耦合度。"

---

## 4. 关键代码位置速查 (Code Map)

*   **安全配置**: `siae-gateway/.../SecurityConfig.java` (WebFlux), `siae-security-starter/.../SecurityAutoConfiguration.java` (Servlet).
*   **JWT 过滤器**: `siae-security-starter/.../JwtAuthenticationFilter.java`.
*   **限流切面**: `siae-attendance/.../RateLimitAspect.java`.
*   **全局异常**: `siae-web-starter/.../GlobalExceptionHandler.java`.
*   **RabbitMQ 消费者**: `siae-notification/.../UserDeletedEventConsumer.java`.
*   **文件服务**: `siae-media/.../FileServiceImpl.java`.

祝面试顺利！
