# Media服务性能分析与优化建议

## 当前架构分析

### 1. 分片上传流程

```
前端 → Media服务(初始化) → 返回预签名URL
前端 → MinIO(直传分片) → 上传完成
前端 → Media服务(完成) → 合并分片
```

### 2. 性能瓶颈分析

#### ✅ 优点

1. **预签名URL直传**
   - 文件不经过Media服务
   - 直接上传到MinIO
   - 减轻服务器压力

2. **分片并行上传**
   - 前端可以并行上传多个分片
   - 充分利用带宽

3. **数据库操作简单**
   - 只记录元数据
   - 不存储文件内容

#### ⚠️ 潜在瓶颈

1. **初始化阶段**
   ```java
   // 为每个分片生成预签名URL
   for (int i = 1; i <= upload.getTotalParts(); i++) {
       String url = storageService.generatePartUploadUrl(...);
       // 每次调用MinIO API
   }
   ```
   - **问题**：大文件（如1GB，200个分片）需要200次MinIO API调用
   - **耗时**：每次5-10ms，总计1-2秒
   - **高并发影响**：100个并发用户 = 20,000次MinIO调用

2. **完成阶段**
   ```java
   // 合并分片
   storageService.composeObject(bucket, partKeys, targetKey);
   // 删除临时分片
   storageService.deleteObjects(bucket, partKeys);
   ```
   - **问题**：合并操作是同步的，阻塞请求
   - **耗时**：200个分片合并需要5-10秒
   - **高并发影响**：阻塞线程池

3. **数据库事务**
   ```java
   @Transactional(rollbackFor = Exception.class)
   public UploadCompleteResponse completeUpload(...) {
       // 长事务，包含文件合并操作
   }
   ```
   - **问题**：事务时间过长
   - **影响**：数据库连接占用

---

## 性能测试数据（预估）

### 场景1：小文件上传（< 5MB）

| 指标 | 单用户 | 10并发 | 100并发 |
|------|--------|--------|---------|
| 初始化耗时 | 50ms | 100ms | 500ms |
| 上传耗时 | 1s | 1s | 1s |
| 完成耗时 | 100ms | 200ms | 1s |
| **总耗时** | **1.15s** | **1.3s** | **2.5s** |

### 场景2：大文件分片上传（1GB，200分片）

| 指标 | 单用户 | 10并发 | 100并发 |
|------|--------|--------|---------|
| 初始化耗时 | 2s | 5s | 20s ⚠️ |
| 上传耗时 | 30s | 30s | 30s |
| 完成耗时 | 8s | 15s | 60s ⚠️ |
| **总耗时** | **40s** | **50s** | **110s** |

**结论**：
- ✅ 小文件性能良好
- ⚠️ 大文件高并发时性能下降明显
- ❌ 100并发时初始化和完成阶段成为瓶颈

---

## 优化方案

### 方案1：批量生成预签名URL ⭐⭐⭐⭐⭐

**问题**：逐个生成URL效率低

**优化**：
```java
// 优化前：循环调用
for (int i = 1; i <= totalParts; i++) {
    String url = storageService.generatePartUploadUrl(...);
}

// 优化后：批量生成
public List<String> batchGeneratePartUploadUrls(
        String bucket, String objectKey, int totalParts, int expirySeconds) {
    
    // 使用线程池并行生成
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<CompletableFuture<String>> futures = new ArrayList<>();
    
    for (int i = 1; i <= totalParts; i++) {
        final int partNumber = i;
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucket)
                    .object(buildPartObjectKey(objectKey, partNumber))
                    .expiry(expirySeconds, TimeUnit.SECONDS)
                    .build()
            );
        }, executor);
        futures.add(future);
    }
    
    // 等待所有URL生成完成
    return futures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList());
}
```

**效果**：
- 200个分片：从2秒降低到200ms（10倍提升）
- 100并发：从20秒降低到2秒（10倍提升）

---

### 方案2：异步合并分片 ⭐⭐⭐⭐⭐

**问题**：合并操作阻塞请求

**优化**：
```java
@Transactional(rollbackFor = Exception.class)
public UploadCompleteResponse completeUpload(String uploadId, UploadCompleteRequest request) {
    // 1. 快速验证和保存元数据
    Upload upload = uploadRepository.selectById(uploadId);
    FileEntity fileEntity = fileRepository.selectById(upload.getFileId());
    
    validateAndSaveParts(upload, request);
    
    // 2. 更新状态为"处理中"
    fileEntity.setStatus(FileStatus.PROCESSING);
    fileRepository.updateById(fileEntity);
    
    upload.setStatus(UploadStatus.PROCESSING);
    uploadRepository.updateById(upload);
    
    // 3. 异步合并分片（不阻塞请求）
    CompletableFuture.runAsync(() -> {
        try {
            mergeMultipartObject(fileEntity, upload);
            
            // 更新状态为"完成"
            fileEntity.setStatus(FileStatus.COMPLETED);
            fileRepository.updateById(fileEntity);
            
            upload.setStatus(UploadStatus.COMPLETED);
            uploadRepository.updateById(upload);
            
            // 发布事件
            publishFileUploadedEvent(fileEntity);
            
        } catch (Exception e) {
            log.error("Failed to merge multipart object", e);
            fileEntity.setStatus(FileStatus.FAILED);
            fileRepository.updateById(fileEntity);
        }
    }, asyncExecutor);
    
    // 4. 立即返回（不等待合并完成）
    UploadCompleteResponse response = new UploadCompleteResponse();
    response.setFileId(fileEntity.getId());
    response.setStatus(FileStatus.PROCESSING);  // 返回"处理中"状态
    return response;
}
```

