# Media Service 包结构说明

## 📦 整体架构

```
com.hngy.siae.media/
├── 📱 MediaServiceApplication.java    # 应用启动类
├── ⚙️  config/                         # 配置层
├── 🎮 controller/                      # 控制器层（API 接口）
├── 📊 domain/                          # 领域模型层
├── 🏗️  infrastructure/                 # 基础设施层
├── 👁️  observability/                  # 可观测性层
├── 💾 repository/                      # 数据访问层
├── 🔒 security/                        # 安全层
├── 🔧 service/                         # 业务服务层
└── ⚡ worker/                          # 异步处理层
```

---

## 📁 详细说明

### ⚙️ config - 配置层
**职责**：应用配置和常量定义

```
config/
├── GracefulShutdownConfig.java      # 优雅关闭配置
├── MediaPermissions.java            # 权限常量定义
├── MediaProperties.java             # 媒体服务属性配置
├── MetricsConfig.java               # 指标监控配置
├── MinioConfig.java                 # MinIO 对象存储配置
├── MyBatisPlusConfig.java           # MyBatis Plus 配置
├── SecurityConfig.java              # Spring Security 配置
└── WebMvcConfig.java                # Web MVC 配置
```

**特点**：
- 集中管理所有配置类
- 包含常量定义（权限、配置属性等）
- 使用 `@Configuration` 注解

---

### 🎮 controller - 控制器层
**职责**：REST API 接口定义

```
controller/
├── AuditController.java             # 审计日志接口
├── DownloadController.java          # 下载签名接口
├── FileController.java              # 文件管理接口
├── QuotaController.java             # 配额管理接口
├── StreamingController.java         # 流式播放接口
└── UploadController.java            # 文件上传接口
```

**特点**：
- 所有 Controller 在同一层级
- 使用 `@RestController` 注解
- 统一的 API 路径前缀：`/api/v1/media`

---

### 📊 domain - 领域模型层
**职责**：业务领域对象定义

```
domain/
├── dto/                             # 数据传输对象
│   ├── file/                        # 文件相关 DTO
│   │   ├── FileQueryRequest.java
│   │   ├── FileInfoResponse.java
│   │   └── FileUpdateRequest.java
│   ├── sign/                        # 签名相关 DTO
│   │   ├── SignRequest.java
│   │   └── SignResponse.java
│   └── upload/                      # 上传相关 DTO
│       ├── UploadInitRequest.java
│       ├── UploadInitResponse.java
│       ├── UploadCompleteRequest.java
│       ├── UploadCompleteResponse.java
│       ├── UploadRefreshRequest.java
│       └── UploadRefreshResponse.java
├── entity/                          # 实体类
│   ├── AuditLog.java
│   ├── DownloadToken.java
│   ├── FileDerivative.java
│   ├── FileEntity.java
│   ├── LifecyclePolicy.java
│   ├── MultipartPart.java
│   ├── ProcessingJob.java
│   ├── Quota.java
│   └── Upload.java
├── enums/                           # 枚举类
│   ├── ActorType.java
│   ├── AuditAction.java
│   ├── FileStatus.java
│   ├── JobStatus.java
│   ├── JobType.java
│   └── UploadStatus.java
└── event/                           # 事件类
    ├── FileEvent.java
    └── FileUploadedEvent.java
```

**优化点**：
- ✅ DTO 按业务模块分组（file、sign、upload）
- ✅ 清晰的职责划分
- ✅ 便于维护和扩展

---

### 🏗️ infrastructure - 基础设施层
**职责**：外部系统集成和基础设施服务

```
infrastructure/
├── messaging/                       # 消息队列
│   ├── EventIdempotency.java       # 事件幂等性
│   └── EventPublisher.java         # 事件发布器
├── security/                        # 安全基础设施（预留）
└── storage/                         # 对象存储
    └── StorageService.java         # MinIO 存储服务
```

**特点**：
- 封装外部依赖
- 提供统一的接口
- 便于切换实现

---

### 👁️ observability - 可观测性层
**职责**：监控、健康检查、指标采集

```
observability/
├── DatabaseHealthIndicator.java     # 数据库健康检查
├── RabbitMQHealthIndicator.java     # RabbitMQ 健康检查
├── RedisHealthIndicator.java        # Redis 健康检查
├── StorageHealthIndicator.java      # 存储健康检查
├── MediaInfoContributor.java        # 服务信息贡献者
└── MediaMetrics.java                # 业务指标工具
```

