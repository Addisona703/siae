# 文件下载和签名 API 文档

## 概述

Media Service 提供了安全的文件下载功能，支持：
- 基于 ACL 的权限验证
- 带有效期限制的签名 URL
- IP 绑定验证
- 单次使用限制
- 完整的审计日志

## API 接口

### 1. 生成下载签名

生成一个带签名的下载 URL，用于安全地下载文件。

**请求**

```http
POST /api/media/download/sign
Content-Type: application/json
X-Tenant-Id: {tenant_id}
X-User-Id: {user_id}

{
  "fileId": "file-uuid",
  "expirySeconds": 3600,
  "bindIp": false,
  "singleUse": false,
  "filename": "download.pdf"
}
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileId | String | 是 | 文件ID |
| expirySeconds | Integer | 否 | 签名有效期（秒），默认3600，范围60-86400 |
| bindIp | Boolean | 否 | 是否绑定IP，默认false |
| singleUse | Boolean | 否 | 是否单次使用，默认false |
| filename | String | 否 | 下载文件名（用于Content-Disposition） |

**响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "fileId": "file-uuid",
    "url": "https://minio.example.com/bucket/path?signature=xxx&token=yyy",
    "token": "abc123def456",
    "expiresAt": "2024-01-01T12:00:00",
    "bindIp": false,
    "singleUse": false
  }
}
```

**响应字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| fileId | String | 文件ID |
| url | String | 签名下载URL |
| token | String | 下载令牌（仅在bindIp或singleUse为true时返回） |
| expiresAt | DateTime | 过期时间 |
| bindIp | Boolean | 是否绑定IP |
| singleUse | Boolean | 是否单次使用 |

## 权限控制

### ACL 权限模型

文件的 ACL 字段支持以下配置：

```json
{
  "public": false,
  "allowedUsers": ["user1", "user2"],
  "allowedRoles": ["admin", "editor"]
}
```

**权限验证规则**

1. 文件所有者始终有权访问
2. 如果 `public: true`，所有人都可以访问
3. 如果用户在 `allowedUsers` 列表中，可以访问
4. 如果没有配置 ACL，只有所有者可以访问

### IP 绑定

当 `bindIp: true` 时：
- 系统会记录请求者的 IP 地址
- 生成的下载令牌只能从该 IP 访问
- 适用于需要严格控制访问来源的场景

### 单次使用

当 `singleUse: true` 时：
- 生成的下载令牌只能使用一次
- 使用后立即失效
- 适用于临时分享、一次性下载等场景

## 使用示例

### 示例 1：普通下载

生成一个1小时有效的普通下载链接：

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "fileId": "file-uuid-123",
    "expirySeconds": 3600
  }'
```

### 示例 2：IP 绑定下载

生成一个绑定IP的下载链接：

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "fileId": "file-uuid-123",
    "expirySeconds": 1800,
    "bindIp": true
  }'
```

### 示例 3：单次使用下载

生成一个只能使用一次的下载链接：

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "fileId": "file-uuid-123",
    "expirySeconds": 600,
    "singleUse": true
  }'
```

### 示例 4：组合使用

生成一个绑定IP且单次使用的下载链接：

```bash
curl -X POST http://localhost:8080/api/media/download/sign \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-001" \
  -H "X-User-Id: user-001" \
  -d '{
    "fileId": "file-uuid-123",
    "expirySeconds": 300,
    "bindIp": true,
    "singleUse": true,
    "filename": "report.pdf"
  }'
```

## 错误处理

### 常见错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 400 | 参数验证失败 | 检查请求参数是否符合要求 |
| 403 | 无权访问该文件 | 检查用户权限和文件ACL配置 |
| 404 | 文件不存在 | 确认文件ID是否正确 |
| 410 | 文件已被删除 | 文件已被软删除，无法下载 |
| 423 | 文件状态不可用 | 文件未完成上传或处理中 |

### 错误响应示例

```json
{
  "code": 403,
  "message": "无权访问该文件",
  "data": null
}
```

## 审计日志

所有下载签名生成操作都会记录审计日志，包括：

- 文件ID
- 租户ID
- 用户ID
- 操作时间
- 客户端IP
- 签名参数（有效期、IP绑定、单次使用等）

审计日志可用于：
- 安全审计
- 访问分析
- 问题排查
- 合规要求

## 最佳实践

1. **选择合适的有效期**
   - 临时分享：300-600秒（5-10分钟）
   - 普通下载：1800-3600秒（30分钟-1小时）
   - 长期访问：使用公开ACL而非长有效期签名

2. **使用IP绑定**
   - 适用于内部系统间调用
   - 防止链接被转发滥用
   - 注意代理和负载均衡的影响

3. **使用单次使用**
   - 适用于敏感文件下载
   - 防止链接被多次使用
   - 结合短有效期使用效果更好

4. **设置合理的ACL**
   - 默认私有，按需开放
   - 定期审查权限配置
   - 使用角色而非直接指定用户

## 性能优化

1. **令牌清理**
   - 系统每小时自动清理过期令牌
   - 每天凌晨2点清理7天前的已使用令牌
   - 无需手动维护

2. **缓存策略**
   - 签名URL可以在客户端缓存
   - 注意有效期，避免使用过期URL
   - 建议在过期前5分钟重新生成

3. **并发控制**
   - 支持高并发签名生成
   - 令牌验证使用数据库索引优化
   - 建议使用连接池和缓存

## 安全建议

1. **HTTPS传输**
   - 生产环境必须使用HTTPS
   - 防止签名URL被中间人截获

2. **有效期控制**
   - 不要设置过长的有效期
   - 根据实际需求选择合适的时长

3. **访问日志**
   - 定期检查审计日志
   - 监控异常访问模式
   - 及时发现安全问题

4. **租户隔离**
   - 严格的租户ID验证
   - 防止跨租户访问
   - 使用独立的存储桶
