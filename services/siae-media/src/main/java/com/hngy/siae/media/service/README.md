# Service 层结构说明

Media Service 的业务逻辑层按功能模块组织，每个子包负责特定的业务领域。

## 包结构

```
service/
├── audit/              # 审计服务
│   └── AuditService.java
├── file/               # 文件管理服务
│   └── FileService.java
├── lifecycle/          # 生命周期管理服务
│   └── LifecycleService.java
├── media/              # 媒体处理服务
│   └── MediaProcessService.java
├── quota/              # 配额管理服务
│   └── QuotaService.java
├── scan/               # 文件扫描服务
│   └── ScanService.java
├── sign/               # 签名和下载服务
│   ├── SignService.java
│   ├── StreamingService.java
│   └── DownloadTokenCleanupService.java
└── upload/             # 上传服务
    ├── UploadService.java
    └── UploadCleanupService.java
```

## 模块说明

### 1. audit - 审计服务
**职责**：记录和查询系统操作审计日志

**主要功能**：
- 记录文件上传、下载、删除等操作
- 记录签名生成和访问
- 提供审计日志查询和导出
- 实现日志脱敏和隐私保护

**核心类**：
- `AuditService` - 审计日志服务

### 2. file - 文件管理服务
**职责**：文件元数据的增删改查

**主要功能**：
- 文件查询（支持多条件筛选、分页）
- 文件元数据更新
- 文件软删除和恢复
- 文件标签和扩展属性管理

**核心类**：
- `FileService` - 文件管理服务

### 3. lifecycle - 生命周期管理服务
**职责**：文件生命周期策略执行

**主要功能**：
- 基于策略的文件归档
- 自动删除过期文件
- 冷热数据分层存储
- 生命周期操作通知

**核心类**：
- `LifecycleService` - 生命周期管理服务

### 4. media - 媒体处理服务
**职责**：媒体文件的处理和转换

**主要功能**：
- 图片缩略图生成（多尺寸）
- 视频缩略图和预览生成
- 音频波形图生成
- 文档预览生成
- 支持多种文件格式

**核心类**：
- `MediaProcessService` - 媒体处理服务

**支持的格式**：
- 图片：所有 `image/*` 类型
- 视频：所有 `video/*` 类型
- 音频：所有 `audio/*` 类型
- 文档：PDF、Office、OpenDocument、文本等

### 5. quota - 配额管理服务
**职责**：租户存储配额管理

**主要功能**：
- 配额检查和限制
- 存储使用量统计
- 配额告警（80%、90%阈值）
- 配额更新和查询

**核心类**：
- `QuotaService` - 配额管理服务

### 6. scan - 文件扫描服务
**职责**：文件安全扫描和内容审核

**主要功能**：
- 病毒扫描（预留 ClamAV 集成）
- 内容安全审核
- 文件安全标记
- 扫描结果记录

**核心类**：
- `ScanService` - 文件扫描服务

### 7. sign - 签名和下载服务
**职责**：文件下载签名和访问控制

**主要功能**：
- 生成下载签名 URL
- ACL 权限验证
- 下载令牌管理（IP 绑定、单次使用）
- 流式播放支持
- 过期令牌清理

**核心类**：
- `SignService` - 签名生成和验证服务
- `StreamingService` - 流式播放服务
- `DownloadTokenCleanupService` - 令牌清理服务

### 8. upload - 上传服务
**职责**：文件上传流程管理

**主要功能**：
- 上传初始化
- 预签名 URL 生成
- 分片上传支持
- 上传完成确认
- 过期上传清理

**核心类**：
- `UploadService` - 上传管理服务
- `UploadCleanupService` - 上传清理服务

## 服务间依赖关系

```
UploadService
    ├─> QuotaService (配额检查)
    ├─> StorageService (生成预签名 URL)
    ├─> EventPublisher (发布上传事件)
    └─> AuditService (记录审计日志)

FileService
    ├─> QuotaService (配额更新)
    └─> AuditService (记录审计日志)

SignService
    ├─> StorageService (生成下载 URL)
    └─> AuditService (记录审计日志)

MediaProcessService
    └─> FileDerivativeRepository (保存衍生文件)

ScanService
    └─> FileRepository (更新扫描结果)

LifecycleService
    ├─> FileRepository (查询和更新文件)
    └─> StorageService (删除对象)
```

## 设计原则

1. **单一职责**：每个服务类只负责一个业务领域
2. **依赖注入**：通过构造函数注入依赖
3. **事务管理**：关键操作使用 `@Transactional` 注解
4. **异常处理**：抛出明确的业务异常，由统一异常处理器处理
5. **日志记录**：使用 Slf4j 记录关键操作和错误
6. **幂等性**：支持重试的操作需要保证幂等性

## 扩展指南

### 添加新的服务模块

1. 在 `service/` 下创建新的子包
2. 创建服务类，使用 `@Service` 注解
3. 实现业务逻辑
4. 编写单元测试
5. 更新本 README 文档

### 示例

```java
package com.hngy.siae.media.service.newmodule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewModuleService {
    
    private final SomeDependency dependency;
    
    public void doSomething() {
        log.info("Doing something...");
        // 业务逻辑
    }
}
```

## 相关文档

- [Worker 层说明](../../worker/README.md)
- [支持的文件格式](../../../../../SUPPORTED_FORMATS.md)
- [部署指南](../../../../../DEPLOYMENT.md)
