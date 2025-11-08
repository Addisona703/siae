# 优雅关闭（Graceful Shutdown）说明

## 什么是优雅关闭？

优雅关闭是指当应用程序需要停止时，不是立即强制终止所有操作，而是按照以下步骤有序关闭：

```
1. 停止接收新请求 ❌
   ↓
2. 等待正在处理的请求完成 ⏳
   ↓
3. 关闭所有资源连接 🔌
   ↓
4. 最后才真正退出应用 ✅
```

## 为什么需要优雅关闭？

### 没有优雅关闭的问题

❌ **用户体验差**
- 用户正在上传文件，突然中断
- 下载到一半失败
- API 请求返回 500 错误

❌ **数据不一致**
- 数据库事务未提交
- 文件写入一半
- 缓存未更新

❌ **资源泄漏**
- 数据库连接未关闭
- 消息队列消息丢失
- 临时文件未清理

### 有优雅关闭的好处

✅ **用户体验好**
- 正在进行的操作能够完成
- 不会出现突然中断
- 返回正确的响应

✅ **数据一致性**
- 事务正常提交
- 文件完整写入
- 状态正确更新

✅ **资源正确释放**
- 连接池正常关闭
- 消息正常确认
- 临时资源清理

## Media Service 的配置

### 配置文件（application.yml）

```yaml
server:
  port: 8084
  shutdown: graceful  # 启用优雅关闭

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s  # 每个关闭阶段的超时时间
```

### 配置说明

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `server.shutdown` | `graceful` | 启用优雅关闭模式 |
| `timeout-per-shutdown-phase` | `30s` | 最多等待30秒让请求完成 |

## 关闭流程

### 1. 接收关闭信号
```bash
# Docker/Kubernetes 发送 SIGTERM 信号
kill -15 <pid>

# 或者通过 Actuator 端点
POST /actuator/shutdown
```

### 2. 停止接收新请求
- Web 服务器停止接受新连接
- 负载均衡器将流量切走
- 健康检查返回 DOWN 状态

### 3. 等待现有请求完成
```
正在处理的请求：
├── 文件上传请求 (15s) ⏳
├── 文件下载请求 (5s)  ⏳
└── 查询请求 (1s)      ✅ 完成

等待时间：最多 30 秒
```

### 4. 关闭资源
```
关闭顺序：
1. 停止定时任务 (UploadCleanupService, LifecycleWorker)
2. 关闭 RabbitMQ 连接
3. 关闭数据库连接池
4. 关闭 Redis 连接
5. 关闭 MinIO 客户端
```

### 5. 退出应用
```
应用正常退出，返回状态码 0
```

## 实际场景示例

### 场景1：文件上传中

```
时间线：
00:00 - 用户开始上传 100MB 文件
00:05 - 运维人员执行滚动更新
00:05 - 应用收到 SIGTERM 信号
00:05 - 停止接收新请求
00:05 - 继续处理上传请求
00:20 - 上传完成，返回成功响应
00:20 - 应用正常退出
```

✅ **结果**：用户上传成功，无感知

### 场景2：数据库事务中

```
时间线：
00:00 - 开始处理文件元数据更新
00:01 - 数据库事务开始
00:02 - 应用收到关闭信号
00:02 - 等待事务完成
00:03 - 事务提交成功
00:03 - 应用正常退出
```

✅ **结果**：数据一致性保证

### 场景3：超时情况

```
时间线：
00:00 - 用户开始上传大文件
00:05 - 应用收到关闭信号
00:35 - 超过30秒超时时间
00:35 - 强制关闭应用
```

⚠️ **结果**：超时后强制关闭，避免无限等待

## 监控和日志

### 关闭日志示例

```log
2025-01-XX 10:00:00.000 [main] INFO  o.s.b.w.e.tomcat.GracefulShutdown - Commencing graceful shutdown. Waiting for active requests to complete
2025-01-XX 10:00:15.000 [main] INFO  o.s.b.w.e.tomcat.GracefulShutdown - Graceful shutdown complete
2025-01-XX 10:00:15.100 [main] INFO  c.h.s.m.MediaServiceApplication - Closing RabbitMQ connections
2025-01-XX 10:00:15.200 [main] INFO  c.h.s.m.MediaServiceApplication - Closing database connections
2025-01-XX 10:00:15.300 [main] INFO  c.h.s.m.MediaServiceApplication - Application shutdown complete
```

