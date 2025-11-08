# SIAE Media Service

媒体服务 - 文件上传与分发平台

## 概述

Media Service 是一个企业级的文件上传与分发平台，为软件协会官网的多个业务模块提供统一的媒体文件管理能力。

## 核心功能

- **文件上传**：支持单文件和分片上传，提供预签名 URL 直传对象存储
- **文件管理**：文件查询、元数据管理、软删除和恢复
- **访问控制**：基于 ACL 的权限验证，生成带有效期的签名 URL
- **异步处理**：病毒扫描、缩略图生成、内容审核
- **配额管理**：租户级存储配额和对象数量限制
- **生命周期管理**：文件归档、自动删除、冷热数据分层
- **审计日志**：完整的操作审计和日志记录

## 技术栈

- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- MyBatis Plus 3.5.6
- MinIO (S3 兼容)
- MySQL 8.0
- Redis
- Kafka/RabbitMQ
- Flyway

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- MinIO 或 S3 兼容存储

### 本地开发

1. 启动依赖服务（MySQL、Redis、MinIO、Nacos）

2. 配置数据库
```sql
CREATE DATABASE siae_media CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. 配置 MinIO
```bash
# 创建 bucket
mc mb local/siae-media
```

4. 启动服务
```bash
mvn spring-boot:run
```

5. 访问 API 文档
```
http://localhost:8084/swagger-ui.html
```

## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/siae_media
    username: root
    password: root
```

### MinIO 配置
```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: siae-media
```

### 媒体服务配置
```yaml
media:
  upload:
    max-file-size: 104857600  # 100MB
    max-multipart-size: 5368709120  # 5GB
  download:
    presigned-url-expiry: 900  # 15 minutes
```

## API 接口

### 上传接口
- `POST /api/v1/media/uploads:init` - 初始化上传
- `POST /api/v1/media/uploads/{upload_id}:complete` - 完成上传
- `POST /api/v1/media/uploads/{upload_id}:abort` - 中断上传

### 文件管理
- `GET /api/v1/media/files` - 查询文件列表
- `GET /api/v1/media/files/{file_id}` - 获取文件详情
- `PATCH /api/v1/media/files/{file_id}` - 更新文件元数据
- `DELETE /api/v1/media/files/{file_id}` - 删除文件

### 下载接口
- `POST /api/v1/media/files/{file_id}:sign` - 生成下载签名

## 项目结构

```
siae-media/
├── src/main/java/com/hngy/siae/media/
│   ├── controller/              # REST API 控制器
│   ├── service/                 # 业务服务层
│   │   ├── upload/              # 上传相关服务
│   │   ├── file/                # 文件管理服务
│   │   ├── sign/                # 签名服务
│   │   └── worker/              # 异步处理服务
│   ├── repository/              # 数据访问层
│   ├── domain/                  # 领域模型
│   │   ├── entity/              # 实体类
│   │   ├── dto/                 # 数据传输对象
│   │   └── enums/               # 枚举类
│   ├── infrastructure/          # 基础设施层
│   │   ├── storage/             # 存储适配器
│   │   ├── messaging/           # 消息队列适配器
│   │   └── security/            # 安全组件
│   └── config/                  # 配置类
├── src/main/resources/
│   ├── application.yml          # 应用配置
│   ├── application-dev.yml      # 开发环境配置
│   ├── application-prod.yml     # 生产环境配置
│   └── db/migration/            # Flyway 迁移脚本
└── src/test/                    # 测试代码
```

## 监控和运维

### 健康检查
```
GET /actuator/health
```

### 指标监控
```
GET /actuator/metrics
GET /actuator/prometheus
```

## 开发指南

详细的开发指南请参考：
- [需求文档](../../../.specs/media/requirements.md)
- [设计文档](../../../.specs/media/design.md)
- [任务列表](../../../.specs/media/tasks.md)

## 许可证

Copyright © 2025 SIAE Team
