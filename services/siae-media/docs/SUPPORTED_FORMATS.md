# 支持的文件格式

Media Service 支持以下文件格式的自动处理和预览生成。

## 图片格式

所有以 `image/` 开头的 MIME 类型都支持，包括但不限于：

- **JPEG/JPG** - `image/jpeg`
- **PNG** - `image/png`
- **GIF** - `image/gif`
- **WebP** - `image/webp`
- **BMP** - `image/bmp`
- **TIFF** - `image/tiff`
- **SVG** - `image/svg+xml`
- **ICO** - `image/x-icon`

**处理功能**：
- 自动生成多尺寸缩略图（64x64, 128x128, 256x256, 512x512）
- 保持原始宽高比
- 优化文件大小

## 视频格式

所有以 `video/` 开头的 MIME 类型都支持，包括但不限于：

- **MP4** - `video/mp4`
- **WebM** - `video/webm`
- **AVI** - `video/x-msvideo`
- **MOV** - `video/quicktime`
- **MKV** - `video/x-matroska`
- **FLV** - `video/x-flv`
- **WMV** - `video/x-ms-wmv`

**处理功能**：
- 提取关键帧作为缩略图
- 生成低分辨率预览视频（480p）
- 提取视频元数据（时长、分辨率、编码格式等）

## 音频格式

所有以 `audio/` 开头的 MIME 类型都支持，包括但不限于：

- **MP3** - `audio/mpeg`
- **WAV** - `audio/wav`
- **OGG** - `audio/ogg`
- **AAC** - `audio/aac`
- **FLAC** - `audio/flac`
- **M4A** - `audio/mp4`
- **WMA** - `audio/x-ms-wma`

**处理功能**：
- 生成音频波形图
- 提取音频元数据（时长、比特率、采样率等）

## 文档格式

### PDF 文档
- **PDF** - `application/pdf`

### Microsoft Office 文档
- **Word (.doc)** - `application/msword`
- **Word (.docx)** - `application/vnd.openxmlformats-officedocument.wordprocessingml.document`
- **Excel (.xls)** - `application/vnd.ms-excel`
- **Excel (.xlsx)** - `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- **PowerPoint (.ppt)** - `application/vnd.ms-powerpoint`
- **PowerPoint (.pptx)** - `application/vnd.openxmlformats-officedocument.presentationml.presentation`

### OpenDocument 格式
- **Writer (.odt)** - `application/vnd.oasis.opendocument.text`
- **Calc (.ods)** - `application/vnd.oasis.opendocument.spreadsheet`
- **Impress (.odp)** - `application/vnd.oasis.opendocument.presentation`

### 文本文档
- **纯文本 (.txt)** - `text/plain`
- **Markdown (.md)** - `text/markdown`
- **CSV (.csv)** - `text/csv`
- **RTF (.rtf)** - `application/rtf`

**处理功能**：
- 生成文档首页预览图
- 提取文档元数据（页数、作者、标题等）
- 支持文本内容提取（用于搜索）

## 其他格式

对于不在上述列表中的文件格式：
- 仍然可以上传和存储
- 不会自动生成预览
- 可以通过原始文件下载

## 文件大小限制

- **单文件上传限制**：默认 5GB
- **分片上传**：支持大于 5GB 的文件
- **缩略图生成**：建议原图不超过 50MB
- **视频处理**：建议视频不超过 2GB

## 扩展支持

如需支持其他文件格式，请：
1. 在 `MediaProcessService.isDocumentType()` 中添加 MIME 类型
2. 实现相应的处理逻辑
3. 更新本文档

## 注意事项

1. **安全扫描**：所有上传的文件都会经过病毒扫描和内容审核
2. **处理时间**：大文件的处理可能需要几分钟时间
3. **存储空间**：衍生文件（缩略图、预览等）会占用额外的存储空间
4. **格式转换**：某些格式可能需要安装额外的处理工具（如 FFmpeg、ImageMagick）

## 相关配置

在 `application.yml` 中可以配置：
```yaml
media:
  processing:
    enabled: true
    thumbnail-sizes: [64, 128, 256, 512]
    video-preview-resolution: 480p
    max-processing-time: 300s
```
