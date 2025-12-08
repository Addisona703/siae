# Feign 配置说明

## 📚 配置层级

SIAE 项目的 Feign 配置分为三层：

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Web-Starter 全局配置 (所有服务)                           │
│    ├── FeignAuthenticationInterceptor (认证拦截器)           │
│    └── FeignConfig (注册拦截器 + 日志级别)                   │
└─────────────────────────────────────────────────────────────┘
                    ↓ 继承
┌─────────────────────────────────────────────────────────────┐
│ 2. API 模块配置 (特定 API 包)                                │
│    ├── UserApiFeignConfig                                    │
│    ├── ContentApiFeignConfig                                 │
│    ├── ResultUnwrapDecoder (解包 Result)                    │
│    └── ErrorDecoder (错误处理)                               │
└─────────────────────────────────────────────────────────────┘
                    ↓ 应用到
┌─────────────────────────────────────────────────────────────┐
│ 3. Feign Client (具体接口)                                   │
│    ├── UserFeignClient                                       │
│    ├── MembershipFeignClient                                 │
│    └── ContentFeignClient                                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 组件说明

### 1. FeignAuthenticationInterceptor

**位置**：`siae-web-starter/config`  
**作用**：为所有 Feign 调用自动添加认证头

**添加的请求头**：
- `X-Internal-Service-Call`：内部服务调用密钥
- `X-Caller-Service`：调用方服务名
- `X-Call-Timestamp`：调用时间戳
- `X-On-Behalf-Of-User`：代表用户ID（如果有）

**使用场景**：
- 服务间调用认证
- 传递用户上下文
- 审计和日志追踪

**示例**：
```java
// 自动添加，无需手动配置
userFeignClient.getUser(1L);
// 请求头会自动包含：
// X-Internal-Service-Call: your-secret-key
// X-Caller-Service: siae-auth
// X-Call-Timestamp: 1234567890
```

---

### 2. FeignConfig

**位置**：`siae-web-starter/config`  
**作用**：
1. 注册 `FeignAuthenticationInterceptor` 为全局拦截器
2. 配置全局默认的 Feign 日志级别

**日志级别**：
- `NONE`：不记录任何日志
- `BASIC`：记录请求方法、URL、响应状态码和执行时间（默认）
- `HEADERS`：在 BASIC 基础上增加请求和响应头
- `FULL`：记录所有细节，包括请求体和响应体

**配置方式**：

在 `application.yml` 中配置：
```yaml
siae:
  feign:
    log-level: BASIC  # NONE, BASIC, HEADERS, FULL
```

**推荐配置**：
- 开发环境：`FULL` 或 `HEADERS`
- 测试环境：`BASIC`
- 生产环境：`BASIC` 或 `NONE`

---

### 3. API 模块配置

**位置**：各个 API 模块（如 `siae-user-api`、`siae-content-api`）  
**作用**：配置特定 API 的解码器和错误处理

**组件**：
- `ResultUnwrapDecoder`：自动解包 `Result<T>` 对象（来自 siae-core）
- `ErrorDecoder`：自定义错误处理

**示例**：
```java
@Configuration
public class UserApiFeignConfig {
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return new UserApiErrorDecoder();
    }
    
    @Bean
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new ResultUnwrapDecoder(new SpringDecoder(messageConverters));
    }
}
```

---

## 🚀 使用指南

### 场景 1：创建新的 API 模块

1. 创建 API 模块配置类：
```java
@Configuration
public class NewApiFeignConfig {
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return new NewApiErrorDecoder();
    }
    
    @Bean
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new ResultUnwrapDecoder(new SpringDecoder(messageConverters));
    }
}
```

2. 定义 Feign Client：
```java
@FeignClient(
    name = "service-name",
    path = "/api",
    configuration = NewApiFeignConfig.class  // 指定配置类
)
public interface NewFeignClient {
    @GetMapping("/data")
    DataVO getData();
}
```

### 场景 2：调整日志级别

在服务的 `application.yml` 中：
```yaml
# 开发环境
siae:
  feign:
    log-level: FULL

# 生产环境
siae:
  feign:
    log-level: BASIC
```

### 场景 3：自定义拦截器

如果需要添加额外的拦截器：
```java
@Configuration
public class CustomFeignConfig {
    
    @Bean
    public RequestInterceptor customInterceptor() {
        return template -> {
            // 添加自定义请求头
            template.header("X-Custom-Header", "value");
        };
    }
}
```

---

## ⚠️ 注意事项

1. **不要在 API 模块中配置日志级别**
   - 日志级别由 `FeignConfig` 统一管理
   - API 模块只配置解码器和错误处理

2. **认证拦截器是全局的**
   - 所有 Feign 调用都会自动添加认证头
   - 无需在每个 API 模块中重复配置

3. **配置优先级**
   - Feign Client 的 `configuration` 属性 > API 模块配置 > 全局配置

4. **日志性能影响**
   - `FULL` 级别会记录请求体和响应体，影响性能
   - 生产环境建议使用 `BASIC` 或 `NONE`

---

## 🔍 调试技巧

### 查看 Feign 请求日志

1. 设置日志级别为 `FULL`：
```yaml
siae:
  feign:
    log-level: FULL
```

2. 启用 Feign 日志：
```yaml
logging:
  level:
    com.hngy.siae: DEBUG
```

### 验证认证头

在 Provider 服务中添加日志：
```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public Result<UserVO> getUser(
        @PathVariable Long id,
        @RequestHeader("X-Internal-Service-Call") String secretKey,
        @RequestHeader("X-Caller-Service") String caller
    ) {
        log.info("Received call from: {}, secret: {}", caller, secretKey);
        // ...
    }
}
```

---

## 📖 相关文档

- [ResultUnwrapDecoder 使用说明](../../siae-core/src/main/java/com/hngy/siae/core/feign/README.md)
- [Feign API 包设计文档](../../../.kiro/specs/feign-api-package/design.md)
