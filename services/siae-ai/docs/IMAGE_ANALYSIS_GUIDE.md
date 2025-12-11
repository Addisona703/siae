# 图片识别功能使用指南

## 功能概述

AI 服务现在支持图片识别功能，用户可以上传图片并让 AI 分析图片内容。

## 实现方式

采用 **Tool（工具）模式**：
- 图片分析被实现为一个 AI 工具 (`ImageAnalysisTool`)
- 模型会根据用户的提问自动判断是否需要调用图片分析工具
- 不支持视觉功能的模型不会调用此工具，自然降级

## 使用流程

### 1. 前端上传图片

```javascript
// 用户上传图片到 siae-media 服务
const formData = new FormData();
formData.append('file', imageFile);

const response = await fetch('/api/v1/media/upload', {
  method: 'POST',
  body: formData
});

const { fileId } = await response.json();
```

### 2. 发送带图片的消息

```javascript
// 发送消息时携带 fileIds
const chatRequest = {
  message: "请分析这张图片，告诉我图片里有什么内容",
  sessionId: "xxx-xxx-xxx",
  fileIds: [fileId]  // 图片文件ID列表
};

const response = await fetch('/api/v1/ai/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(chatRequest)
});
```

### 3. AI 自动分析

- AI 模型收到消息后，会识别用户想要分析图片
- 自动调用 `analyzeImage` 工具
- 工具从 media 服务获取图片数据
- 调用视觉模型分析图片
- 返回分析结果

## 后端架构

### 组件说明

1. **siae-media 服务**
   - 新增接口：`GET /feign/files/{fileId}/bytes`
   - 返回文件的字节数组

2. **api-media (Feign 客户端)**
   - 新增方法：`byte[] getFileBytes(String fileId)`
   - 供其他服务调用

3. **siae-ai 服务**
   - `ChatRequest` DTO 新增 `fileIds` 字段
   - `ImageAnalysisTool` 工具类：分析图片内容
   - 工具自动注册到 Spring AI

### 工具定义

```java
@Tool(description = "分析图片内容，支持图片识别、OCR文字识别、场景理解等。需要提供文件ID和具体问题。")
public String analyzeImage(
    @ToolParam(description = "图片文件ID") String fileId,
    @ToolParam(description = "对图片的具体提问") String question
)
```

## 支持的模型

需要使用支持视觉功能的多模态模型，例如：
- **Ollama**: `llava`, `llava:13b`, `bakllava`
- **OpenAI**: `gpt-4-vision-preview`, `gpt-4o`
- **Claude**: `claude-3-opus`, `claude-3-sonnet`
- **通义千问**: `qwen-vl-plus`, `qwen-vl-max`

## 示例对话

### 示例 1：图片内容识别

```
用户: [上传图片] 这张图片里有什么？
AI: [调用 analyzeImage 工具]
AI: 这张图片显示了一只橙色的猫咪坐在窗台上，背景是蓝天白云...
```

### 示例 2：OCR 文字识别

```
用户: [上传图片] 帮我识别图片中的文字
AI: [调用 analyzeImage 工具]
AI: 图片中的文字内容如下：
    标题：产品使用说明
    1. 打开包装...
```

### 示例 3：场景理解

```
用户: [上传图片] 这是在哪里拍的？
AI: [调用 analyzeImage 工具]
AI: 从图片来看，这应该是在海边拍摄的。可以看到沙滩、海浪和远处的灯塔...
```

## 错误处理

### 文件不存在
```json
{
  "error": "无法获取图片数据，文件可能不存在或为空"
}
```

### 模型不支持视觉
- 模型不会调用 `analyzeImage` 工具
- 会回复类似："抱歉，我无法查看图片内容"

### 媒体服务不可用
```json
{
  "error": "媒体服务暂时不可用，请稍后重试"
}
```

## 配置说明

### application.yaml

```yaml
siae:
  ai:
    model: llava:13b  # 使用支持视觉的模型
    tools:
      enabled: true   # 启用工具调用
```

## 性能优化建议

1. **图片大小限制**：建议前端压缩图片到 2MB 以内
2. **缓存策略**：相同图片的分析结果可以缓存
3. **异步处理**：大图片分析可以使用流式响应

## 安全考虑

1. **权限验证**：确保用户只能访问自己上传的图片
2. **文件类型检查**：只允许图片类型文件
3. **大小限制**：防止超大文件消耗资源

## 未来扩展

- [ ] 支持批量图片分析
- [ ] 支持图片对比
- [ ] 支持视频帧分析
- [ ] 支持图片生成（DALL-E、Stable Diffusion）
