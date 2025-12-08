# SIAE Feign Starter

为微服务提供统一的 Feign 客户端配置与解码器。

## 功能特性

- **ResultUnwrapDecoder**: 自动解包 `Result<T>` 对象，Feign Client 可以直接返回 `T` 类型
- **ResultErrorDecoder**: 自动将 Provider 返回的错误响应转换为 `BusinessException`
- **自动配置**: 基于 Spring Boot 自动配置，开箱即用

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.hngy</groupId>
    <artifactId>siae-feign-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 配置属性（可选）

```yaml
siae:
  feign:
    enabled: true           # 是否启用 Feign 自动配置，默认 true
    unwrap-result: true     # 是否启用 Result 解包解码器，默认 true
    error-decoder: true     # 是否启用 Result 错误解码器，默认 true
    log-level: BASIC        # Feign 日志级别：NONE, BASIC, HEADERS, FULL
    connect-timeout: 5000   # 连接超时时间（毫秒）
    read-timeout: 10000     # 读取超时时间（毫秒）
```

### 3. 使用示例

```java
// Provider 端返回 Result<UserVO>
@GetMapping("/users/{id}")
public Result<UserVO> getUser(@PathVariable Long id) {
    return Result.success(userService.getById(id));
}

// Consumer 端 Feign Client 可以直接返回 UserVO
@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/users/{id}")
    UserVO getUser(@PathVariable Long id);
}
```

## 组件说明

### ResultUnwrapDecoder

自动解包 `Result<T>` 对象，提取其中的 `data` 字段。

**工作流程：**
```
Provider 返回: Result<UserVO>
    ↓
ResultUnwrapDecoder 解包
    ↓
Feign Client 返回: UserVO
```

### ResultErrorDecoder

自动将 Provider 返回的错误响应转换为业务异常。

**工作流程：**
```
Provider 返回错误: Result(code=404, message="用户不存在")
    ↓
ResultErrorDecoder 解析
    ↓
转换为 BusinessException(404, "用户不存在")
```

## 注意事项

1. 本模块依赖 `siae-core` 模块中的 `Result`、`BusinessException`、`ServiceException` 类
2. 如果需要自定义解码器，可以通过定义自己的 `Decoder` 或 `ErrorDecoder` Bean 来覆盖默认配置
3. 日志级别建议在开发环境使用 `FULL`，生产环境使用 `BASIC` 或 `NONE`
