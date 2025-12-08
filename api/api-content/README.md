# siae-content-api

Content Service API - Feign Client Interfaces

## 概述

本模块包含 Content 服务的 Feign 客户端接口定义、DTO 和相关配置。其他微服务可以通过引入此模块来调用 Content 服务的接口。

## 模块结构

```
com.hngy.siae.content.api/
├── client/      # Feign Client 接口
├── dto/         # 数据传输对象
├── enums/       # 枚举类型
├── config/      # Feign 配置
└── fallback/    # 降级处理
```

## 使用方式

### 1. 添加依赖

在需要调用 Content 服务的模块中添加以下依赖：

```xml
<dependency>
    <groupId>com.hngy</groupId>
    <artifactId>siae-content-api</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 启用 Feign Clients

在启动类上添加 `@EnableFeignClients` 注解：

```java
@SpringBootApplication
@EnableFeignClients(basePackages = "com.hngy.siae.content.api.client")
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
    
    private final ContentFeignClient contentFeignClient;
    
    public void yourMethod() {
        // 调用 Content 服务接口
        ContentVO content = contentFeignClient.queryContent(contentId);
    }
}
```

## 可用的 Feign Client

- `ContentFeignClient`: 内容查询接口
- `CategoryFeignClient`: 分类查询接口
- `TagFeignClient`: 标签查询接口

## 注意事项

1. 确保 Nacos 服务注册中心已启动
2. 确保 Content 服务（siae-content）已启动并注册到 Nacos
3. 配置合理的超时时间和重试策略
4. 使用 Fallback 机制处理服务不可用的情况
