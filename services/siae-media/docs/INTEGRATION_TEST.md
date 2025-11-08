# 文件下载和签名功能集成测试指南

## 测试环境准备

### 1. 启动依赖服务

```bash
# 启动 MySQL
docker run -d --name mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=siae_media \
  -p 3306:3306 \
  mysql:8.0

# 启动 MinIO
docker run -d --name minio \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  -p 9000:9000 \
  -p 9001:9001 \
  minio/minio server /data --console-address ":9001"

# 启动 Redis
docker run -d --name redis \
  -p 6379:6379 \
  redis:7-alpine
```

### 2. 配置应用

确保 `application-dev.yml` 配置正确：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/siae_media
    username: root
    password: root

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: siae-media
```

### 3. 启动应用

```bash
cd siae/services/siae-media
mvn spring-boot:run
```

## 测试场景

### 场景 1：基本下载签名生成

**步骤 1：上传文件**

```bash
# 初始化上传
curl -X POST http://localhost:8080/api/media/upload/init \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "filename": "test.pdf",
    "size": 1024,
    "mime": "application/pdf"
  }'

# 记录返回的 fileId 和 uploadUrl
```

**步骤 2：上传文件到 MinIO**

```bash
# 使用返回的 uploadUrl 上传文件
curl -X PUT "{uploadUrl}" \
  --data-binary "@test.pdf"
```

**步骤 3：完成上传**

```bash
curl -X POST http://localhost:8080/api/media/upload/complete \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "uploadId": "{uploadId}",
    "sha256": "abc123..."
  }'
```

**步骤 4：生成下载签名**

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "fileId": "{fileId}",
    "expirySeconds": 3600
  }'
```

**预期结果：**
- 返回包含签名URL的响应
- URL可以直接在浏览器中访问下载文件

### 场景 2：IP 绑定下载

**步骤 1：生成绑定IP的签名**

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "fileId": "{fileId}",
    "expirySeconds": 1800,
    "bindIp": true
  }'
```

**步骤 2：从相同IP访问**

```bash
# 使用返回的URL下载文件
curl -X GET "{signedUrl}"
```

**步骤 3：从不同IP访问（应该失败）**

```bash
# 使用代理或不同网络访问
curl -X GET "{signedUrl}" --proxy http://proxy-server:8080
```

**预期结果：**
- 从相同IP访问成功
- 从不同IP访问失败（如果实现了IP验证）

### 场景 3：单次使用下载

**步骤 1：生成单次使用签名**

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "fileId": "{fileId}",
    "expirySeconds": 600,
    "singleUse": true
  }'
```

**步骤 2：第一次下载**

```bash
curl -X GET "{signedUrl}" -o downloaded-file.pdf
```

**步骤 3：第二次下载（应该失败）**

```bash
curl -X GET "{signedUrl}" -o downloaded-file-2.pdf
```

**预期结果：**
- 第一次下载成功
- 第二次下载失败（令牌已使用）

### 场景 4：权限验证

**步骤 1：更新文件ACL（设置为私有）**

```bash
curl -X PUT http://localhost:8080/api/media/files/{fileId} \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "acl": {
      "public": false,
      "allowedUsers": ["user-001"]
    }
  }'
```

**步骤 2：所有者生成签名（应该成功）**

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "fileId": "{fileId}",
    "expirySeconds": 3600
  }'
```

**步骤 3：其他用户生成签名（应该失败）**

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-002" \
  -d '{
    "fileId": "{fileId}",
    "expirySeconds": 3600
  }'
```

**预期结果：**
- 所有者可以生成签名
- 其他用户无法生成签名（返回403错误）

### 场景 5：公开文件访问

**步骤 1：设置文件为公开**

```bash
curl -X PUT http://localhost:8080/api/media/files/{fileId} \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "acl": {
      "public": true
    }
  }'
```

**步骤 2：任意用户生成签名**

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-999" \
  -d '{
    "fileId": "{fileId}",
    "expirySeconds": 3600
  }'
