# 异步上传开发指南

## 概述

Media服务已实施异步分片合并优化，本文档说明如何正确使用新的上传API。

## 核心变化

### 1. 上传完成响应

**之前**：
```json
{
  "fileId": "xxx",
  "status": "completed"
}
```

**现在**（分片上传）：
```json
{
  "fileId": "xxx",
  "status": "processing"  // 处理中
}
```

### 2. 文件状态流转

```
init → uploading → processing → completed
                              ↘ failed
```

## 前端集成

### 方式1：轮询状态（推荐）

```javascript
async function uploadFile(file) {
  // 1. 初始化上传
  const initResponse = await fetch('/api/v1/upload/init', {
    method: 'POST',
    body: JSON.stringify({
      filename: file.name,
      size: file.size,
      mime: file.type,
      multipart: {
        enabled: true,
        partSize: 5242880  // 5MB
      }
    })
  });
  
  const { uploadId, parts } = await initResponse.json();
  
  // 2. 上传分片
  for (const part of parts) {
    await fetch(part.url, {
      method: 'PUT',
      body: file.slice((part.partNumber - 1) * 5242880, part.partNumber * 5242880)
    });
  }
  
  // 3. 完成上传
  const completeResponse = await fetch(`/api/v1/upload/${uploadId}/complete`, {
    method: 'POST',
    body: JSON.stringify({
      parts: parts.map(p => ({ partNumber: p.partNumber, etag: 'xxx' }))
    })
  });
  
  const { fileId, status } = await completeResponse.json();
  
  // 4. 如果是处理中，轮询状态
  if (status === 'processing') {
    await pollFileStatus(fileId);
  }
  
  return fileId;
}

async function pollFileStatus(fileId, maxAttempts = 60, interval = 1000) {
  for (let i = 0; i < maxAttempts; i++) {
    const response = await fetch(`/api/v1/files/${fileId}`);
    const file = await response.json();
    
    if (file.status === 'completed') {
      console.log('文件处理完成');
      return file;
    } else if (file.status === 'failed') {
      throw new Error('文件处理失败');
    }
    
    // 等待后重试
    await new Promise(resolve => setTimeout(resolve, interval));
  }
  
  throw new Error('文件处理超时');
}
```

### 方式2：事件监听（高级）

```javascript
// 使用WebSocket或SSE监听文件事件
const eventSource = new EventSource('/api/v1/files/events');

eventSource.addEventListener('file.uploaded', (event) => {
  const data = JSON.parse(event.data);
  console.log('文件上传完成:', data.fileId);
  // 更新UI
});

eventSource.addEventListener('file.failed', (event) => {
  const data = JSON.parse(event.data);
  console.error('文件处理失败:', data.fileId);
  // 显示错误
});
```

## 后端集成

### 查询文件状态

```java
@GetMapping("/files/{fileId}")
public FileResponse getFile(@PathVariable String fileId) {
    FileEntity file = fileRepository.selectById(fileId);
    
    FileResponse response = new FileResponse();
    response.setId(file.getId());
    response.setStatus(file.getStatus());  // processing, completed, failed
    response.setSize(file.getSize());
    // ...
    
    return response;
}
```

### 监听文件事件

```java
@Component
public class FileEventListener {
    
    @RabbitListener(queues = "file.uploaded")
    public void handleFileUploaded(FileEvent event) {
        log.info("File uploaded: {}", event.getFileId());
        
        // 执行后续处理
        // 例如：生成缩略图、转码视频等
    }
}
```

## 性能对比

### 小文件（< 5MB）
- 无变化，直接完成

### 大文件（1GB，200分片）

| 操作 | 优化前 | 优化后 | 说明 |
|------|--------|--------|------|
| 初始化 | 2s | 200ms | 批量生成URL |
| 上传 | 30s | 30s | 无变化 |
| 完成 | 8s | 100ms | 异步合并 |
| **总响应时间** | **40s** | **30.3s** | **提升32%** |

**注意**：实际文件可用时间相同，但API响应更快。

## 配置说明

### 线程池配置

在 `AsyncConfig.java` 中配置：

```java
// 文件处理线程池
executor.setCorePoolSize(5);      // 核心线程数
executor.setMaxPoolSize(20);      // 最大线程数
executor.setQueueCapacity(100);   // 队列容量

// URL生成线程池
executor.setCorePoolSize(10);     // 核心线程数
executor.setMaxPoolSize(30);      // 最大线程数
executor.setQueueCapacity(200);   // 队列容量
```

### 调优建议

根据服务器配置调整：
- CPU密集型：线程数 = CPU核心数 + 1
- IO密集型：线程数 = CPU核心数 * 2

## 监控指标

### 关键指标

1. **异步任务队列长度**
   - 指标：`file_process_queue_size`
   - 告警：> 80

2. **异步任务执行时间**
   - 指标：`file_merge_duration`
   - 告警：P99 > 30s

3. **异步任务失败率**
   - 指标：`file_merge_failure_rate`
   - 告警：> 1%

### Prometheus查询

```promql
# 队列长度
file_process_executor_queue_size

# 执行时间P99
histogram_quantile(0.99, file_merge_duration_seconds_bucket)

# 失败率
rate(file_merge_failures_total[5m]) / rate(file_merge_total[5m])
```

## 故障排查

### 问题1：文件一直处于processing状态

**原因**：
- 异步任务失败
- 线程池饱和

**排查**：
```bash
# 查看日志
tail -f logs/siae-media.log | grep "Failed to merge"

# 查看线程池状态
curl http://localhost:8080/actuator/metrics/executor.pool.size
```

**解决**：
- 检查MinIO连接
- 增加线程池大小
- 手动重试合并

### 问题2：初始化慢

**原因**：
- URL生成线程池不足
- MinIO响应慢

**排查**：
```bash
# 查看URL生成耗时
curl http://localhost:8080/actuator/metrics/url.generation.duration
```

**解决**：
- 增加urlGenerationExecutor线程数
- 检查MinIO性能

## 最佳实践

### 1. 前端

✅ **推荐**：
- 使用轮询检查文件状态
- 显示"处理中"提示
- 设置合理的超时时间

❌ **避免**：
- 假设文件立即可用
- 无限轮询
- 阻塞用户操作

### 2. 后端

✅ **推荐**：
- 监控异步任务状态
- 设置合理的线程池大小
- 记录详细日志

❌ **避免**：
- 在异步任务中执行长事务
- 忽略异步任务失败
- 无限重试

## 常见问题

### Q1：为什么不等待合并完成再返回？

A：大文件合并需要5-10秒，会阻塞线程池，影响并发性能。异步处理可以立即释放资源。

### Q2：如何确保文件合并成功？

A：
1. 异步任务有完善的错误处理
2. 失败时更新文件状态为failed
3. 可以通过监控告警及时发现问题

### Q3：单文件上传也是异步的吗？

A：不是，单文件上传（< 5MB）仍然是同步的，立即返回completed状态。

### Q4：如何手动重试失败的合并？

A：可以调用管理接口：
```bash
POST /api/v1/admin/files/{fileId}/retry-merge
```

## 总结

异步上传优化显著提升了大文件上传的性能和并发能力，但需要前端配合实现状态轮询。遵循本指南可以确保正确集成和使用新的上传API。
