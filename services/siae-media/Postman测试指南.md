# SIAE Media - Postman 测试指南

## 前置准备

### 1. 确保服务已启动
- ✅ siae-media 服务运行在 `http://localhost:8084`
- ✅ MinIO 运行在 `http://localhost:9005` (API) 和 `http://localhost:9000` (控制台)
- ✅ MySQL、Redis、RabbitMQ 都已启动

### 2. 获取 JWT Token（如果需要认证）

**方式一：临时跳过认证（开发环境）**
- 如果配置了 `enableDirectAccess: true`，可以暂时不需要 token

**方式二：从登录接口获取**
```
POST http://localhost:8081/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```
复制响应中的 `token` 字段

---

## Postman 测试步骤

### 步骤 1: 初始化上传（小文件）

#### 1.1 创建请求

**请求类型**: `POST`

**URL**: 
```
http://localhost:8084/api/v1/media/uploads:init
```

**注意**: URL 中的 `:init` 是路径的一部分，不要漏掉冒号！

#### 1.2 设置请求头

点击 `Headers` 标签，添加：

| Key | Value |
|-----|-------|
| Content-Type | application/json |
| Authorization | Bearer {your_token} |

**如果不需要认证，可以暂时不加 Authorization**

#### 1.3 设置请求体

点击 `Body` 标签 → 选择 `raw` → 选择 `JSON`

**复制以下内容**:
```json
{
  "filename": "test-document.pdf",
  "size": 1024000,
  "mime": "application/pdf",
  "tenantId": "tenant-001",
  "ownerId": "user-123",
  "bizTags": ["test", "document"],
  "ext": {
    "category": "test",
    "description": "测试文件"
  }
}
```

#### 1.4 发送请求

点击 `Send` 按钮

#### 1.5 查看响应

**成功响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uploadId": "1857046789123456789",
    "fileId": "f8e7d6c5b4a3",
    "bucket": "siae-media",
    "parts": [
      {
        "partNumber": 1,
        "url": "http://localhost:9005/siae-media/tenant-001/1730976543210/test-document.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
        "expiresAt": "2024-11-07T17:30:00"
      }
    ],
    "headers": {},
    "expireAt": "2024-11-07T17:30:00"
  }
}
```

**重要**: 
- ✅ 复制 `uploadId` - 后续完成上传时需要
- ✅ 复制 `fileId` - 下载文件时需要
- ✅ 复制 `parts[0].url` - 这是预签名URL，用于上传文件

---

### 步骤 2: 上传文件到 MinIO

#### 2.1 创建新请求

**请求类型**: `PUT`

**URL**: 
```
粘贴步骤1响应中的 parts[0].url
```

**完整URL示例**:
```
http://localhost:9005/siae-media/tenant-001/1730976543210/test-document.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20241107%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20241107T083000Z&X-Amz-Expires=3600&X-Amz-SignedHeaders=host&X-Amz-Signature=abc123...
```

#### 2.2 设置请求头

| Key | Value |
|-----|-------|
| Content-Type | application/pdf |

**注意**: 不需要 Authorization，预签名URL已包含认证信息

#### 2.3 设置请求体

点击 `Body` 标签 → 选择 `binary` → 点击 `Select File` → 选择你要上传的文件

**或者使用测试文件**:
- 创建一个简单的文本文件 `test.txt`
- 内容随意，比如 "Hello SIAE Media"

#### 2.4 发送请求

点击 `Send` 按钮

#### 2.5 查看响应

**成功响应**:
- Status: `200 OK`
- Headers 中会有 `ETag` 字段（如果是分片上传需要保存）

**示例响应头**:
```
ETag: "d41d8cd98f00b204e9800998ecf8427e"
```

---

### 步骤 3: 完成上传

#### 3.1 创建请求

**请求类型**: `POST`

**URL**: 
```
http://localhost:8084/api/v1/media/uploads/{uploadId}:complete
```

**替换 {uploadId}**: 使用步骤1中获取的 uploadId

**完整URL示例**:
```
http://localhost:8084/api/v1/media/uploads/1857046789123456789:complete
```

#### 3.2 设置请求头

| Key | Value |
|-----|-------|
| Content-Type | application/json |
| Authorization | Bearer {your_token} |

#### 3.3 设置请求体

点击 `Body` 标签 → 选择 `raw` → 选择 `JSON`

**简单完成（不验证校验和）**:
```json
{
  "checksum": {}
}
```

**或者提供校验和**:
```json
{
  "checksum": {
    "sha256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
  }
}
```

#### 3.4 发送请求

点击 `Send` 按钮

#### 3.5 查看响应

**成功响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "fileId": "f8e7d6c5b4a3",
    "status": "COMPLETED",
    "downloadUrl": "http://localhost:8084/api/v1/media/files/f8e7d6c5b4a3/download",
    "previewUrl": "http://localhost:8084/api/v1/media/files/f8e7d6c5b4a3/preview"
  }
}
```

