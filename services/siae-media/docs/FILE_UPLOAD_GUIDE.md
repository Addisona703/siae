# 文件上传完整指南

本文档详细说明媒体服务的文件上传流程，包括单文件上传和分片上传两种方式。

---

## 目录

1. [上传流程概述](#上传流程概述)
2. [单文件上传](#单文件上传)
3. [分片上传](#分片上传)
4. [API 接口详解](#api-接口详解)
5. [前端完整示例](#前端完整示例)
6. [错误处理](#错误处理)
7. [最佳实践](#最佳实践)

---

## 上传流程概述

### 上传方式选择

- **单文件上传**：适用于小文件（< 100MB）
- **分片上传**：适用于大文件（≥ 100MB），支持断点续传

### 基本流程

```
1. 初始化上传 (POST /api/v1/media/uploads/init)
   ↓
2. 使用预签名URL上传文件到对象存储
   ↓
3. 完成上传 (POST /api/v1/media/uploads/{uploadId}/complete)
```

---

## 单文件上传

### 步骤 1: 初始化上传

#### 请求接口

```
POST /api/v1/media/uploads/init
Content-Type: application/json
Authorization: Bearer {token}
```

#### 请求参数 (UploadInitDTO)

| 参数 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `filename` | String | ✅ | 原始文件名（含扩展名） | `"avatar.jpg"` |
| `size` | Long | ✅ | 文件大小（字节） | `1048576` (1MB) |
| `mime` | String | ❌ | 文件MIME类型 | `"image/jpeg"` |
| `tenantId` | String | ✅ | 租户ID | `"tenant-001"` |
| `ownerId` | String | ❌ | 文件所有者用户ID | `"user-123"` |
| `accessPolicy` | Enum | ❌ | 访问策略，默认PRIVATE | `"PUBLIC"` / `"PRIVATE"` |
| `bizTags` | List<String> | ❌ | 业务标签 | `["avatar", "profile"]` |
| `multipart` | Object | ❌ | 分片配置（单文件上传不填） | `null` |
| `checksum` | Map | ❌ | 文件校验和 | `{"sha256": "abc123..."}` |
| `acl` | Map | ❌ | 访问控制策略 | `{"allowUsers": ["user1"]}` |
| `ext` | Map | ❌ | 自定义扩展信息 | `{"category": "profile"}` |

#### 请求示例

```json
{
  "filename": "avatar.jpg",
  "size": 1048576,
  "mime": "image/jpeg",
  "tenantId": "tenant-001",
  "ownerId": "user-123",
  "accessPolicy": "PUBLIC",
  "bizTags": ["avatar", "profile"],
  "checksum": {
    "sha256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
  }
}
```

#### 响应参数 (UploadInitVO)

| 参数 | 类型 | 说明 |
|------|------|------|
| `uploadId` | String | 上传会话ID |
| `fileId` | String | 文件ID |
| `bucket` | String | 存储桶名称 |
| `parts` | List | 预签名URL列表（单文件只有1个） |
| `parts[].partNumber` | Integer | 分片序号（单文件为1） |
| `parts[].url` | String | 预签名上传URL |
| `parts[].expiresAt` | DateTime | URL过期时间 |
| `headers` | Map | 上传时需要的HTTP头 |
| `expireAt` | DateTime | 整体过期时间 |

#### 响应示例

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
        "url": "https://minio.example.com/siae-media/uploads/file-xyz789?X-Amz-Algorithm=...",
        "expiresAt": "2024-11-26T21:00:00"
      }
    ],
    "headers": {
      "Content-Type": "image/jpeg"
    },
    "expireAt": "2024-11-26T21:00:00"
  }
}
```

### 步骤 2: 上传文件到对象存储

使用步骤1返回的预签名URL，直接通过HTTP PUT上传文件到对象存储。

**注意**：这一步是直接上传到MinIO/S3，不经过后端服务器。

### 步骤 3: 完成上传

#### 请求接口

```
POST /api/v1/media/uploads/{uploadId}/complete
Content-Type: application/json
Authorization: Bearer {token}
```

#### 请求参数 (UploadCompleteDTO)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `parts` | List | ❌ | 分片信息（**单文件上传可不填或传空数组**） |
| `parts[].partNumber` | Integer | ✅ | 分片序号（仅分片上传需要） |
| `parts[].etag` | String | ✅ | 对象存储返回的ETag（仅分片上传需要） |
| `checksum` | Map | ❌ | 最终文件校验和（可选） |

#### 请求示例

**单文件上传（推荐）**：
```json
{
  "checksum": {
    "sha256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
  }
}
```

或者传空对象：
```json
{}
```

**分片上传**：
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
    "sha256": "final-file-sha256-hash"
  }
}
```

#### 响应参数 (UploadCompleteVO)

| 参数 | 类型 | 说明 |
|------|------|------|
| `fileId` | String | 文件ID |
| `status` | Enum | 文件状态 (COMPLETED/PROCESSING) |
| `url` | String | 文件访问URL |
| `urlExpiresAt` | DateTime | URL过期时间（仅私有文件） |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "fileId": "file-xyz789",
    "status": "COMPLETED",
    "url": "https://minio.example.com/siae-media/public/file-xyz789.jpg",
    "urlExpiresAt": null
  }
}
```

---

## 分片上传

分片上传适用于大文件（≥ 100MB），支持断点续传和并发上传。

### 步骤 1: 初始化分片上传

#### 请求参数

与单文件上传类似，但需要添加 `multipart` 配置：

```json
{
  "filename": "large-video.mp4",
  "size": 524288000,
  "mime": "video/mp4",
  "tenantId": "tenant-001",
  "ownerId": "user-123",
  "accessPolicy": "PRIVATE",
  "bizTags": ["video", "course"],
  "multipart": {
    "enabled": true,
    "partSize": 5242880
  }
}
```

#### multipart 配置说明

| 参数 | 类型 | 必填 | 说明 | 推荐值 |
|------|------|------|------|--------|
| `enabled` | Boolean | ✅ | 是否启用分片上传 | `true` |
| `partSize` | Integer | ✅ | 单个分片大小（字节） | `5242880` (5MB) |

**分片大小建议**：
- 最小：5MB (5242880 字节)
- 推荐：5-10MB
- 最大：100MB

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uploadId": "upload-multipart-abc123",
    "fileId": "file-large-xyz789",
    "bucket": "siae-media",
    "parts": [
      {
        "partNumber": 1,
        "url": "https://minio.example.com/siae-media/uploads/file-large-xyz789?partNumber=1&uploadId=...",
        "expiresAt": "2024-11-26T21:00:00"
      },
      {
        "partNumber": 2,
        "url": "https://minio.example.com/siae-media/uploads/file-large-xyz789?partNumber=2&uploadId=...",
        "expiresAt": "2024-11-26T21:00:00"
      }
      // ... 更多分片
    ],
    "headers": {
      "Content-Type": "video/mp4"
    },
    "expireAt": "2024-11-26T21:00:00"
  }
}
```

### 步骤 2: 上传各个分片

使用返回的每个分片的预签名URL，并发上传文件分片到对象存储。

**重要**：
- 每个分片必须使用对应的 `partNumber` 的URL
- 保存每个分片上传后返回的 `ETag`
- 支持并发上传多个分片

### 步骤 3: 刷新过期的URL（可选）

如果上传过程中URL过期，可以刷新URL。

#### 请求接口

```
POST /api/v1/media/uploads/{uploadId}/refresh
Content-Type: application/json
Authorization: Bearer {token}
```

#### 请求参数 (UploadRefreshDTO)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `parts` | List | ❌ | 需要刷新的分片列表，为空则刷新全部 |
| `parts[].partNumber` | Integer | ✅ | 分片序号 |

#### 请求示例

```json
{
  "parts": [
    { "partNumber": 3 },
    { "partNumber": 5 }
  ]
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uploadId": "upload-multipart-abc123",
    "parts": [
      {
        "partNumber": 3,
        "url": "https://minio.example.com/siae-media/uploads/file-large-xyz789?partNumber=3&uploadId=...",
        "expiresAt": "2024-11-26T22:00:00"
      },
      {
        "partNumber": 5,
        "url": "https://minio.example.com/siae-media/uploads/file-large-xyz789?partNumber=5&uploadId=...",
        "expiresAt": "2024-11-26T22:00:00"
      }
    ],
    "expiresAt": "2024-11-26T22:00:00"
  }
}

### 步骤 4: 完成分片上传

#### 请求参数

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
    // ... 所有分片的ETag
  ],
  "checksum": {
    "sha256": "final-file-sha256-hash"
  }
}
```

**注意**：
- 必须提供所有分片的 `partNumber` 和 `etag`
- 分片顺序必须正确
- ETag 必须包含双引号

---

## API 接口详解

### 1. 初始化上传

```
POST /api/v1/media/uploads/init
```

**功能**：创建上传会话，生成预签名URL

**权限**：需要 `media:upload` 权限

**超时**：30秒

### 2. 刷新上传URL

```
POST /api/v1/media/uploads/{uploadId}/refresh
```

**功能**：刷新过期的预签名URL

**使用场景**：
- URL过期（默认1小时）
- 网络中断后恢复上传
- 追加新的分片

### 3. 完成上传

```
POST /api/v1/media/uploads/{uploadId}/complete
```

**功能**：通知服务器上传完成，合并分片

**处理流程**：
1. 验证所有分片完整性
2. 合并分片（分片上传）
3. 更新文件状态为 COMPLETED
4. 返回文件访问URL

### 4. 中断上传

```
POST /api/v1/media/uploads/{uploadId}/abort
```

**功能**：取消上传，清理临时文件

**使用场景**：
- 用户主动取消上传
- 上传失败需要重新开始

---

## 前端完整示例

### 1. 单文件上传 - JavaScript/TypeScript

```typescript
/**
 * 单文件上传工具类
 */
class SimpleFileUploader {
  private apiBaseUrl: string;
  private token: string;

  constructor(apiBaseUrl: string, token: string) {
    this.apiBaseUrl = apiBaseUrl;
    this.token = token;
  }

  /**
   * 上传单个文件
   */
  async uploadFile(
    file: File,
    options: {
      tenantId: string;
      ownerId?: string;
      accessPolicy?: 'PUBLIC' | 'PRIVATE';
      bizTags?: string[];
      onProgress?: (progress: number) => void;
    }
  ): Promise<{ fileId: string; url: string }> {
    try {
      // 步骤1: 初始化上传
      console.log('初始化上传...');
      const initResponse = await this.initUpload(file, options);
      
      // 步骤2: 上传文件到对象存储
      console.log('上传文件到对象存储...');
      const uploadUrl = initResponse.parts[0].url;
      const etag = await this.uploadToStorage(file, uploadUrl, options.onProgress);
      
      // 步骤3: 完成上传（单文件上传可以传空对象）
      console.log('完成上传...');
      const completeResponse = await this.completeUpload(initResponse.uploadId);
      
      return {
        fileId: completeResponse.fileId,
        url: completeResponse.url
      };
    } catch (error) {
      console.error('上传失败:', error);
      throw error;
    }
  }

  /**
   * 初始化上传
   */
  private async initUpload(file: File, options: any) {
    const response = await fetch(`${this.apiBaseUrl}/uploads/init`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.token}`
      },
      body: JSON.stringify({
        filename: file.name,
        size: file.size,
        mime: file.type,
        tenantId: options.tenantId,
        ownerId: options.ownerId,
        accessPolicy: options.accessPolicy || 'PRIVATE',
        bizTags: options.bizTags || []
      })
    });

    if (!response.ok) {
      throw new Error(`初始化上传失败: ${response.statusText}`);
    }

    const result = await response.json();
    return result.data;
  }

  /**
   * 上传文件到对象存储
   */
  private async uploadToStorage(
    file: File,
    url: string,
    onProgress?: (progress: number) => void
  ): Promise<string> {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();

      // 监听上传进度
      if (onProgress) {
        xhr.upload.addEventListener('progress', (e) => {
          if (e.lengthComputable) {
            const progress = Math.round((e.loaded / e.total) * 100);
            onProgress(progress);
          }
        });
      }

      xhr.addEventListener('load', () => {
        if (xhr.status === 200) {
          // 单文件上传成功，不需要 ETag
          resolve('');
        } else {
          reject(new Error(`上传失败: ${xhr.statusText}`));
        }
      });

      xhr.addEventListener('error', () => {
        reject(new Error('上传失败'));
      });

      xhr.open('PUT', url);
      xhr.setRequestHeader('Content-Type', file.type);
      xhr.send(file);
    });
  }

  /**
   * 完成上传
   * 单文件上传时传空对象即可，后端会自动处理
   */
  private async completeUpload(uploadId: string) {
    const response = await fetch(`${this.apiBaseUrl}/uploads/${uploadId}/complete`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.token}`
      },
      body: JSON.stringify({})  // 单文件上传传空对象
    });

    if (!response.ok) {
      throw new Error(`完成上传失败: ${response.statusText}`);
    }

    const result = await response.json();
    return result.data;
  }
}

// 使用示例
const uploader = new SimpleFileUploader(
  'https://api.example.com/api/v1/media',
  'your-auth-token'
);

// 上传文件
const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
fileInput.addEventListener('change', async (e) => {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;

  try {
    const result = await uploader.uploadFile(file, {
      tenantId: 'tenant-001',
      ownerId: 'user-123',
      accessPolicy: 'PUBLIC',
      bizTags: ['avatar'],
      onProgress: (progress) => {
        console.log(`上传进度: ${progress}%`);
      }
    });

    console.log('上传成功:', result);
    console.log('文件ID:', result.fileId);
    console.log('访问URL:', result.url);
  } catch (error) {
    console.error('上传失败:', error);
  }
});
```

---

### 2. 分片上传 - JavaScript/TypeScript

```typescript
/**
 * 分片上传工具类
 */
class MultipartFileUploader {
  private apiBaseUrl: string;
  private token: string;
  private chunkSize: number = 5 * 1024 * 1024; // 5MB
  private concurrency: number = 3; // 并发上传数

  constructor(apiBaseUrl: string, token: string) {
    this.apiBaseUrl = apiBaseUrl;
    this.token = token;
  }

  /**
   * 分片上传文件
   */
  async uploadFile(
    file: File,
    options: {
      tenantId: string;
      ownerId?: string;
      accessPolicy?: 'PUBLIC' | 'PRIVATE';
      bizTags?: string[];
      chunkSize?: number;
      onProgress?: (progress: number) => void;
      onChunkComplete?: (chunkIndex: number, total: number) => void;
    }
  ): Promise<{ fileId: string; url: string }> {
    if (options.chunkSize) {
      this.chunkSize = options.chunkSize;
    }

    try {
      // 步骤1: 初始化分片上传
      console.log('初始化分片上传...');
      const initResponse = await this.initMultipartUpload(file, options);
      
      // 步骤2: 分片上传
      console.log(`开始上传 ${initResponse.parts.length} 个分片...`);
      const uploadedParts = await this.uploadChunks(
        file,
        initResponse.parts,
        options
      );
      
      // 步骤3: 完成上传
      console.log('合并分片...');
      const completeResponse = await this.completeUpload(
        initResponse.uploadId,
        uploadedParts
      );
      
      return {
        fileId: completeResponse.fileId,
        url: completeResponse.url
      };
    } catch (error) {
      console.error('分片上传失败:', error);
      throw error;
    }
  }

  /**
   * 初始化分片上传
   */
  private async initMultipartUpload(file: File, options: any) {
    const response = await fetch(`${this.apiBaseUrl}/uploads/init`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.token}`
      },
      body: JSON.stringify({
        filename: file.name,
        size: file.size,
        mime: file.type,
        tenantId: options.tenantId,
        ownerId: options.ownerId,
        accessPolicy: options.accessPolicy || 'PRIVATE',
        bizTags: options.bizTags || [],
        multipart: {
          enabled: true,
          partSize: this.chunkSize
        }
      })
    });

    if (!response.ok) {
      throw new Error(`初始化分片上传失败: ${response.statusText}`);
    }

    const result = await response.json();
    return result.data;
  }

  /**
   * 上传所有分片
   */
  private async uploadChunks(
    file: File,
    parts: Array<{ partNumber: number; url: string }>,
    options: any
  ): Promise<Array<{ partNumber: number; etag: string }>> {
    const uploadedParts: Array<{ partNumber: number; etag: string }> = [];
    const totalParts = parts.length;
    let completedParts = 0;

    // 使用并发控制上传分片
    const queue = [...parts];
    const workers: Promise<void>[] = [];

    for (let i = 0; i < this.concurrency; i++) {
      workers.push(this.uploadWorker(file, queue, uploadedParts, () => {
        completedParts++;
        
        // 更新总体进度
        if (options.onProgress) {
          const progress = Math.round((completedParts / totalParts) * 100);
          options.onProgress(progress);
        }
        
        // 通知单个分片完成
        if (options.onChunkComplete) {
          options.onChunkComplete(completedParts, totalParts);
        }
      }));
    }

    await Promise.all(workers);

    // 按 partNumber 排序
    return uploadedParts.sort((a, b) => a.partNumber - b.partNumber);
  }

  /**
   * 上传工作线程
   */
  private async uploadWorker(
    file: File,
    queue: Array<{ partNumber: number; url: string }>,
    results: Array<{ partNumber: number; etag: string }>,
    onComplete: () => void
  ): Promise<void> {
    while (queue.length > 0) {
      const part = queue.shift();
      if (!part) break;

      try {
        const chunk = this.getFileChunk(file, part.partNumber);
        const etag = await this.uploadChunk(chunk, part.url);
        
        results.push({
          partNumber: part.partNumber,
          etag
        });
        
        onComplete();
      } catch (error) {
        console.error(`分片 ${part.partNumber} 上传失败:`, error);
        // 重新加入队列重试
        queue.push(part);
      }
    }
  }

  /**
   * 获取文件分片
   */
  private getFileChunk(file: File, partNumber: number): Blob {
    const start = (partNumber - 1) * this.chunkSize;
    const end = Math.min(start + this.chunkSize, file.size);
    return file.slice(start, end);
  }

  /**
   * 上传单个分片
   */
  private async uploadChunk(chunk: Blob, url: string): Promise<string> {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();

      xhr.addEventListener('load', () => {
        if (xhr.status === 200) {
          const etag = xhr.getResponseHeader('ETag');
          resolve(etag || '');
        } else {
          reject(new Error(`分片上传失败: ${xhr.statusText}`));
        }
      });

      xhr.addEventListener('error', () => {
        reject(new Error('分片上传失败'));
      });

      xhr.open('PUT', url);
      xhr.send(chunk);
    });
  }

  /**
   * 完成上传
   */
  private async completeUpload(
    uploadId: string,
    parts: Array<{ partNumber: number; etag: string }>
  ) {
    const response = await fetch(`${this.apiBaseUrl}/uploads/${uploadId}/complete`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.token}`
      },
      body: JSON.stringify({ parts })
    });

    if (!response.ok) {
      throw new Error(`完成上传失败: ${response.statusText}`);
    }

    const result = await response.json();
    return result.data;
  }

  /**
   * 中断上传
   */
  async abortUpload(uploadId: string): Promise<void> {
    const response = await fetch(`${this.apiBaseUrl}/uploads/${uploadId}/abort`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${this.token}`
      }
    });

    if (!response.ok) {
      throw new Error(`中断上传失败: ${response.statusText}`);
    }
  }
}