**效果**：
- 完成接口响应时间：从8秒降低到100ms（80倍提升）
- 用户体验：立即返回，后台处理
- 高并发：不阻塞线程池

---

### 方案3：使用Redis缓存上传会话 ⭐⭐⭐⭐

**问题**：频繁查询数据库

**优化**：
```java
@Service
public class UploadService {
    
    @Autowired
    private RedisTemplate<String, Upload> redisTemplate;
    
    private static final String UPLOAD_CACHE_KEY = "upload:session:";
    private static final int CACHE_TTL = 3600; // 1小时
    
    public UploadInitResponse initUpload(UploadInitRequest request) {
        // 创建上传会话
        Upload upload = createUploadSession(request, fileEntity.getId());
        
        // 保存到数据库
        uploadRepository.insert(upload);
        
        // 缓存到Redis
        String cacheKey = UPLOAD_CACHE_KEY + upload.getUploadId();
        redisTemplate.opsForValue().set(cacheKey, upload, CACHE_TTL, TimeUnit.SECONDS);
        
        return response;
    }
    
    public UploadCompleteResponse completeUpload(String uploadId, UploadCompleteRequest request) {
        // 先从Redis获取
        String cacheKey = UPLOAD_CACHE_KEY + uploadId;
        Upload upload = redisTemplate.opsForValue().get(cacheKey);
        
        if (upload == null) {
            // 缓存未命中，从数据库查询
            upload = uploadRepository.selectById(uploadId);
        }
        
        // ... 处理逻辑
    }
}
```

**效果**：
- 查询耗时：从10ms降低到1ms
- 数据库压力：减少50%

---

### 方案4：限流和熔断 ⭐⭐⭐⭐

**问题**：高并发时服务雪崩

**优化**：
```java
@Service
public class UploadService {
    
    // 使用Resilience4j限流
    @RateLimiter(name = "uploadInit", fallbackMethod = "initUploadFallback")
    public UploadInitResponse initUpload(UploadInitRequest request) {
        // 原有逻辑
    }
    
    // 降级方法
    public UploadInitResponse initUploadFallback(UploadInitRequest request, Throwable t) {
        log.warn("Upload init rate limited, returning error");
        throw new RuntimeException("服务繁忙，请稍后重试");
    }
}
```

**配置**：
```yaml
resilience4j:
  ratelimiter:
    instances:
      uploadInit:
        limitForPeriod: 100      # 每秒100个请求
        limitRefreshPeriod: 1s
        timeoutDuration: 5s
```

---

### 方案5：数据库连接池优化 ⭐⭐⭐

**优化配置**：
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50      # 增加连接池大小
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

## 优化后性能预估

### 场景：大文件分片上传（1GB，200分片）

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| **单用户** |
| 初始化 | 2s | 200ms | 10倍 |
| 完成 | 8s | 100ms | 80倍 |
| 总耗时 | 40s | 30.3s | 1.3倍 |
| **100并发** |
| 初始化 | 20s | 2s | 10倍 |
| 完成 | 60s | 1s | 60倍 |
| 总耗时 | 110s | 33s | 3.3倍 |

---

## 实施优先级

### 高优先级（立即实施）⭐⭐⭐⭐⭐

1. **异步合并分片**
   - 影响最大
   - 实施简单
   - 立竿见影

2. **批量生成URL**
   - 大文件必需
   - 实施中等
   - 效果显著

### 中优先级（近期实施）⭐⭐⭐⭐

3. **Redis缓存**
   - 减轻数据库压力
   - 实施简单

4. **限流熔断**
   - 保护服务稳定性
   - 实施简单

### 低优先级（长期优化）⭐⭐⭐

5. **数据库优化**
   - 调整连接池
   - 添加索引

---

## 监控指标

### 关键指标

```java
@Aspect
@Component
public class UploadMetricsAspect {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Around("execution(* com.hngy.siae.media.service.upload.UploadService.initUpload(..))")
    public Object monitorInitUpload(ProceedingJoinPoint pjp) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Object result = pjp.proceed();
            sample.stop(Timer.builder("upload.init.duration")
                .tag("status", "success")
                .register(meterRegistry));
            return result;
        } catch (Exception e) {
            sample.stop(Timer.builder("upload.init.duration")
                .tag("status", "error")
                .register(meterRegistry));
            throw e;
        }
    }
}
```

### 监控面板

- 上传初始化耗时（P50, P95, P99）
- 上传完成耗时（P50, P95, P99）
- 并发上传数
- 失败率
- MinIO API调用次数

---

## 总结

### 当前状态

✅ **小文件（< 5MB）**：性能良好，无需优化  
⚠️ **大文件（> 100MB）**：单用户可用，高并发需优化  
❌ **超大文件（> 1GB）+ 高并发**：需要立即优化

### 推荐方案

1. **立即实施**：异步合并分片 + 批量生成URL
2. **近期实施**：Redis缓存 + 限流熔断
3. **持续优化**：监控 + 调优

### 预期效果

- 大文件上传性能提升 **3-10倍**
- 高并发支持能力提升 **5-10倍**
- 用户体验显著改善