**优化点**：
- ✅ 合并了 actuator、health、metrics 三个包
- ✅ 统一的可观测性管理
- ✅ 支持 Prometheus、Actuator

**访问端点**：
- `/actuator/health` - 健康检查
- `/actuator/info` - 服务信息
- `/actuator/prometheus` - Prometheus 指标

---

### 💾 repository - 数据访问层
**职责**：数据库访问接口

```
repository/
├── AuditLogRepository.java
├── DownloadTokenRepository.java
├── FileDerivativeRepository.java
├── FileRepository.java
├── LifecyclePolicyRepository.java
├── MultipartPartRepository.java
├── ProcessingJobRepository.java
├── QuotaRepository.java
└── UploadRepository.java
```

**特点**：
- 基于 MyBatis Plus
- 继承 `BaseMapper<T>`
- 支持自定义查询方法

---

### 🔒 security - 安全层
**职责**：认证、授权、审计

```
security/
├── AuditLog.java                    # 审计日志注解
├── AuditLogAspect.java              # 审计日志切面
├── RequirePermission.java           # 权限注解
├── TenantContext.java               # 租户上下文
└── TenantInterceptor.java           # 租户拦截器
```

**优化点**：
- ✅ 合并了 aspect 包
- ✅ 安全相关功能集中管理
- ✅ 包含注解、切面、拦截器

---

### 🔧 service - 业务服务层
**职责**：核心业务逻辑

```
service/
├── README.md                        # 服务层说明文档
├── audit/                           # 审计服务
│   └── AuditService.java
├── file/                            # 文件管理
│   └── FileService.java
├── lifecycle/                       # 生命周期管理
│   └── LifecycleService.java
├── media/                           # 媒体处理
│   └── MediaProcessService.java
├── quota/                           # 配额管理
│   └── QuotaService.java
├── scan/                            # 文件扫描
│   └── ScanService.java
├── sign/                            # 签名和下载
│   ├── SignService.java
│   ├── StreamingService.java
│   └── DownloadTokenCleanupService.java
└── upload/                          # 上传管理
    ├── UploadService.java
    └── UploadCleanupService.java
```

**特点**：
- 按业务模块分包
- 单一职责原则
- 详细的 README 文档

---

### ⚡ worker - 异步处理层
**职责**：后台任务和事件处理

```
worker/
├── FileScanWorker.java              # 文件扫描 Worker
├── LifecycleWorker.java             # 生命周期 Worker
└── MediaProcessWorker.java          # 媒体处理 Worker
```

**特点**：
- 监听 RabbitMQ 消息队列
- 使用 `@SiaeRabbitListener` 注解
- 异步处理耗时任务

---

## 🎯 优化总结

### 优化前的问题
- ❌ 监控相关的包分散（actuator、health、metrics）
- ❌ 配置和常量分离（config、constant）
- ❌ 安全和切面分离（security、aspect）
- ❌ DTO 全部在一个包中，不易管理
- ❌ 存在大量空的 .gitkeep 文件
- ❌ 使用 Kafka 而不是 RabbitMQ

### 优化后的改进
- ✅ 合并监控包为 `observability`
- ✅ 合并配置包，常量放入 `config`
- ✅ 合并安全包，切面放入 `security`
- ✅ DTO 按业务模块分组（file、sign、upload）
- ✅ 删除所有 .gitkeep 文件
- ✅ 更新为 RabbitMQ 健康检查

### 包结构特点
1. **清晰的分层**：每一层职责明确
2. **模块化**：相关功能聚合在一起
3. **可扩展**：易于添加新功能
4. **易维护**：结构清晰，便于定位代码

---

## 📚 相关文档

- [Service 层详细说明](../src/main/java/com/hngy/siae/media/service/README.md)
- [支持的文件格式](SUPPORTED_FORMATS.md)
- [部署指南](DEPLOYMENT.md)

---

## 🔄 包依赖关系

```
Controller → Service → Repository → Entity
    ↓          ↓
  DTO    Infrastructure
    ↓          ↓
Security   Storage/Messaging
    ↓
Observability
```

---

**最后更新**: 2025-01-XX  
**维护人员**: SIAE 开发团队
