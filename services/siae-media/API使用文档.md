# SIAE Media 文件上传 API 使用文档

## 基础信息

- **Base URL**: `http://localhost:8084/api/v1/media`
- **认证方式**: Bearer Token (JWT)
- **Content-Type**: `application/json`

---

## 文件上传流程

### 方式一：小文件直接上传（推荐 < 100MB）

```
1. 初始化上传 (获取预签名URL)
   ↓
2. 使用预签名URL直接上传文件到MinIO
   ↓
3. 完成上传 (通知服务器)
```

### 方式二：大文件分片上传（推荐 > 100MB）

```
1. 初始化上传 (启用分片，获取多个预签名URL)
   ↓
2. 并行上传各个分片到MinIO
   ↓
3. 完成上传 (提交所有分片的ETag)
```

---

## API 接口详情

### 1. 初始化上传

**接口**: `POST /uploads:init`

**请求头**:
```http
Authorization: Bearer {your_jwt_token}
Content-Type: application/json
```

#### 示例 1: 小文件直接上传

**请求体**:
```json
{
  "filename": "report.pdf",
  "size": 5242880,
  "mime": "application/pdf",
  "tenantId": "tenant-001",
  "ownerId": "user-123",
  "bizTags": ["report", "2024"],
  "checksum": {
    "sha256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
  },
  "acl": {
    "public": false,
    "allowedUsers": ["user-456"]
  },
  "ext": {
    "category": "document",
    "department": "finance"
  }
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uploadId": "upload-abc123",
    "fileId": "file-xyz789",
    "bucket": "siae-media",
    "parts": [
      {
        "partNumber": 1,
        "url": "http://localhost:9005/siae-media/tenant-001/1699876543210/report.pdf?X-Amz-Algorithm=...",
        "expiresAt": "2024-11-07T17:00:00"
      }
    ],
    "headers": {},
    "expireAt": "2024-11-07T17:00:00"
  }
}
```

#### 示例 2: 大文件分片上传

**请求体**:
```json
{
  "filename": "video.mp4",
  "size": 524288000,
  "mime": "video/mp4",
  "tenantId": "tenant-001",
  "ownerId": "user-123",
  "multipart": {
    "enabled": true,
    "partSize": 10485760
  },
  "bizTags": ["video", "training"],
  "ext": {
    "duration": 3600,
    "resolution": "1920x1080"
  }
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uploadId": "upload-def456",
    "fileId": "file-uvw321",
    "bucket": "siae-media",
    "parts": [
      {
        "partNumber": 1,
        "url": "http://localhost:9005/siae-media/tenant-001/1699876543210/video.mp4?partNumber=1&X-Amz-Algorithm=...",
        "expiresAt": "2024-11-07T17:00:00"
      },
      {
        "partNumber": 2,
        "url": "http://localhost:9005/siae-media/tenant-001/1699876543210/video.mp4?partNumber=2&X-Amz-Algorithm=...",
        "expiresAt": "2024-11-07T17:00:00"
      }
      // ... 更多分片
    ],
    "expireAt": "2024-11-07T17:00:00"
  }
}
```

---

### 2. 上传文件到 MinIO

使用步骤1返回的预签名URL，直接通过 HTTP PUT 上传文件。

#### 小文件上传示例

```bash
curl -X PUT \
  "http://localhost:9005/siae-media/tenant-001/1699876543210/report.pdf?X-Amz-Algorithm=..." \
  -H "Content-Type: application/pdf" \
  --data-binary @report.pdf
```

#### 分片上传示例

```bash
# 上传第1个分片
curl -X PUT \
  "http://localhost:9005/siae-media/tenant-001/1699876543210/video.mp4?partNumber=1&X-Amz-Algorithm=..." \
  -H "Content-Type: video/mp4" \
  --data-binary @video.part1

# 上传第2个分片
curl -X PUT \
  "http://localhost:9005/siae-media/tenant-001/1699876543210/video.mp4?partNumber=2&X-Amz-Algorithm=..." \
  -H "Content-Type: video/mp4" \
  --data-binary @video.part2
```

**注意**: 保存响应头中的 `ETag` 值，完成上传时需要提交。

