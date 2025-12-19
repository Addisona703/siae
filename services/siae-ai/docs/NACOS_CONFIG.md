# Nacos Configuration for siae-ai Service

## Configuration Overview

This document describes the Nacos configuration setup for the siae-ai service.

## Architecture Changes (v2.0)

The AI service has been refactored with the following key changes:
- **Removed Redis dependency**: Sessions are now stored directly in MySQL
- **Removed Ollama dependency**: Using ZhipuAI as the primary LLM provider
- **Multi-provider support**: Support for multiple LLM providers (ZhipuAI, OpenAI, DeepSeek)
- **Simplified interfaces**: Only streaming chat interface is retained

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

# ===================================================================
# Custom AI Properties - 多供应商配置
# Requirements: 1.1, 1.2, 1.3, 8.2, 8.3
# ===================================================================
siae:
  ai:
    # 默认供应商
    default-provider: zhipu
    
    # 供应商配置
    providers:
      # 智谱 AI 配置（默认供应商）
      zhipu:
        api-key: ${ZHIPU_API_KEY:}
        base-url: https://open.bigmodel.cn/api/paas/v4
        display-name: 智谱AI
        models:
          - glm-4-flash
          - glm-4
          - glm-4-plus
        default-model: glm-4-flash
      
      # OpenAI 配置（可选）
      openai:
        api-key: ${OPENAI_API_KEY:}
        base-url: https://api.openai.com/v1
        display-name: OpenAI
        models:
          - gpt-3.5-turbo
          - gpt-4
          - gpt-4-turbo
        default-model: gpt-3.5-turbo
      
      # DeepSeek 配置（可选）
      deepseek:
        api-key: ${DEEPSEEK_API_KEY:}
        base-url: https://api.deepseek.com/v1
        display-name: DeepSeek
        models:
          - deepseek-chat
          - deepseek-coder
        default-model: deepseek-chat
    
    # 聊天参数配置
    chat:
      temperature: 0.7
      max-tokens: 2000
      response-timeout: 60
      system-prompt: >
        您是SIAE（学生创新与创业协会）的智能助手，致力于为协会成员和访客提供高效、专业的信息查询和支持服务。
        您将通过智能交互为用户提供各种协会相关数据、统计信息、资源推荐等内容。
        回答应专业，避免重复用户问题。所有回答必须使用Markdown格式，并保持格式一致性。
    
    # 会话配置
    session:
      max-messages: 20
  
  # Security Configuration
  security:
    enabled: true
    jwt:
      enabled: true
      header-name: Authorization
      token-prefix: "Bearer "
    permission:
      cache-enabled: true
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

### Required Variables (至少配置一个供应商)

| Variable | Description | Example |
|----------|-------------|---------|
| `ZHIPU_API_KEY` | 智谱 AI API Key | `xxxxxxxxxxxxxxxx.xxxxxxxxxxxxxxxx` |

### Optional Provider Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `OPENAI_API_KEY` | OpenAI API Key | `sk-xxxxxxxxxxxxx` |
| `DEEPSEEK_API_KEY` | DeepSeek API Key | `sk-xxxxxxxxxxxxx` |

### Infrastructure Variables

| Variable | Description | Default |
|----------|-------------|---------|
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
1. Verify the appropriate API key environment variable is set (e.g., `ZHIPU_API_KEY`, `OPENAI_API_KEY`)
2. Check that the API key is valid and has not expired
3. Ensure the provider is correctly configured in `siae.ai.providers`
4. Verify the default-provider setting matches an available provider

### Service Not Registering with Nacos

**Problem:** Service not appearing in Nacos service list

**Solution:**
1. Verify `spring.cloud.nacos.discovery.enabled=true`
2. Check Nacos server address is correct
3. Ensure network connectivity to Nacos server
4. Check application name is set correctly

## References

- [Spring Cloud Alibaba Nacos Config](https://github.com/alibaba/spring-cloud-alibaba/wiki/Nacos-config)
- [智谱 AI 开放平台](https://open.bigmodel.cn/)
- [OpenAI API Documentation](https://platform.openai.com/docs/api-reference)
- [DeepSeek API Documentation](https://platform.deepseek.com/api-docs)

---

**Document Version:** v3.0  
**Last Updated:** 2024-12-20  
**Maintainer:** SIAE Development Team
