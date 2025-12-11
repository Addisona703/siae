# Nacos Configuration for siae-ai Service

## Configuration Overview

This document describes the Nacos configuration setup for the siae-ai service.

## Nacos Configuration Files

### 1. Base Configuration (siae-ai.yaml)

**Data ID:** `siae-ai.yaml`  
**Group:** `SIAE_GROUP`  
**Format:** `YAML`

This is the base configuration shared across all environments.

```yaml
# ===================================================================
# SIAE-AI NACOS BASE CONFIGURATION
# Data ID: siae-ai.yaml
# Group: SIAE_GROUP
# ===================================================================

server:
  port: 8086
  servlet:
    context-path: /

spring:
  # AI Configuration - Spring AI OpenAI Compatible
  ai:
    openai:
      # 使用阿里通义千问 (Qwen) - OpenAI兼容接口
      base-url: ${QWEN_BASE_URL:https://dashscope.aliyuncs.com/compatible-mode/v1}
      api-key: ${QWEN_API_KEY:your-api-key-here}
      chat:
        options:
          model: ${QWEN_MODEL:qwen-turbo}
          temperature: 0.7
          max-tokens: 2000

  # Redis Configuration
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms

  # Cloud Configuration
  cloud:
    nacos:
      discovery:
        enabled: true
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:public}
        group: SIAE_GROUP
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}

# Custom AI Properties
siae:
  ai:
    provider: qwen
    api-key: ${QWEN_API_KEY:your-api-key-here}
    model: ${QWEN_MODEL:qwen-turbo}
    base-url: ${QWEN_BASE_URL:https://dashscope.aliyuncs.com/compatible-mode/v1}
    max-tokens: 2000
    temperature: 0.7
    response-timeout: 30
    system-prompt: "你是SIAE（学生创新与创业协会）的智能助手，可以帮助用户查询成员信息、获奖记录等数据。请用简洁、专业的中文回答问题。"
    session:
      max-messages: 20
      timeout-minutes: 30
    retry:
      max-attempts: 3
      initial-interval: 1000
      multiplier: 2.0
      max-interval: 10000
  
  # Security Configuration
  security:
    enabled: true
    jwt:
      enabled: true
      header-name: Authorization
      token-prefix: "Bearer "
    permission:
      cache-enabled: true
      redis-enabled: true
      log-enabled: true
    whitelist-paths:
      - /swagger-ui/**
      - /v3/api-docs/**
      - /actuator/health
      - /actuator/info
  
  # Auth Properties (for ServiceAuthenticationFilter)
  auth:
    enable-gateway-auth: true
    enable-direct-access: false  # 生产环境应设为false
    gateway-secret-key: ${GATEWAY_SECRET_KEY:siae-gateway-secret-2024}
    gateway-secret-valid-seconds: 60
    internal-secret-key: ${INTERNAL_SECRET_KEY:siae-internal-secret-2024}

# Logging
logging:
  level:
    root: INFO
    com.hngy.siae.ai: INFO
    org.springframework.ai: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# SpringDoc OpenAPI
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

### 2. Development Environment Configuration (siae-ai-dev.yaml)

**Data ID:** `siae-ai-dev.yaml`  
**Group:** `SIAE_GROUP`  
**Format:** `YAML`

This configuration is automatically loaded when `spring.profiles.active=dev`.

```yaml
# ===================================================================
# SIAE-AI NACOS DEV CONFIGURATION
# Data ID: siae-ai-dev.yaml
# Group: SIAE_GROUP
# ===================================================================

# Development-specific overrides
logging:
  level:
    root: INFO
    com.hngy.siae.ai: DEBUG
    org.springframework.ai: DEBUG

siae:
  auth:
    enable-direct-access: true  # 开发环境允许直接访问

springdoc:
  swagger-ui:
    enabled: true
```

### 3. Nacos Environment Configuration (siae-ai-nacos.yaml)

**Data ID:** `siae-ai-nacos.yaml`  
**Group:** `SIAE_GROUP`  
**Format:** `YAML`

This configuration is automatically loaded when `spring.profiles.active=nacos`.

```yaml
# ===================================================================
# SIAE-AI NACOS ENVIRONMENT CONFIGURATION
# Data ID: siae-ai-nacos.yaml
# Group: SIAE_GROUP
# ===================================================================

# Nacos environment specific configuration
spring:
  cloud:
    nacos:
      discovery:
        enabled: true
      config:
        enabled: true
        refresh-enabled: true

logging:
  level:
    root: INFO
    com.hngy.siae.ai: INFO
    org.springframework.ai: INFO
