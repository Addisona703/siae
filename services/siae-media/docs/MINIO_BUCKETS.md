# MinIO Buckets 配置说明

## Bucket 列表

### 1. siae-media（主存储桶）

**用途**：存储所有用户上传的原始文件

**配置**：
- 访问策略：Private（私有）
- 版本控制：建议启用
- 加密：建议启用 SSE-S3
- 对象锁定：根据需求启用

**存储内容**：
- 用户上传的图片、视频、音频、文档等
- 文件路径格式：`{tenantId}/{year}/{month}/{day}/{fileId}.{ext}`

**示例路径**：
```
siae-media/
├── tenant-001/
│   ├── 2025/
│   │   ├── 01/
│   │   │   ├── 15/
│   │   │   │   ├── abc123.jpg
│   │   │   │   ├── def456.mp4
│   │   │   │   └── ghi789.pdf
```

---

### 2. siae-media-derivatives（衍生文件存储桶）

**用途**：存储由原始文件生成的衍生文件

**配置**：
- 访问策略：Public-Read（公开读）或 Private
- 版本控制：不需要
- 生命周期：可设置较短保留期（如 90 天）

**存储内容**：
- 图片缩略图（64x64, 128x128, 256x256, 512x512）
- 视频转码文件（不同分辨率、格式）
- 视频封面图
- 音频波形图
- 文档预览图

**示例路径**：
```
siae-media-derivatives/
├── thumbs/
│   ├── abc123_64.jpg
│   ├── abc123_128.jpg
│   ├── abc123_256.jpg
│   └── abc123_512.jpg
├── transcoded/
│   ├── def456_720p.mp4
│   ├── def456_480p.mp4
│   └── def456_360p.mp4
└── previews/
    ├── def456_cover.jpg
    └── ghi789_preview.png
```

---

### 3. siae-media-temp（临时文件存储桶）

**用途**：存储分片上传过程中的临时文件

**配置**：
- 访问策略：Private（私有）
- 版本控制：不需要
- 生命周期：自动删除 7 天前的文件
- 对象锁定：不需要

**存储内容**：
- 分片上传的临时分片
- 上传失败或中断的残留文件

**示例路径**：
```
siae-media-temp/
├── uploads/
│   ├── upload-001/
│   │   ├── part-1
│   │   ├── part-2
│   │   └── part-3
```

**自动清理**：
- 生命周期策略会自动删除 7 天前的文件
- 避免临时文件占用存储空间

---

## 创建 Buckets

### 方式 1：自动创建（推荐）

应用启动时会自动创建所需的 Buckets，无需手动操作。

查看日志确认：
```
✓ 创建 Bucket 成功: siae-media - 主存储桶
✓ 创建 Bucket 成功: siae-media-derivatives - 衍生文件存储桶
✓ 创建 Bucket 成功: siae-media-temp - 临时文件存储桶
✓ 设置 Bucket 生命周期策略成功: siae-media-temp (7天后自动删除)
```

### 方式 2：手动创建

1. 访问 MinIO Console：`http://localhost:9000`
2. 登录（默认：minioadmin/minioadmin）
3. 点击 "Buckets" → "Create Bucket"
4. 创建以下 buckets：
   - `siae-media`
   - `siae-media-derivatives`
   - `siae-media-temp`

### 方式 3：使用 mc 命令行工具

```bash
# 配置 MinIO 客户端
mc alias set local http://localhost:9000 minioadmin minioadmin

# 创建 buckets
mc mb local/siae-media
mc mb local/siae-media-derivatives
mc mb local/siae-media-temp

# 设置临时文件桶的生命周期（7天后删除）
mc ilm add --expiry-days 7 local/siae-media-temp

# 查看 buckets
mc ls local
```

---

## Bucket 访问策略

### 主存储桶（siae-media）

**策略**：Private（私有）

所有文件访问都需要通过签名 URL：
- 上传：使用预签名 PUT URL
- 下载：使用预签名 GET URL
- 流式播放：使用预签名 GET URL（支持 Range 请求）

### 衍生文件桶（siae-media-derivatives）

**选项 1：Private（推荐）**
- 更安全，所有访问都需要签名
- 适合包含敏感信息的衍生文件

**选项 2：Public-Read**
- 提高访问速度，减少签名开销
- 适合公开的缩略图、预览图
- 可以直接通过 URL 访问

设置公开读策略：
```bash
mc anonymous set download local/siae-media-derivatives
```

---

## 存储空间规划

### 估算示例

假设：
- 用户数：10,000
- 每用户平均文件：100 个
- 平均文件大小：5 MB

**主存储桶**：
```
10,000 用户 × 100 文件 × 5 MB = 5 TB
```

**衍生文件桶**：
```
缩略图：1,000,000 文件 × 4 尺寸 × 50 KB = 200 GB
转码视频：假设 10% 视频 × 3 分辨率 × 10 MB = 300 GB
总计：约 500 GB
```

**临时文件桶**：
```
峰值上传：100 并发 × 100 MB = 10 GB
（自动清理，实际占用更少）
```

---

## 监控和维护

### 查看 Bucket 使用情况

```bash
# 查看 bucket 大小
mc du local/siae-media
mc du local/siae-media-derivatives
mc du local/siae-media-temp

# 查看对象数量
mc ls --recursive local/siae-media | wc -l
```

### 清理建议

1. **主存储桶**：
   - 根据业务需求设置归档策略
   - 定期清理已删除文件（软删除后 30 天）

2. **衍生文件桶**：
   - 可以设置 90 天生命周期
   - 源文件删除时同步删除衍生文件

3. **临时文件桶**：
   - 自动清理 7 天前的文件
   - 无需手动维护

---

## 备份策略

### 主存储桶备份

```bash
# 使用 mc mirror 进行增量备份
mc mirror local/siae-media /backup/siae-media

# 或使用 mc cp 复制到另一个 MinIO 实例
mc cp --recursive local/siae-media remote/siae-media-backup
```

### 版本控制

启用版本控制可以防止误删除：
```bash
mc version enable local/siae-media
```

---

## 故障排查

### Bucket 不存在

**错误**：`The specified bucket does not exist`

**解决**：
1. 检查应用日志，确认自动创建是否成功
2. 手动创建 bucket
3. 检查 MinIO 服务是否正常运行

### 权限不足

**错误**：`Access Denied`

**解决**：
1. 检查 MinIO 访问密钥是否正确
2. 确认 bucket 访问策略
3. 检查文件路径是否正确

### 生命周期策略未生效

**解决**：
1. 检查 MinIO 版本（需要 RELEASE.2020-06-01 或更高）
2. 手动设置生命周期策略
3. 查看 MinIO 日志确认策略执行情况

---

**最后更新**：2025-01-XX  
**维护人员**：SIAE 开发团队
