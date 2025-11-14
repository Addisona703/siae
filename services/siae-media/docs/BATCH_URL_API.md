# 批量获取文件URL API

## 概述

批量获取文件访问URL的接口，支持Redis缓存，提升性能。

## 接口信息

- **路径**: `POST /api/v1/media/files/urls/batch`
- **权限**: `MEDIA_FILE_QUERY`
- **Content-Type**: `application/json`

## 请求参数

```json
{
  "fileIds": ["file-id-1", "file-id-2", "file-id-3"],
  "expirySeconds": 86400
}
```

| 字段 | 类型 | 必填 | 说明 | 默认值 |
|------|------|------|------|--------|
| fileIds | String[] | 是 | 文件ID列表 | - |
| expirySeconds | Integer | 否 | URL过期时间（秒） | 86400（24小时） |

## 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "urls": {
      "file-id-1": "https://minio.example.com/bucket/path1?signature=...",
      "file-id-2": "https://minio.example.com/bucket/path2?signature=...",
      "file-id-3": "https://minio.example.com/bucket/path3?signature=..."
    },
    "expiresAt": "2024-11-15T10:30:00",
    "successCount": 3,
    "failedCount": 0
  }
}
```

## 响应字段

| 字段 | 类型 | 说明 |
|------|------|------|
| urls | Map<String, String> | 文件ID到URL的映射 |
| expiresAt | LocalDateTime | URL过期时间 |
| successCount | Integer | 成功生成URL的数量 |
| failedCount | Integer | 失败的数量 |

## 缓存策略

- **缓存Key**: `media:url:{fileId}`
- **缓存时间**: 23小时（比URL有效期短1小时）
- **缓存命中**: 直接从Redis返回，无需查询数据库和生成URL
- **缓存未命中**: 查询数据库，生成URL，并缓存

## 性能优化

### 批量查询优化

```
100个文件ID请求：
- 无缓存：100次数据库查询 + 100次URL生成 ≈ 2000ms
- 有缓存（90%命中率）：1次Redis批量查询 + 10次数据库查询 + 10次URL生成 ≈ 50ms
```

### 使用建议

1. **用户列表场景**：一次性传入所有用户的头像ID
2. **合理设置过期时间**：根据业务需求调整，默认24小时适合大多数场景
3. **前端缓存**：前端可以缓存返回的URL，在过期前无需重复请求

## 配置说明

在 `application.yaml` 中配置：

```yaml
media:
  url:
    expiration: 86400      # URL过期时间：24小时
    cache-enabled: true    # 启用缓存
    cache-ttl: 82800       # 缓存TTL：23小时
```

## 注意事项

1. 只有状态为 `COMPLETED` 且未删除的文件才会生成URL
2. 如果某个文件不存在或不可用，不会影响其他文件，只是不会出现在返回的 `urls` 中
3. 缓存的URL会提前1小时过期，避免返回即将失效的URL
4. 支持CDN加速（需要在配置中启用）

## 错误处理

- 如果 `fileIds` 为空，返回 400 错误
- 如果 `expirySeconds` 小于等于0，返回 400 错误
- 如果所有文件都不可用，返回空的 `urls` Map，`successCount` 为 0