// 使用示例
const multipartUploader = new MultipartFileUploader(
  'https://api.example.com/api/v1/media',
  'your-auth-token'
);

// 上传大文件
const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
fileInput.addEventListener('change', async (e) => {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;

  try {
    const result = await multipartUploader.uploadFile(file, {
      tenantId: 'tenant-001',
      ownerId: 'user-123',
      accessPolicy: 'PRIVATE',
      bizTags: ['video', 'course'],
      chunkSize: 5 * 1024 * 1024, // 5MB
      onProgress: (progress) => {
        console.log(`总体进度: ${progress}%`);
        // 更新进度条
        document.querySelector('.progress-bar')?.setAttribute('style', `width: ${progress}%`);
      },
      onChunkComplete: (completed, total) => {
        console.log(`已完成分片: ${completed}/${total}`);
      }
    });

    console.log('上传成功:', result);
    console.log('文件ID:', result.fileId);
    console.log('访问URL:', result.url);
  } catch (error) {
    console.error('上传失败:', error);
  }
});
```

---
```

### 步骤 4: 完成分片上传

#### 请求参数

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
    // ... 所有分片的ETag
  ],
  "checksum": {
    "sha256": "final-file-sha256-hash"
  }
}
```

**注意**：
- 必须提供所有分片的 `partNumber` 和 `etag`
- 分片顺序必须正确
- ETag 必须包含双引号

---

## API 接口详解

### 1. 初始化上传

```
POST /api/v1/media/uploads/init
```

**功能**：创建上传会话，生成预签名URL

**权限**：需要 `media:upload` 权限

**超时**：30秒

### 2. 刷新上传URL

```
POST /api/v1/media/uploads/{uploadId}/refresh
```

**功能**：刷新过期的预签名URL

**使用场景**：
- URL过期（默认1小时）
- 网络中断后恢复上传
- 追加新的分片

### 3. 完成上传

```
POST /api/v1/media/uploads/{uploadId}/complete
```

**功能**：通知服务器上传完成，合并分片

**处理流程**：
1. 验证所有分片完整性
2. 合并分片（分片上传）
3. 更新文件状态为 COMPLETED
4. 返回文件访问URL

### 4. 中断上传

```
POST /api/v1/media/uploads/{uploadId}/abort
```

**功能**：取消上传，清理临时文件

**使用场景**：
- 用户主动取消上传
- 上传失败需要重新开始

---


### 3. Vue 3 组件示例

```vue
<template>
  <div class="file-uploader">
    <div class="upload-area" @click="selectFile">
      <input
        ref="fileInput"
        type="file"
        @change="handleFileChange"
        style="display: none"
      />
      <div v-if="!uploading">
        <i class="upload-icon">📁</i>
        <p>点击选择文件或拖拽文件到此处</p>
        <p class="hint">支持单文件上传和大文件分片上传</p>
      </div>
      <div v-else class="uploading">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
        </div>
        <p>上传中... {{ progress }}%</p>
        <p class="chunk-info" v-if="chunkInfo">
          {{ chunkInfo.completed }}/{{ chunkInfo.total }} 分片已完成
        </p>
        <button @click.stop="cancelUpload" class="btn-cancel">取消上传</button>
      </div>
    </div>

    <div v-if="uploadedFile" class="upload-result">
      <h3>上传成功！</h3>
      <p>文件ID: {{ uploadedFile.fileId }}</p>
      <p>访问URL: <a :href="uploadedFile.url" target="_blank">{{ uploadedFile.url }}</a></p>
    </div>

    <div v-if="error" class="error-message">
      {{ error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { MultipartFileUploader } from './MultipartFileUploader';

const fileInput = ref<HTMLInputElement>();
const uploading = ref(false);
const progress = ref(0);
const chunkInfo = ref<{ completed: number; total: number } | null>(null);
const uploadedFile = ref<{ fileId: string; url: string } | null>(null);
const error = ref('');
const currentUploadId = ref('');

const uploader = new MultipartFileUploader(
  import.meta.env.VITE_API_BASE_URL + '/api/v1/media',
  localStorage.getItem('token') || ''
);

const selectFile = () => {
  fileInput.value?.click();
};

const handleFileChange = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;

  // 重置状态
  uploading.value = true;
  progress.value = 0;
  chunkInfo.value = null;
  uploadedFile.value = null;
  error.value = '';

  try {
    // 根据文件大小选择上传方式
    const isLargeFile = file.size > 100 * 1024 * 1024; // 100MB

    const result = await uploader.uploadFile(file, {
      tenantId: 'tenant-001',
      ownerId: localStorage.getItem('userId') || undefined,
      accessPolicy: 'PUBLIC',
      bizTags: ['user-upload'],
      chunkSize: isLargeFile ? 10 * 1024 * 1024 : 5 * 1024 * 1024,
      onProgress: (p) => {
        progress.value = p;
      },
      onChunkComplete: (completed, total) => {
        chunkInfo.value = { completed, total };
      }
    });

    uploadedFile.value = result;
  } catch (err: any) {
    error.value = err.message || '上传失败';
  } finally {
    uploading.value = false;
  }
};

const cancelUpload = async () => {
  if (currentUploadId.value) {
    try {
      await uploader.abortUpload(currentUploadId.value);
      uploading.value = false;
      error.value = '上传已取消';
    } catch (err: any) {
      error.value = '取消上传失败: ' + err.message;
    }
  }
};
</script>

<style scoped>
.file-uploader {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.upload-area {
  border: 2px dashed #ccc;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.upload-icon {
  font-size: 48px;
}

.hint {
  color: #999;
  font-size: 14px;
}

.uploading {
  padding: 20px;
}

.progress-bar {
  width: 100%;
  height: 20px;
  background-color: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background-color: #409eff;
  transition: width 0.3s;
}

.chunk-info {
  color: #666;
  font-size: 14px;
  margin-top: 10px;
}

.btn-cancel {
  margin-top: 10px;
  padding: 8px 16px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.upload-result {
  margin-top: 20px;
  padding: 20px;
  background-color: #f0f9ff;
  border-radius: 8px;
}

.error-message {
  margin-top: 20px;
  padding: 12px;
  background-color: #fef0f0;
  color: #f56c6c;
  border-radius: 4px;
}
</style>
```

---

### 4. React 组件示例

```tsx
import React, { useState, useRef } from 'react';
import { MultipartFileUploader } from './MultipartFileUploader';

interface UploadResult {
  fileId: string;
  url: string;
}

interface ChunkInfo {
  completed: number;
  total: number;
}

const FileUploader: React.FC = () => {
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [chunkInfo, setChunkInfo] = useState<ChunkInfo | null>(null);
  const [uploadedFile, setUploadedFile] = useState<UploadResult | null>(null);
  const [error, setError] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);
  const uploadIdRef = useRef('');

  const uploader = new MultipartFileUploader(
    process.env.REACT_APP_API_BASE_URL + '/api/v1/media',
    localStorage.getItem('token') || ''
  );

  const selectFile = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 重置状态
    setUploading(true);
    setProgress(0);
    setChunkInfo(null);
    setUploadedFile(null);
    setError('');

    try {
      // 根据文件大小选择上传方式
      const isLargeFile = file.size > 100 * 1024 * 1024; // 100MB

      const result = await uploader.uploadFile(file, {
        tenantId: 'tenant-001',
        ownerId: localStorage.getItem('userId') || undefined,
        accessPolicy: 'PUBLIC',
        bizTags: ['user-upload'],
        chunkSize: isLargeFile ? 10 * 1024 * 1024 : 5 * 1024 * 1024,
        onProgress: (p) => {
          setProgress(p);
        },
        onChunkComplete: (completed, total) => {
          setChunkInfo({ completed, total });
        }
      });

      setUploadedFile(result);
    } catch (err: any) {
      setError(err.message || '上传失败');
    } finally {
      setUploading(false);
    }
  };

  const cancelUpload = async () => {
    if (uploadIdRef.current) {
      try {
        await uploader.abortUpload(uploadIdRef.current);
        setUploading(false);
        setError('上传已取消');
      } catch (err: any) {
        setError('取消上传失败: ' + err.message);
      }
    }
  };

  return (
    <div className="file-uploader">
      <div className="upload-area" onClick={selectFile}>
        <input
          ref={fileInputRef}
          type="file"
          onChange={handleFileChange}
          style={{ display: 'none' }}
        />
        {!uploading ? (
          <div>
            <div className="upload-icon">📁</div>
            <p>点击选择文件或拖拽文件到此处</p>
            <p className="hint">支持单文件上传和大文件分片上传</p>
          </div>
        ) : (
          <div className="uploading">
            <div className="progress-bar">
              <div className="progress-fill" style={{ width: `${progress}%` }}></div>
            </div>
            <p>上传中... {progress}%</p>
            {chunkInfo && (
              <p className="chunk-info">
                {chunkInfo.completed}/{chunkInfo.total} 分片已完成
              </p>
            )}
            <button onClick={(e) => { e.stopPropagation(); cancelUpload(); }} className="btn-cancel">
              取消上传
            </button>
          </div>
        )}
      </div>

      {uploadedFile && (
        <div className="upload-result">
          <h3>上传成功！</h3>
          <p>文件ID: {uploadedFile.fileId}</p>
          <p>
            访问URL: <a href={uploadedFile.url} target="_blank" rel="noreferrer">{uploadedFile.url}</a>
          </p>
        </div>
      )}

      {error && <div className="error-message">{error}</div>}
    </div>
  );
};

export default FileUploader;
```

---

## 错误处理

### 常见错误码

| 错误码 | 说明 | 处理方式 |
|--------|------|----------|
| 400 | 请求参数错误 | 检查请求参数是否完整和正确 |
| 401 | 未授权 | 检查 token 是否有效 |
| 403 | 权限不足 | 检查用户是否有 `media:upload` 权限 |
| 404 | 上传会话不存在 | 重新初始化上传 |
| 413 | 文件过大 | 使用分片上传或减小文件大小 |
| 500 | 服务器错误 | 稍后重试或联系管理员 |
| 503 | 服务不可用 | 稍后重试 |

### 错误处理示例

```typescript
async function uploadWithRetry(
  file: File,
  options: any,
  maxRetries: number = 3
): Promise<any> {
  let lastError: Error | null = null;

  for (let i = 0; i < maxRetries; i++) {
    try {
      return await uploader.uploadFile(file, options);
    } catch (error: any) {
      lastError = error;
      console.error(`上传失败 (尝试 ${i + 1}/${maxRetries}):`, error);

      // 根据错误类型决定是否重试
      if (error.status === 401 || error.status === 403) {
        // 认证/权限错误，不重试
        throw error;
      }

      if (error.status === 413) {
        // 文件过大，不重试
        throw new Error('文件过大，请使用分片上传');
      }

      // 等待后重试
      if (i < maxRetries - 1) {
        await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
      }
    }
  }

  throw lastError || new Error('上传失败');
}
```

### URL 过期处理

```typescript
class SmartUploader extends MultipartFileUploader {
  async uploadChunkWithRefresh(
    uploadId: string,
    partNumber: number,
    chunk: Blob,
    url: string
  ): Promise<string> {
    try {
      return await this.uploadChunk(chunk, url);
    } catch (error: any) {
      // 如果是 URL 过期错误，刷新 URL 后重试
      if (error.status === 403 || error.message.includes('expired')) {
        console.log(`分片 ${partNumber} URL 过期，刷新中...`);
        
        // 刷新 URL
        const refreshResponse = await this.refreshUpload(uploadId, [{ partNumber }]);
        const newUrl = refreshResponse.parts.find(p => p.partNumber === partNumber)?.url;
        
        if (newUrl) {
          return await this.uploadChunk(chunk, newUrl);
        }
      }
      throw error;
    }
  }

  private async refreshUpload(
    uploadId: string,
    parts: Array<{ partNumber: number }>
  ) {
    const response = await fetch(`${this.apiBaseUrl}/uploads/${uploadId}/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.token}`
      },
      body: JSON.stringify({ parts })
    });

    if (!response.ok) {
      throw new Error(`刷新URL失败: ${response.statusText}`);
    }

    const result = await response.json();
    return result.data;
  }
}
```

---

## 最佳实践

### 1. 文件大小判断

```typescript
function shouldUseMultipart(fileSize: number): boolean {
  const threshold = 100 * 1024 * 1024; // 100MB
  return fileSize >= threshold;
}