---

## 完整测试流程（分片上传）

### 步骤 1: 初始化分片上传

**URL**: `POST http://localhost:8084/api/v1/media/uploads:init`

**请求体**:
```json
{
  "filename": "large-video.mp4",
  "size": 52428800,
  "mime": "video/mp4",
  "tenantId": "tenant-001",
  "ownerId": "user-123",
  "multipart": {
    "enabled": true,
    "partSize": 10485760
  },
  "bizTags": ["video", "test"]
}
```

**响应会包含多个 parts**:
```json
{
  "code": 200,
  "data": {
    "uploadId": "1857046789123456790",
    "fileId": "f8e7d6c5b4a4",
    "parts": [
      {
        "partNumber": 1,
        "url": "http://localhost:9005/...?partNumber=1&..."
      },
      {
        "partNumber": 2,
        "url": "http://localhost:9005/...?partNumber=2&..."
      },
      {
        "partNumber": 3,
        "url": "http://localhost:9005/...?partNumber=3&..."
      }
    ]
  }
}
```

### 步骤 2: 上传每个分片

**对每个 part 创建一个 PUT 请求**:

**Part 1**:
- URL: `parts[0].url`
- Body: binary → 选择文件的第1部分（0-10MB）

**Part 2**:
- URL: `parts[1].url`
- Body: binary → 选择文件的第2部分（10MB-20MB）

**保存每个响应的 ETag**

### 步骤 3: 完成分片上传

**URL**: `POST http://localhost:8084/api/v1/media/uploads/{uploadId}:complete`

**请求体**:
```json
{
  "parts": [
    {
      "partNumber": 1,
      "etag": "\"d41d8cd98f00b204e9800998ecf8427e\""
    },
    {
      "partNumber": 2,
      "etag": "\"098f6bcd4621d373cade4e832627b4f6\""
    },
    {
      "partNumber": 3,
      "etag": "\"5d41402abc4b2a76b9719d911017c592\""
    }
  ]
}
```

---

## 验证上传结果

### 方法 1: 通过 MinIO 控制台查看

1. 访问: http://localhost:9000
2. 登录: minioadmin / minioadmin
3. 进入 `siae-media` bucket
4. 查看 `tenant-001/{timestamp}/` 目录下的文件

### 方法 2: 通过下载接口验证

**URL**: `GET http://localhost:8084/api/v1/media/files/{fileId}/download`

**示例**:
```
GET http://localhost:8084/api/v1/media/files/f8e7d6c5b4a3/download
Authorization: Bearer {your_token}
```

### 方法 3: 查看数据库

```sql
-- 查看文件记录
SELECT * FROM files WHERE id = 'f8e7d6c5b4a3';

-- 查看上传记录
SELECT * FROM uploads WHERE file_id = 'f8e7d6c5b4a3';
```

---

## 常见问题排查

### 问题 1: 401 Unauthorized

**原因**: 缺少或无效的 JWT token

**解决**:
1. 检查 Authorization 头是否正确
2. 确认 token 格式: `Bearer {token}`
3. 检查 token 是否过期
4. 开发环境可以临时禁用认证

### 问题 2: 404 Not Found (上传初始化)

**原因**: URL 路径错误

**检查**:
- ✅ URL 是否包含 `:init`
- ✅ 端口是否正确 (8084)
- ✅ 路径是否完整: `/api/v1/media/uploads:init`

### 问题 3: 预签名URL上传失败

