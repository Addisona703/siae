# Media Service 部署指南

## 目录
- [本地开发环境](#本地开发环境)
- [Docker 部署](#docker-部署)
- [Kubernetes 部署](#kubernetes-部署)
- [监控和告警](#监控和告警)
- [故障排查](#故障排查)

## 本地开发环境

### 前置条件
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.12+
- MinIO (可选)
- Nacos 2.2+

### 方式一：纯本地环境（推荐开发使用）

#### 1. 安装依赖服务

**MySQL**
```bash
# Windows: 下载安装包
https://dev.mysql.com/downloads/mysql/

# 创建数据库
mysql -u root -p
CREATE DATABASE siae_media CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**Redis**
```bash
# Windows: 下载安装包或使用 WSL
https://github.com/tporadowski/redis/releases

# 启动 Redis
redis-server
```

**RabbitMQ**
```bash
# Windows: 下载安装包
https://www.rabbitmq.com/download.html

# 启动 RabbitMQ
rabbitmq-server

# 启用管理插件
rabbitmq-plugins enable rabbitmq_management
```

**MinIO**
```bash
# Windows: 下载可执行文件
https://min.io/download

# 启动 MinIO
minio.exe server C:\minio-data --console-address ":9001"
```

**Nacos**
```bash
# 下载 Nacos
https://github.com/alibaba/nacos/releases

# Windows 启动（单机模式）
cd nacos/bin
startup.cmd -m standalone
```

#### 2. 配置本地环境

在 `application-dev.yml` 中确认配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/siae_media
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379

siae:
  messaging:
    rabbit:
      connection:
        addresses: localhost:5672
        username: guest
        password: guest

minio:
  endpoint: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin
```

#### 3. 启动应用

```bash
# 方式1: 使用 Maven
cd siae/services/siae-media
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 方式2: 使用 IDE
# IntelliJ IDEA: 右键 MediaServiceApplication → Run
# 配置 Active Profile: dev
```

### 方式二：Docker Compose 快速启动（推荐测试使用）

如果不想在本地安装这么多服务，可以使用 Docker Compose：

```bash
cd siae/services/siae-media

# 只启动依赖服务（不启动应用）
docker-compose up -d mysql redis rabbitmq minio nacos

# 然后在 IDE 中启动应用
```

### 访问地址
- **应用**: http://localhost:8084
- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **Actuator**: http://localhost:8084/actuator
- **Actuator Health**: http://localhost:8084/actuator/health
- **Prometheus Metrics**: http://localhost:8084/actuator/prometheus
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Nacos Console**: http://localhost:8848/nacos (nacos/nacos)

### 验证服务启动

```bash
# 检查健康状态
curl http://localhost:8084/actuator/health

# 查看服务信息
curl http://localhost:8084/actuator/info

# 测试上传接口
curl -X POST http://localhost:8084/api/v1/media/uploads/init \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: test-tenant" \
  -H "X-User-Id: test-user" \
  -d '{
    "filename": "test.txt",
    "size": 1024,
    "mime": "text/plain"
  }'
```

### 开发建议

1. **使用 IDE 启动**
   - IntelliJ IDEA: 右键 `MediaServiceApplication` → Run
   - 配置 Active Profile: `dev`
   - 配置 VM Options: `-Xms512m -Xmx1024m`

2. **热重载**
   ```xml
   <!-- 在 pom.xml 中添加 -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-devtools</artifactId>
       <optional>true</optional>
   </dependency>
   ```

3. **调试模式**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
   ```

4. **查看日志**
   - 控制台日志：实时输出
   - 文件日志：`./logs/siae-media.log`

### 常见问题

**Q: 依赖服务太多，有没有更简单的方式？**  
A: 使用 Docker Compose 启动依赖服务，应用在 IDE 中启动，这样既方便调试又不用手动安装。

**Q: 如何快速重置开发环境？**  
A: 
```bash
# 停止并删除所有容器和数据
docker-compose down -v

# 重新启动
docker-compose up -d
```

**Q: Nacos 启动失败怎么办？**  
A: 确保 MySQL 已启动，Nacos 需要 MySQL 存储配置数据。

## Docker 部署

### 构建镜像
```bash
# 在项目根目录执行
cd siae/services/siae-media
docker build -t siae/siae-media:latest .
```

### 使用 Docker Compose 启动完整环境
```bash
docker-compose up -d
```

### 查看日志
```bash
docker-compose logs -f siae-media
```

### 停止服务
```bash
docker-compose down
```

## Kubernetes 部署

### 前置条件
- Kubernetes 集群 (v1.24+)
- kubectl 已配置
- Helm 3+ (可选，用于依赖服务)

### 部署依赖服务

#### 1. 部署 MySQL
```bash
helm install mysql bitnami/mysql \
  --set auth.rootPassword=root \
  --set auth.database=siae_media \
  --namespace siae
```

#### 2. 部署 Redis
```bash
helm install redis bitnami/redis \
  --set auth.password=redis123 \
  --namespace siae
```

#### 3. 部署 RabbitMQ
```bash
helm install rabbitmq bitnami/rabbitmq \
  --set auth.username=guest \
  --set auth.password=guest \
  --namespace siae
```

#### 4. 部署 MinIO
```bash
helm install minio bitnami/minio \
  --set auth.rootUser=minioadmin \
  --set auth.rootPassword=minioadmin \
  --namespace siae
```

### 部署 Media Service

#### 方式一：使用部署脚本
```bash
cd k8s
chmod +x deploy.sh
./deploy.sh siae prod
```

#### 方式二：手动部署
```bash
# 创建命名空间
kubectl create namespace siae

# 应用配置
kubectl apply -f k8s/rbac.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# 部署应用
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml

# 部署监控（如果已安装 Prometheus Operator）
kubectl apply -f k8s/servicemonitor.yaml
kubectl apply -f k8s/prometheusrule.yaml
```

### 验证部署
```bash
# 查看 Pod 状态
kubectl get pods -n siae -l app=siae-media

# 查看服务
kubectl get svc -n siae -l app=siae-media

# 查看 HPA
kubectl get hpa -n siae

# 查看日志
kubectl logs -f -n siae -l app=siae-media
```

### 端口转发（本地访问）
```bash
kubectl port-forward -n siae svc/siae-media 8084:8084
```

## 监控和告警

### Prometheus 指标
访问 `/actuator/prometheus` 端点查看所有指标。

关键指标：
- `media_file_upload_total` - 文件上传总数
- `media_file_upload_failure_total` - 上传失败总数
- `media_file_download_total` - 文件下载总数
- `media_quota_exceeded_total` - 配额超限次数
- `media_storage_used_bytes` - 存储使用量
- `http_server_requests_seconds` - HTTP 请求延迟

### Grafana 仪表板
1. 导入 Grafana 仪表板模板（如果有）
2. 配置 Prometheus 数据源
3. 查看关键指标和告警

### 告警规则
告警规则已在 `k8s/prometheusrule.yaml` 中定义：
- 服务不可用告警
- 高错误率告警
- 高延迟告警
- CPU/内存使用率告警
- 上传失败率告警
- Kafka 消费延迟告警

## 故障排查

### 常见问题

#### 1. Pod 无法启动
```bash
# 查看 Pod 事件
kubectl describe pod <pod-name> -n siae

# 查看日志
kubectl logs <pod-name> -n siae
```

#### 2. 健康检查失败
```bash
# 进入容器
kubectl exec -it <pod-name> -n siae -- sh

# 手动测试健康检查
curl http://localhost:8084/actuator/health
```

#### 3. 数据库连接失败
- 检查 Secret 中的数据库凭证
- 验证数据库服务是否可访问
- 检查网络策略

#### 4. MinIO 连接失败
- 验证 MinIO 服务状态
- 检查 MinIO 凭证配置
- 确认存储桶已创建

#### 5. RabbitMQ 队列堆积
```bash
# 查看队列状态
kubectl exec -it <rabbitmq-pod> -n siae -- rabbitmqctl list_queues name messages consumers

# 查看交换机绑定
kubectl exec -it <rabbitmq-pod> -n siae -- rabbitmqctl list_bindings
```

### 日志查看
```bash
# 实时查看日志
kubectl logs -f -n siae -l app=siae-media

# 查看最近的日志
kubectl logs --tail=100 -n siae <pod-name>

# 查看特定容器的日志
kubectl logs -n siae <pod-name> -c siae-media
```

### 性能分析
```bash
# 查看资源使用情况
kubectl top pods -n siae -l app=siae-media

# 查看节点资源
kubectl top nodes
```

## 扩缩容

### 手动扩缩容
```bash
# 扩容到 5 个副本
kubectl scale deployment/siae-media --replicas=5 -n siae

# 缩容到 2 个副本
kubectl scale deployment/siae-media --replicas=2 -n siae
```

### 自动扩缩容
HPA 已配置，会根据 CPU 和内存使用率自动扩缩容（3-10 个副本）。

## 滚动更新

### 更新镜像
```bash
kubectl set image deployment/siae-media \
  siae-media=registry.example.com/siae/siae-media:v1.1.0 \
  -n siae
```

### 查看更新状态
```bash
kubectl rollout status deployment/siae-media -n siae
```

### 回滚
```bash
# 回滚到上一个版本
kubectl rollout undo deployment/siae-media -n siae

# 回滚到指定版本
kubectl rollout undo deployment/siae-media --to-revision=2 -n siae
```

## 备份和恢复

### 数据库备份
```bash
# 备份数据库
kubectl exec -n siae <mysql-pod> -- \
  mysqldump -u root -p<password> siae_media > backup.sql

# 恢复数据库
kubectl exec -i -n siae <mysql-pod> -- \
  mysql -u root -p<password> siae_media < backup.sql
```

### MinIO 数据备份
使用 MinIO Client (mc) 进行数据备份和恢复。

## 安全建议

1. **Secret 管理**
   - 生产环境使用外部密钥管理系统（如 Vault）
   - 定期轮换密钥
   - 不要将 Secret 提交到版本控制

2. **网络策略**
   - 配置 NetworkPolicy 限制 Pod 间通信
   - 使用 Service Mesh 进行流量加密

3. **RBAC**
   - 遵循最小权限原则
   - 定期审计权限配置

4. **镜像安全**
   - 使用官方基础镜像
   - 定期扫描镜像漏洞
   - 使用非 root 用户运行容器

## 性能优化

1. **JVM 调优**
   - 根据实际负载调整堆内存大小
   - 使用 G1GC 垃圾收集器
   - 启用 JIT 编译优化

2. **数据库优化**
   - 配置连接池大小
   - 启用查询缓存
   - 定期分析慢查询

3. **缓存策略**
   - 合理使用 Redis 缓存
   - 配置缓存过期时间
   - 监控缓存命中率

4. **资源限制**
   - 根据实际使用情况调整 requests 和 limits
   - 避免资源过度分配或不足

## 联系方式

如有问题，请联系：
- 开发团队: dev@siae.com
- 运维团队: ops@siae.com
- 文档: https://docs.siae.com/media-service