---

### 3. 完成上传

**接口**: `POST /uploads/{uploadId}:complete`

#### 小文件完成示例

**请求**:
```http
POST /uploads/upload-abc123:complete
Authorization: Bearer {your_jwt_token}
Content-Type: application/json
```

**请求体**:
```json
{
  "checksum": {
    "sha256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
  }
}
```

#### 分片上传完成示例

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
    }
  ],
  "checksum": {
    "sha256": "final_file_sha256_hash"
  }
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "fileId": "file-xyz789",
    "status": "COMPLETED",
    "downloadUrl": "http://localhost:8084/api/v1/media/files/file-xyz789/download",
    "previewUrl": "http://localhost:8084/api/v1/media/files/file-xyz789/preview"
  }
}
```
```cmd
示例：

    python scripts/multipart_upload.py `
      --file C:\tmp\video.mp4 `
      --token <你的JWT> `
      --tenant tenant-001 `
      --base-url http://localhost/api/v1/media `
      --part-size 20
```

quick:
```cmd
python multipart_upload.py ^
      --file ..\..\meida-test-data\lol.mp4 ^
      --token eyJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoicHJlc2lkZW50IiwiaWF0IjoxNzYyNTA0NDYwLCJleHAiOjE3NjI1OTA5MDN9.FnKWMiaOhwZcbdkf4s4V3XTmwTkLfmD8wAnl1EI5CnEzjxPVdJkhyZ9ySu9IGHB0 ^
      --tenant tenant-001 ^
      --part-size 10
```

---

### 4. 刷新上传 URL（可选）

当预签名URL过期时使用。

**接口**: `POST /uploads/{uploadId}:refresh`

**请求体**:
```json
{
  "parts": [
    {
      "partNumber": 3
    },
    {
      "partNumber": 4
    }
  ]
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uploadId": "upload-def456",
    "parts": [
      {
        "partNumber": 3,
        "url": "http://localhost:9005/...",
        "expiresAt": "2024-11-07T18:00:00"
      },
      {
        "partNumber": 4,
        "url": "http://localhost:9005/...",
        "expiresAt": "2024-11-07T18:00:00"
      }
    ],
    "expiresAt": "2024-11-07T18:00:00"
  }
}
```

---

### 5. 中断上传

**接口**: `POST /uploads/{uploadId}:abort`

**请求**:
```http
POST /uploads/upload-abc123:abort
Authorization: Bearer {your_jwt_token}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 完整示例代码

### JavaScript/Axios 示例

```javascript
const axios = require('axios');
const fs = require('fs');

const BASE_URL = 'http://localhost:8084/api/v1/media';
const TOKEN = 'your_jwt_token_here';

// 1. 初始化上传
async function initUpload() {
  const response = await axios.post(
    `${BASE_URL}/uploads:init`,
    {
      filename: 'test.pdf',
      size: 1024000,
      mime: 'application/pdf',
      tenantId: 'tenant-001',
      ownerId: 'user-123',
      bizTags: ['test']
    },
    {
      headers: {
        'Authorization': `Bearer ${TOKEN}`,
        'Content-Type': 'application/json'
      }
    }
  );
  
  return response.data.data;
}

// 2. 上传文件到MinIO
async function uploadFile(presignedUrl, filePath) {
  const fileBuffer = fs.readFileSync(filePath);
  
  const response = await axios.put(presignedUrl, fileBuffer, {
    headers: {
      'Content-Type': 'application/pdf'
    }
  });
  
  return response.headers.etag;
}

// 3. 完成上传
async function completeUpload(uploadId, etag) {
  const response = await axios.post(
    `${BASE_URL}/uploads/${uploadId}:complete`,
    {
      checksum: {
        sha256: 'file_hash_here'
      }
    },
    {
      headers: {
        'Authorization': `Bearer ${TOKEN}`,
        'Content-Type': 'application/json'
      }
    }
  );
  
  return response.data.data;
}

// 完整流程
async function uploadFileComplete() {
  try {
    // 步骤1: 初始化
    const initData = await initUpload();
    console.log('Upload initialized:', initData.uploadId);
    
    // 步骤2: 上传文件
    const presignedUrl = initData.parts[0].url;
    const etag = await uploadFile(presignedUrl, './test.pdf');
    console.log('File uploaded, ETag:', etag);
    
    // 步骤3: 完成上传
    const result = await completeUpload(initData.uploadId, etag);
    console.log('Upload completed:', result.fileId);
    console.log('Download URL:', result.downloadUrl);
    
  } catch (error) {
    console.error('Upload failed:', error.response?.data || error.message);
  }
}

uploadFileComplete();
```

