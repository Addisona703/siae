# API-Media 模块

媒体服务的 Feign 客户端 API 模块，提供文件查询和 URL 获取相关的远程调用接口。

## 📦 模块说明

本模块是 `siae-media` 服务的 API 包，包含：
- Feign Client 接口定义
- 请求/响应 DTO
- 枚举类型
- Fallback 降级实现

## 🚀 快速开始

### 1. 添加依赖

在需要调用媒体服务的模块中添加依赖：

```xml
<dependency>
    <groupId>com.hngy.siae</groupId>
    <artifactId>api-media</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 启用 Feign 客户端

在启动类上添加注解：

```java
@EnableFeignClients(basePackages = "com.hngy.siae.api.media.client")
@SpringBootApplication
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
    
    private final MediaFeignClient mediaFeignClient;
    
    public void yourMethod() {
        // 获取文件详情
        FileInfoVO fileInfo = mediaFeignClient.getFileById("file-id-123");
        
        // 获取文件访问URL
        String fileUrl = mediaFeignClient.getFileUrl("file-id-123", 3600);
        
        // 批量获取文件URL
        BatchUrlDTO request = new BatchUrlDTO();
        request.setFileIds(Arrays.asList("file-id-1", "file-id-2"));
        request.setExpirySeconds(7200);
        BatchUrlVO result = mediaFeignClient.batchGetFileUrls(request);
    }
}
```

## 📋 可用的 Feign Client

### MediaFeignClient

媒体服务客户端，提供以下接口：

#### 1. 获取文件详情
```java
FileInfoVO getFileById(String fileId)
```
- **参数**: `fileId` - 文件ID
- **返回**: 文件详细信息

#### 2. 获取文件访问URL
```java
String getFileUrl(String fileId, Integer expirySeconds)
```
- **参数**: 
  - `fileId` - 文件ID
  - `expirySeconds` - URL过期时间（秒），默认24小时
- **返回**: 预签名访问URL

#### 3. 批量获取文件URL
```java
BatchUrlVO batchGetFileUrls(BatchUrlDTO request)
```
- **参数**: `request` - 批量请求参数（包含文件ID列表和过期时间）
- **返回**: 文件ID到URL的映射及统计信息

## 🛡️ 降级处理（Fallback）

本模块提供了 Fallback 实现类，用于在服务不可用时执行降级逻辑：

- `MediaFeignClientFallback`: MediaFeignClient 的降级实现

### 启用 Fallback

Fallback 已在 `@FeignClient` 注解中配置：

```java
@FeignClient(
    name = "siae-media",
    path = "/api/v1/media/feign",
    contextId = "mediaFeignClient",
    fallback = MediaFeignClientFallback.class  // 已启用降级
)
```

### 降级策略说明

当媒体服务不可用时：
- 记录错误日志
- 抛出 `ServiceException(503, "媒体服务暂时不可用，请稍后重试")`
- 调用方可以捕获异常并进行相应处理

## 📁 包结构

```
com.hngy.siae.api.media
├── client/              # Feign 客户端接口
│   └── MediaFeignClient.java
├── dto/                 # 数据传输对象
│   ├── request/         # 请求 DTO
│   │   └── BatchUrlDTO.java
│   └── response/        # 响应 DTO
│       ├── FileInfoVO.java
│       └── BatchUrlVO.java
├── enums/               # 枚举类型
│   ├── FileStatus.java
│   └── AccessPolicy.java
└── fallback/            # 降级实现
    └── MediaFeignClientFallback.java
```

## 🔗 相关文档

- [Feign 配置说明](../../packages/siae-web-starter/src/main/java/com/hngy/siae/web/config/FEIGN_README.md)
- [Feign 工具包](../../packages/siae-core/src/main/java/com/hngy/siae/core/feign/README.md)

## 🎯 使用示例

### 示例1：获取文件信息并生成访问链接

```java
@Service
@RequiredArgsConstructor
public class ContentService {
    
    private final MediaFeignClient mediaFeignClient;
    
    public ContentDetailVO getContentWithMedia(Long contentId) {
        // 获取内容信息
        Content content = contentRepository.findById(contentId);
        
        // 获取关联的媒体文件URL
        String mediaUrl = mediaFeignClient.getFileUrl(
            content.getMediaFileId(), 
            3600  // 1小时有效期
        );
        
        // 组装返回数据
        ContentDetailVO vo = new ContentDetailVO();
        vo.setMediaUrl(mediaUrl);
        return vo;
    }
}
```

### 示例2：批量获取文件URL

```java
@Service
@RequiredArgsConstructor
public class GalleryService {
    
    private final MediaFeignClient mediaFeignClient;
    
    public List<ImageVO> getGalleryImages(List<String> fileIds) {
        // 批量获取文件URL
        BatchUrlDTO request = new BatchUrlDTO();
        request.setFileIds(fileIds);
        request.setExpirySeconds(7200);  // 2小时有效期
        
        BatchUrlVO result = mediaFeignClient.batchGetFileUrls(request);
        
        // 转换为业务对象
        return fileIds.stream()
            .map(fileId -> {
                ImageVO vo = new ImageVO();
                vo.setFileId(fileId);
                vo.setUrl(result.getUrls().get(fileId));
                return vo;
            })
            .collect(Collectors.toList());
    }
}
```

## ⚠️ 注意事项

1. **服务依赖**：确保 `siae-media` 服务已启动并可访问
2. **URL有效期**：预签名URL有时效性，建议根据业务场景设置合理的过期时间
3. **批量操作**：批量获取URL时注意文件数量，避免单次请求过大
4. **异常处理**：调用方应捕获 `ServiceException` 并进行适当的错误处理
5. **缓存策略**：对于频繁访问的文件URL，建议在调用方实现缓存机制

## 📝 版本历史

- **v1.0.0** - 初始版本
  - 提供文件查询接口
  - 提供单个/批量URL获取接口
  - 实现降级处理

---

**维护团队**: SIAE Team  
**最后更新**: 2024-11-26