### Actuator 健康检查

在关闭过程中，健康检查状态变化：

```
正常运行：
GET /actuator/health
→ {"status": "UP"}

收到关闭信号后：
GET /actuator/health
→ {"status": "OUT_OF_SERVICE"}
```

## 最佳实践

### 1. 设置合理的超时时间

```yaml
# 根据业务场景调整
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s  # 文件上传服务建议 30-60s
```

### 2. 配合负载均衡器

```yaml
# Kubernetes 配置
spec:
  containers:
  - name: siae-media
    lifecycle:
      preStop:
        exec:
          command: ["/bin/sh", "-c", "sleep 10"]  # 等待负载均衡器摘除
    terminationGracePeriodSeconds: 40  # 大于应用超时时间
```

### 3. 监控关闭过程

```java
@Component
public class ShutdownListener {
    
    @EventListener
    public void onShutdown(ContextClosedEvent event) {
        log.info("Application is shutting down gracefully");
        // 记录关闭时间、原因等
    }
}
```

### 4. 清理资源

```java
@PreDestroy
public void cleanup() {
    log.info("Cleaning up resources before shutdown");
    // 清理临时文件
    // 关闭自定义连接
    // 保存状态等
}
```

## 测试优雅关闭

### 本地测试

```bash
# 1. 启动应用
mvn spring-boot:run

# 2. 模拟长时间请求
curl -X POST http://localhost:8084/api/v1/media/uploads/init \
  -H "Content-Type: application/json" \
  -d '{"filename":"large-file.zip","size":1073741824}'

# 3. 在另一个终端发送关闭信号
kill -15 $(pgrep -f siae-media)

# 4. 观察日志，确认请求完成后才关闭
```

### Docker 测试

```bash
# 1. 启动容器
docker run -d --name siae-media siae/siae-media:latest

# 2. 发送请求
curl http://localhost:8084/api/v1/media/files

# 3. 停止容器（会发送 SIGTERM）
docker stop siae-media

# 4. 查看日志
docker logs siae-media
```

### Kubernetes 测试

```bash
# 1. 部署应用
kubectl apply -f k8s/deployment.yaml

# 2. 执行滚动更新
kubectl rollout restart deployment/siae-media -n siae

# 3. 观察 Pod 状态
kubectl get pods -n siae -w

# 4. 查看日志
kubectl logs -f <pod-name> -n siae
```

## 常见问题

### Q1: 为什么设置了优雅关闭，应用还是立即退出？

**A**: 检查以下几点：
1. 确认 `server.shutdown=graceful` 已配置
2. 检查是否发送的是 SIGKILL（kill -9）而不是 SIGTERM（kill -15）
3. 查看日志确认是否有异常

### Q2: 超时时间应该设置多长？

**A**: 根据业务场景：
- 普通 API：5-10秒
- 文件上传：30-60秒
- 长时间任务：60-120秒

### Q3: 优雅关闭会影响性能吗？

**A**: 不会。优雅关闭只在应用停止时生效，正常运行时没有任何性能影响。

### Q4: 如何强制立即关闭？

**A**: 发送 SIGKILL 信号：
```bash
kill -9 <pid>
```
⚠️ 注意：这会跳过优雅关闭，可能导致数据丢失。

## 相关配置

- [application.yml](../src/main/resources/application.yml) - 优雅关闭配置
- [GracefulShutdownConfig.java](../src/main/java/com/hngy/siae/media/config/GracefulShutdownConfig.java) - 配置类
- [deployment.yaml](../k8s/deployment.yaml) - Kubernetes 部署配置

## 参考资料

- [Spring Boot Graceful Shutdown](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.graceful-shutdown)
- [Kubernetes Pod Lifecycle](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/)

---

**最后更新**: 2025-01-XX  
**维护人员**: SIAE 开发团队