### Python 示例

```python
import requests
import hashlib

BASE_URL = 'http://localhost:8084/api/v1/media'
TOKEN = 'your_jwt_token_here'

def init_upload(filename, file_size):
    """初始化上传"""
    response = requests.post(
        f'{BASE_URL}/uploads:init',
        json={
            'filename': filename,
            'size': file_size,
            'mime': 'application/pdf',
            'tenantId': 'tenant-001',
            'ownerId': 'user-123',
            'bizTags': ['test']
        },
        headers={
            'Authorization': f'Bearer {TOKEN}',
            'Content-Type': 'application/json'
        }
    )
    return response.json()['data']

def upload_file(presigned_url, file_path):
    """上传文件到MinIO"""
    with open(file_path, 'rb') as f:
        response = requests.put(
            presigned_url,
            data=f,
            headers={'Content-Type': 'application/pdf'}
        )
    return response.headers.get('ETag')

def complete_upload(upload_id):
    """完成上传"""
    response = requests.post(
        f'{BASE_URL}/uploads/{upload_id}:complete',
        json={
            'checksum': {
                'sha256': 'file_hash_here'
            }
        },
        headers={
            'Authorization': f'Bearer {TOKEN}',
            'Content-Type': 'application/json'
        }
    )
    return response.json()['data']

# 完整流程
def upload_file_complete(file_path):
    # 获取文件大小
    import os
    file_size = os.path.getsize(file_path)
    
    # 步骤1: 初始化
    init_data = init_upload('test.pdf', file_size)
    print(f"Upload initialized: {init_data['uploadId']}")
    
    # 步骤2: 上传文件
    presigned_url = init_data['parts'][0]['url']
    etag = upload_file(presigned_url, file_path)
    print(f"File uploaded, ETag: {etag}")
    
    # 步骤3: 完成上传
    result = complete_upload(init_data['uploadId'])
    print(f"Upload completed: {result['fileId']}")
    print(f"Download URL: {result['downloadUrl']}")

upload_file_complete('./test.pdf')
```

---

## 字段说明

### 请求字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| filename | string | 是 | 文件名 |
| size | long | 是 | 文件大小（字节） |
| mime | string | 否 | MIME类型 |
| tenantId | string | 是 | 租户ID |
| ownerId | string | 否 | 所有者ID |
| bizTags | array | 否 | 业务标签 |
| multipart.enabled | boolean | 否 | 是否启用分片上传 |
| multipart.partSize | integer | 否 | 分片大小（字节），默认10MB |
| checksum | object | 否 | 文件校验和 |
| acl | object | 否 | 访问控制列表 |
| ext | object | 否 | 扩展信息 |

### 响应字段

| 字段 | 类型 | 说明 |
|------|------|------|
| uploadId | string | 上传会话ID |
| fileId | string | 文件ID |
| bucket | string | 存储桶名称 |
| parts | array | 预签名URL列表 |
| expireAt | datetime | URL过期时间 |

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 上传会话不存在 |
| 413 | 文件过大，超出配额 |
| 500 | 服务器内部错误 |

---

## 注意事项

1. **预签名URL有效期**: 默认1小时，过期后需要调用刷新接口
2. **分片大小建议**: 10MB - 100MB
3. **并发上传**: 分片上传支持并发，建议最多10个并发
4. **文件大小限制**: 根据租户配额设置
5. **支持的文件类型**: 所有类型，但建议设置正确的MIME类型
6. **ETag保存**: 分片上传时必须保存每个分片的ETag用于完成上传

---

## Swagger 文档

启动服务后访问: http://localhost:8084/api/v1/media/swagger-ui.html
