# siae-user-api

User Service API - Feign Client Interfaces

## 概述

本模块包含 User 服务的 Feign 客户端接口定义、DTO 和相关配置。其他微服务可以通过引入此模块来调用 User 服务的接口。

## 模块结构

```
com.hngy.siae.user.api/
├── client/      # Feign Client 接口
├── dto/         # 数据传输对象
├── enums/       # 枚举类型
├── config/      # Feign 配置
└── fallback/    # 降级处理
```

## 使用方式

### 1. 添加依赖

在需要调用 User 服务的模块中添加以下依赖：

```xml
<dependency>
    <groupId>com.hngy</groupId>
    <artifactId>siae-user-api</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 启用 Feign Clients

在启动类上添加 `@EnableFeignClients` 注解：

```java
@SpringBootApplication
@EnableFeignClients(basePackages = "com.hngy.siae.user.api.client")
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. 注入并使用

```java
@Service
@RequiredArgsConstructor
public class YourService {
    
    private final UserFeignClient userFeignClient;
    
    public void yourMethod() {
        // 调用 User 服务接口
        UserVO user = userFeignClient.getUserByUsername("username");
    }
}
```

## 可用的 Feign Client

- `UserFeignClient`: 用户基础信息查询接口
- `MembershipFeignClient`: 会员信息查询接口

## 降级处理（Fallback）

本模块提供了 Fallback 实现类，用于在服务不可用时执行降级逻辑：

- `UserFeignClientFallback`: UserFeignClient 的降级实现
- `MembershipFeignClientFallback`: MembershipFeignClient 的降级实现

### 启用 Fallback

在 Feign Client 注解中配置 fallback：

```java
@FeignClient(
    name = "siae-user",
    path = "/feign",
    contextId = "userFeignClient",
    fallback = UserFeignClientFallback.class  // 启用降级
)
public interface UserFeignClient {
    // ...
}
```

### 降级策略说明

1. **用户注册、查询等关键操作**：抛出 `ServiceException(503)`，提示服务不可用
2. **用户名/学号存在性检查**：返回 `true`（保守策略，假设已存在，阻止可能的重复注册）
3. **成员身份检查**：返回 `false`（保守策略，假设不是成员，避免授予不应有的权限）
4. **批量查询**：返回空集合，避免空指针异常

所有降级方法都会记录详细的错误日志，便于问题排查。

## 注意事项

1. 确保 Nacos 服务注册中心已启动
2. 确保 User 服务（siae-user）已启动并注册到 Nacos
3. 配置合理的超时时间和重试策略
4. Fallback 类已提供，Consumer 服务可以直接使用或自定义降级实现
5. 降级策略采用保守原则，优先保证数据一致性和安全性