```

### 4. Production Environment Configuration (siae-ai-prod.yaml)

**Data ID:** `siae-ai-prod.yaml`  
**Group:** `SIAE_GROUP`  
**Format:** `YAML`

This configuration is automatically loaded when `spring.profiles.active=prod`.

```yaml
# ===================================================================
# SIAE-AI NACOS PROD CONFIGURATION
# Data ID: siae-ai-prod.yaml
# Group: SIAE_GROUP
# ===================================================================

# Production-specific overrides
logging:
  level:
    root: WARN
    com.hngy.siae.ai: INFO
    org.springframework.ai: WARN

siae:
  auth:
    enable-direct-access: false  # 生产环境禁止直接访问

springdoc:
  swagger-ui:
    enabled: false  # 生产环境禁用 Swagger UI

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

## Environment Variables

The following environment variables should be configured in your deployment environment:

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `QWEN_API_KEY` | 阿里通义千问 API Key | `sk-xxxxxxxxxxxxx` |
| `QWEN_BASE_URL` | 通义千问 API Base URL | `https://dashscope.aliyuncs.com/compatible-mode/v1` |
| `QWEN_MODEL` | LLM 模型名称 | `qwen-turbo` |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `REDIS_HOST` | Redis 服务器地址 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | (empty) |
| `NACOS_SERVER_ADDR` | Nacos 服务器地址 | `localhost:8848` |
| `NACOS_USERNAME` | Nacos 用户名 | `nacos` |
| `NACOS_PASSWORD` | Nacos 密码 | `nacos` |
| `NACOS_NAMESPACE` | Nacos 命名空间 | `public` |
| `GATEWAY_SECRET_KEY` | 网关认证密钥 | `siae-gateway-secret-2024` |
| `INTERNAL_SECRET_KEY` | 内部服务认证密钥 | `siae-internal-secret-2024` |

## Configuration Setup Steps

### Step 1: Create Base Configuration in Nacos

1. Login to Nacos Console (http://localhost:8848/nacos)
2. Navigate to **配置管理** → **配置列表**
3. Click **+** to create new configuration
4. Fill in:
   - **Data ID:** `siae-ai.yaml`
   - **Group:** `SIAE_GROUP`
   - **配置格式:** `YAML`
   - **配置内容:** Copy the content from "Base Configuration" section above
5. Click **发布**

### Step 2: Create Environment-Specific Configurations

Repeat Step 1 for each environment configuration:
- `siae-ai-dev.yaml` (for development)
- `siae-ai-nacos.yaml` (for Nacos environment)
- `siae-ai-prod.yaml` (for production)

### Step 3: Configure Environment Variables

Set the required environment variables in your deployment environment or in Nacos configuration.

### Step 4: Verify Configuration

Start the service and check the logs:

```
INFO  - [Nacos Config] Load config[dataId=siae-ai.yaml, group=SIAE_GROUP] success
INFO  - [Nacos Config] Load config[dataId=siae-ai-dev.yaml, group=SIAE_GROUP] success
```

## Switching Environments

### Local Development (without Nacos)

```bash
# Set environment variable
export ENV=dev

# Or in bootstrap.yaml
spring:
  profiles:
    active: dev
```

### Team Collaboration (with Nacos)

```bash
# Set environment variable
export ENV=nacos

# Or in bootstrap.yaml
spring:
  profiles:
    active: nacos
```

### Production

```bash
# Set environment variable
export ENV=prod

# Or in bootstrap.yaml
spring:
  profiles:
    active: prod
```

## Configuration Priority

From highest to lowest priority:

1. Command line arguments
2. Environment variables
3. `application-{profile}.yaml` (local file)
4. Nacos remote configuration
5. `bootstrap.yaml`

## Troubleshooting

### Configuration Not Loading

**Problem:** Nacos configuration not being loaded

**Solution:**
1. Check that `spring.config.import` includes `.yaml` extension
2. Verify Nacos server is running and accessible
3. Check Data ID and Group match exactly
4. Verify configuration format is set to `YAML` in Nacos

### API Key Not Working

**Problem:** LLM API calls failing with authentication error

**Solution:**
1. Verify `QWEN_API_KEY` environment variable is set correctly
2. Check that the API key is valid and has not expired
3. Ensure the base URL is correct for your LLM provider

### Service Not Registering with Nacos

**Problem:** Service not appearing in Nacos service list

**Solution:**
1. Verify `spring.cloud.nacos.discovery.enabled=true`
2. Check Nacos server address is correct
3. Ensure network connectivity to Nacos server
4. Check application name is set correctly

## References

- [Spring Cloud Alibaba Nacos Config](https://github.com/alibaba/spring-cloud-alibaba/wiki/Nacos-config)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [阿里通义千问 API](https://help.aliyun.com/zh/dashscope/)

---

**Document Version:** v1.0  
**Last Updated:** 2024-12-10  
**Maintainer:** SIAE Development Team