**原因**: URL 过期或格式错误

**解决**:
1. 确保在 URL 过期前上传（默认1小时）
2. 完整复制 URL，不要遗漏参数
3. 使用 PUT 方法，不是 POST
4. 检查 MinIO 是否正常运行

### 问题 4: 完成上传时提示文件不存在

**原因**: 文件未成功上传到 MinIO

**解决**:
1. 检查步骤2的响应状态码是否为 200
2. 在 MinIO 控制台确认文件是否存在
3. 检查 uploadId 是否正确

### 问题 5: 存储配额不足

**原因**: 租户配额已用完

**解决**:
```sql
-- 查看配额
SELECT * FROM quotas WHERE tenant_id = 'tenant-001';

-- 重置配额（开发环境）
UPDATE quotas 
SET bytes_used = 0, objects_count = 0 
WHERE tenant_id = 'tenant-001';
```

---

## Postman Collection 导入

### 创建 Collection

1. 打开 Postman
2. 点击 `Import` 按钮
3. 选择 `Raw text`
4. 粘贴以下 JSON:

```json
{
  "info": {
    "name": "SIAE Media API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. 初始化上传（小文件）",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"filename\": \"test.pdf\",\n  \"size\": 1024000,\n  \"mime\": \"application/pdf\",\n  \"tenantId\": \"tenant-001\",\n  \"ownerId\": \"user-123\",\n  \"bizTags\": [\"test\"]\n}"
        },
        "url": {
          "raw": "http://localhost:8084/api/v1/media/uploads:init",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8084",
          "path": ["api", "v1", "media", "uploads:init"]
        }
      }
    },
    {
      "name": "2. 上传文件到MinIO",
      "request": {
        "method": "PUT",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/pdf"
          }
        ],
        "body": {
          "mode": "file",
          "file": {}
        },
        "url": {
          "raw": "{{presignedUrl}}",
          "host": ["{{presignedUrl}}"]
        }
      }
    },
    {
      "name": "3. 完成上传",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"checksum\": {}\n}"
        },
        "url": {
          "raw": "http://localhost:8084/api/v1/media/uploads/{{uploadId}}:complete",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8084",
          "path": ["api", "v1", "media", "uploads", "{{uploadId}}:complete"]
        }
      }
    },
    {
      "name": "4. 下载文件",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8084/api/v1/media/files/{{fileId}}/download",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8084",
          "path": ["api", "v1", "media", "files", "{{fileId}}", "download"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "uploadId",
      "value": ""
    },
    {
      "key": "fileId",
      "value": ""
    },
    {
      "key": "presignedUrl",
      "value": ""
    }
  ]
}
```

5. 点击 `Import`

### 使用 Collection

1. 执行 "1. 初始化上传"
2. 从响应中复制 `uploadId`、`fileId`、`parts[0].url`
3. 在 Collection 变量中设置这些值
4. 执行后续请求

---

## 快速测试脚本

### 使用 Postman Tests 自动化

在 "1. 初始化上传" 请求的 `Tests` 标签中添加:

```javascript
// 保存响应数据到变量
var jsonData = pm.response.json();

if (jsonData.code === 200) {
    pm.collectionVariables.set("uploadId", jsonData.data.uploadId);
    pm.collectionVariables.set("fileId", jsonData.data.fileId);
    pm.collectionVariables.set("presignedUrl", jsonData.data.parts[0].url);
    
    console.log("Upload ID:", jsonData.data.uploadId);
    console.log("File ID:", jsonData.data.fileId);
    console.log("Presigned URL:", jsonData.data.parts[0].url);
}
```

这样就可以自动保存变量，无需手动复制！

---

## 总结

**测试顺序**:
1. ✅ 初始化上传 → 获取 uploadId 和预签名URL
2. ✅ 使用预签名URL上传文件到MinIO
3. ✅ 完成上传 → 通知服务器
4. ✅ 下载或预览文件验证

**关键点**:
- URL 中的 `:init` 和 `:complete` 不要漏掉
- 预签名URL要完整复制，包含所有参数
- 上传到MinIO使用 PUT 方法
- 保存好 uploadId 和 fileId

有问题随时问我！