// 使用
const file = fileInput.files[0];
if (shouldUseMultipart(file.size)) {
  // 使用分片上传
  await multipartUploader.uploadFile(file, options);
} else {
  // 使用单文件上传
  await simpleUploader.uploadFile(file, options);
}
```

### 2. 分片大小选择

```typescript
function calculateChunkSize(fileSize: number): number {
  if (fileSize < 100 * 1024 * 1024) {
    // < 100MB: 5MB 分片
    return 5 * 1024 * 1024;
  } else if (fileSize < 1024 * 1024 * 1024) {
    // 100MB - 1GB: 10MB 分片
    return 10 * 1024 * 1024;
  } else {
    // > 1GB: 20MB 分片
    return 20 * 1024 * 1024;
  }
}
```

### 3. 并发控制

```typescript
// 根据网络状况动态调整并发数
class AdaptiveUploader extends MultipartFileUploader {
  private adjustConcurrency(uploadSpeed: number) {
    if (uploadSpeed > 10 * 1024 * 1024) {
      // 网速 > 10MB/s，使用 5 个并发
      this.concurrency = 5;
    } else if (uploadSpeed > 5 * 1024 * 1024) {
      // 网速 > 5MB/s，使用 3 个并发
      this.concurrency = 3;
    } else {
      // 网速较慢，使用 2 个并发
      this.concurrency = 2;
    }
  }
}
```

### 4. 断点续传

```typescript
// 保存上传进度到 localStorage
function saveUploadProgress(uploadId: string, completedParts: number[]) {
  localStorage.setItem(`upload_${uploadId}`, JSON.stringify({
    completedParts,
    timestamp: Date.now()
  }));
}

