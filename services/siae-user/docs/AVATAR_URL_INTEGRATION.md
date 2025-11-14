# 用户头像URL集成说明

## 概述

用户服务已集成Media服务，在获取用户信息时自动返回头像的访问URL，而不仅仅是文件ID。

## 功能特性

### 1. 自动填充头像URL

所有用户查询接口都会自动填充 `avatarUrl` 字段：

- ✅ 单个用户查询：`getUserById()`
- ✅ 用户列表查询：`listUsersByPage()`
- ✅ 用户详情查询：`getUserDetailById()`
- ✅ 按用户名查询：`getUserByUsername()`

### 2. 批量优化

用户列表查询使用批量接口，一次性获取所有头像URL，性能优异：

```
100个用户列表：
- 传统方式：100次Media服务调用 ≈ 2000ms
- 批量方式：1次Media服务调用 ≈ 50ms（含缓存）
```

### 3. 容错处理

- Media服务调用失败不影响用户信息返回
- 只是 `avatarUrl` 字段为 `null`
- 记录警告日志便于排查

## 接口响应示例

### 单个用户查询

**请求**: `GET /api/v1/user/users/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "studentId": "2021001",
    "avatarFileId": "abc123",
    "avatarUrl": "https://minio.example.com/bucket/avatar.jpg?signature=...",
    "nickname": "张三",
    "createAt": "2024-01-01T10:00:00"
  }
}
```

### 用户列表查询

**请求**: `POST /api/v1/user/users/page`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "zhangsan",
        "avatarFileId": "abc123",
        "avatarUrl": "https://minio.example.com/bucket/avatar1.jpg?signature=..."
      },
      {
        "id": 2,
        "username": "lisi",
        "avatarFileId": "def456",
        "avatarUrl": "https://minio.example.com/bucket/avatar2.jpg?signature=..."
      }
    ],
    "total": 2,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 用户详情查询

**请求**: `GET /api/v1/user/users/{id}/detail`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "avatarFileId": "abc123",
    "avatarUrl": "https://minio.example.com/bucket/avatar.jpg?signature=...",
    "backgroundFileId": "xyz789",
    "backgroundUrl": "https://minio.example.com/bucket/bg.jpg?signature=...",
    "realName": "张三",
    "email": "zhangsan@example.com",
    "majorName": "软件工程",
    "className": "软工21-1班"
  }
}
```

## 技术实现

### 1. Feign Client

```java
@FeignClient(
    name = "siae-media",
    url = "${feign.media.url:http://localhost:8040}",
    path = "/api/v1/media/files"
)
public interface MediaFeignClient {
    @PostMapping("/urls/batch")
    Result<BatchUrlResponse> batchGetFileUrls(@RequestBody BatchUrlRequest request);
}
```

### 2. 服务层集成

```java
// 单个用户
private void enrichUserWithAvatarUrl(UserVO userVO) {
    if (StrUtil.isBlank(userVO.getAvatarFileId())) {
        return;
    }
    Map<String, String> urls = batchGetMediaUrls(
        Collections.singletonList(userVO.getAvatarFileId())
    );
    userVO.setAvatarUrl(urls.get(userVO.getAvatarFileId()));
}

// 用户列表（批量）
private void enrichUsersWithAvatarUrls(List<UserVO> users) {
    List<String> avatarIds = users.stream()
        .map(UserVO::getAvatarFileId)
        .filter(StrUtil::isNotBlank)
        .distinct()
        .collect(Collectors.toList());
    
    Map<String, String> urls = batchGetMediaUrls(avatarIds);
    
    users.forEach(user -> {
        if (StrUtil.isNotBlank(user.getAvatarFileId())) {
            user.setAvatarUrl(urls.get(user.getAvatarFileId()));
        }
    });
}
```

## 配置说明

### application-dev.yaml

```yaml
feign:
  media:
    url: http://localhost:8040  # Media服务地址
  client:
    config:
      default:
        connectTimeout: 5000    # 连接超时5秒
        readTimeout: 10000      # 读取超时10秒
        loggerLevel: basic      # 日志级别
```

### 生产环境配置

```yaml
feign:
  media:
    url: http://siae-media:8040  # 使用服务名（Nacos/K8s）
```

## URL过期策略

- **URL有效期**: 24小时（Media服务配置）
- **缓存时间**: 23小时（Redis缓存）
- **前端建议**: 可以缓存URL，在过期前无需重复请求

## 性能优化建议

### 1. 前端缓存

```javascript
// 前端可以缓存用户信息，包括avatarUrl
const cachedUser = localStorage.getItem('user');
if (cachedUser && !isExpired(cachedUser)) {
    return JSON.parse(cachedUser);
}
```

### 2. 分页查询

```javascript
// 一次性获取多个用户，利用批量优化
const users = await api.getUserList({ pageNum: 1, pageSize: 20 });
// 所有用户的avatarUrl都已填充
```

### 3. 懒加载

对于不需要立即显示头像的场景，可以延迟加载：

```javascript
// 只在需要时才获取用户详情（包含头像URL）
const userDetail = await api.getUserDetail(userId);
```

## 故障排查

### 1. avatarUrl为null

**可能原因**:
- Media服务未启动
- 文件ID不存在
- 文件已被删除
- 网络超时

**排查方法**:
```bash
# 查看User服务日志
tail -f logs/siae-user.log | grep "Failed to"

# 检查Media服务健康状态
curl http://localhost:8040/actuator/health

# 手动测试批量接口
curl -X POST http://localhost:8040/api/v1/media/files/urls/batch \
  -H "Content-Type: application/json" \
  -d '{"fileIds":["abc123"],"expirySeconds":86400}'
```

### 2. 性能问题

**检查批量调用是否生效**:
```bash
# 查看日志中的批量调用记录
grep "Enriched.*users with avatar URLs" logs/siae-user.log
```

**预期日志**:
```
Enriched 100 users with avatar URLs, success: 95/100
```

### 3. Feign调用失败

**检查配置**:
```yaml
# 确认Media服务地址正确
feign:
  media:
    url: http://localhost:8040
```

**检查网络连通性**:
```bash
curl http://localhost:8040/actuator/health
```

## 注意事项

1. **向后兼容**: `avatarFileId` 字段保留，前端可以选择使用ID或URL
2. **容错设计**: Media服务故障不影响用户信息查询
3. **性能优先**: 列表查询使用批量接口，避免N+1问题
4. **缓存策略**: Media服务端已实现Redis缓存，User服务无需额外缓存

## 未来优化

- [ ] 支持CDN加速（Media服务配置）
- [ ] 支持图片尺寸参数（缩略图）
- [ ] 支持WebP格式转换
- [ ] 支持图片懒加载占位符
