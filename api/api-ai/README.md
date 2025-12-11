# API-AI Module

AI服务API模块，提供AI服务所需的Feign客户端接口和共享DTO。

## 模块结构

```
api-ai/
├── src/main/java/com/hngy/siae/api/ai/
│   ├── client/                    # Feign客户端接口
│   │   └── AiUserFeignClient.java
│   ├── dto/
│   │   └── response/              # 响应DTO
│   │       ├── AwardInfo.java
│   │       ├── AwardStatistics.java
│   │       ├── MemberInfo.java
│   │       └── MemberStatistics.java
│   └── fallback/                  # 降级处理
│       └── AiUserFeignClientFallback.java
└── pom.xml
```

## 使用方式

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.hngy</groupId>
    <artifactId>api-ai</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 启用Feign客户端

在启动类或配置类上添加：

```java
@EnableFeignClients(basePackages = "com.hngy.siae.api.ai.client")
```

### 3. 注入使用

```java
@Autowired
private AiUserFeignClient aiUserFeignClient;

// 查询成员获奖记录
List<AwardInfo> awards = aiUserFeignClient.getAwardsByMember("张三", null);

// 查询成员信息
List<MemberInfo> members = aiUserFeignClient.searchMembers("张", "技术部", null);

// 获取获奖统计
AwardStatistics stats = aiUserFeignClient.getAwardStatistics(null, null, "2024-01-01", "2024-12-31");
```

## 接口说明

### AiUserFeignClient

| 方法 | 路径 | 描述 |
|------|------|------|
| getAwardsByMember | GET /feign/ai/awards/by-member | 根据成员信息查询获奖记录 |
| searchMembers | GET /feign/ai/members/search | 查询成员信息 |
| getAwardStatistics | GET /feign/ai/statistics/awards | 获取获奖统计信息 |
| getMemberStatistics | GET /feign/ai/statistics/members | 获取成员统计信息 |