```

**预期结果：**
- 任何用户都可以生成签名
- 下载成功

### 场景 6：已删除文件访问

**步骤 1：软删除文件**

```bash
curl -X DELETE http://localhost:8080/api/media/files/{fileId} \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001"
```

**步骤 2：尝试生成签名**

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "fileId": "{fileId}",
    "expirySeconds": 3600
  }'
```

**预期结果：**
- 返回错误（文件已被删除）

### 场景 7：跨租户访问

**步骤 1：租户A的用户尝试访问租户B的文件**

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-002" \
  -H "X-User-Id: user-001" \
  -d '{
    "fileId": "{tenant-001-fileId}",
    "expirySeconds": 3600
  }'
```

**预期结果：**
- 返回错误（无权访问该文件）

## 验证审计日志

查询审计日志，验证所有操作都被记录：

```sql
SELECT * FROM audit_logs 
WHERE action IN ('sign', 'download') 
ORDER BY occurred_at DESC 
LIMIT 10;
```

**预期结果：**
- 所有签名生成操作都有记录
- 包含用户ID、文件ID、IP地址等信息

## 验证令牌清理

**步骤 1：生成过期令牌**

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "fileId": "{fileId}",
    "expirySeconds": 60,
    "singleUse": true
  }'
```

**步骤 2：等待令牌过期**

```bash
# 等待60秒以上
sleep 70
```

**步骤 3：检查数据库**

```sql
SELECT COUNT(*) FROM download_tokens WHERE expires_at < NOW();
```

**步骤 4：等待定时任务执行**

```bash
# 等待下一个整点（定时任务每小时执行）
# 或手动触发清理任务
```

**步骤 5：再次检查数据库**

```sql
SELECT COUNT(*) FROM download_tokens WHERE expires_at < NOW();
```

**预期结果：**
- 过期令牌被自动清理
- 数据库中不再有过期令牌

## 性能测试

### 并发签名生成测试

使用 Apache Bench 或 JMeter 进行并发测试：

```bash
# 使用 ab 工具
ab -n 1000 -c 10 -p sign-request.json -T application/json \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  http://localhost:8080/api/media/download/sign
```

**预期结果：**
- 支持高并发请求
- 响应时间在可接受范围内（< 100ms）
- 无错误或超时

## 故障恢复测试

### 数据库连接失败

**步骤 1：停止数据库**

```bash
docker stop mysql
```

**步骤 2：尝试生成签名**

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "fileId": "{fileId}",
    "expirySeconds": 3600
  }'
```

**步骤 3：重启数据库**

```bash
docker start mysql
```

**步骤 4：再次尝试**

**预期结果：**
- 数据库停止时返回错误
- 数据库恢复后正常工作

## 测试清单

- [ ] 基本下载签名生成
- [ ] IP 绑定验证
- [ ] 单次使用限制
- [ ] 所有者权限验证
- [ ] ACL 权限验证
- [ ] 公开文件访问
- [ ] 已删除文件拒绝访问
- [ ] 跨租户隔离
- [ ] 审计日志记录
- [ ] 令牌自动清理
- [ ] 并发性能测试
- [ ] 故障恢复测试

## 常见问题

### Q1: 签名URL无法访问

**可能原因：**
- MinIO服务未启动
- 网络配置问题
- 签名已过期

**解决方案：**
- 检查MinIO服务状态
- 验证网络连接
- 重新生成签名

### Q2: IP绑定不生效

**可能原因：**
- 使用了代理或负载均衡
- IP地址获取逻辑有误

**解决方案：**
- 检查 X-Forwarded-For 头
- 调整IP获取逻辑

### Q3: 令牌未被清理

**可能原因：**
- 定时任务未启用
- 定时任务执行失败

**解决方案：**
- 检查 @EnableScheduling 注解
- 查看应用日志
- 手动执行清理方法