// 恢复上传进度
function loadUploadProgress(uploadId: string): number[] | null {
  const data = localStorage.getItem(`upload_${uploadId}`);
  if (!data) return null;

  const progress = JSON.parse(data);
  // 检查是否过期（24小时）
  if (Date.now() - progress.timestamp > 24 * 60 * 60 * 1000) {
    localStorage.removeItem(`upload_${uploadId}`);
    return null;
  }

  return progress.completedParts;
}
```

### 5. 文件校验

```typescript
// 计算文件 SHA256
async function calculateSHA256(file: File): Promise<string> {
  const buffer = await file.arrayBuffer();
  const hashBuffer = await crypto.subtle.digest('SHA-256', buffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

// 使用
const checksum = await calculateSHA256(file);
await uploader.uploadFile(file, {
  ...options,
  checksum: { sha256: checksum }
});
```

### 6. 上传队列管理

```typescript
class UploadQueue {
  private queue: Array<{ file: File; options: any }> = [];
  private uploading = false;
  private maxConcurrent = 2;
  private currentUploads = 0;

  add(file: File, options: any) {
    this.queue.push({ file, options });
    this.processQueue();
  }

  private async processQueue() {
    if (this.uploading || this.queue.length === 0) return;
    if (this.currentUploads >= this.maxConcurrent) return;

    this.uploading = true;
    const item = this.queue.shift();
    
    if (item) {
      this.currentUploads++;
      try {
        await uploader.uploadFile(item.file, item.options);
      } finally {
        this.currentUploads--;
        this.uploading = false;
        this.processQueue();
      }
    }
  }
}
```

---

## 总结

本文档详细介绍了媒体服务的文件上传流程，包括：

1. ✅ **单文件上传**：适用于小文件，流程简单
2. ✅ **分片上传**：适用于大文件，支持断点续传
3. ✅ **完整的前端示例**：TypeScript、Vue 3、React
4. ✅ **错误处理**：常见错误和处理方式
5. ✅ **最佳实践**：性能优化和用户体验提升

### 快速开始

1. 根据文件大小选择上传方式（100MB 为分界线）
2. 使用提供的工具类进行上传
3. 实现进度回调提升用户体验
4. 添加错误处理和重试机制

### 注意事项

- 预签名 URL 默认有效期为 1 小时
- 分片大小建议 5-10MB
- 并发上传数建议 2-5 个
- 及时清理失败的上传会话

---

**文档版本**: v1.0  
**最后更新**: 2024-11-26  
**维护团队**: SIAE Team
