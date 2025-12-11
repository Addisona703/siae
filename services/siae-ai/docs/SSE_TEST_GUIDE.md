# SSE 流式聊天测试指南

## 问题说明
- ✅ 已修复：SSE 响应不再返回 `ServerSentEvent` 对象，改为直接返回纯文本流
- ✅ 已修复：添加了空内容过滤，避免发送空数据块
- ✅ 已确认：会话上下文在流式传输完成后正确保存到 Redis

## 修改内容

### 1. ChatController.java
- 返回类型：`Flux<ServerSentEvent<String>>` → `Flux<String>`
- 添加了 `.filter()` 过滤空内容

### 2. ChatServiceImpl.java
- 添加了详细的调试日志
- 添加了空内容过滤
- 优化了会话保存逻辑

## Apifox 测试步骤

### 测试 1: 流式聊天（首次对话）

**请求：**
```
GET http://localhost/api/v1/ai/chat/stream?message=你好
Authorization: Bearer {your_token}
```

**预期响应：**
每个 chunk 都是 JSON 格式：
```json
{"sessionId":"xxx-xxx-xxx","role":"assistant","content":"你好","isFinal":false}
{"sessionId":"xxx-xxx-xxx","role":"assistant","content":"！","isFinal":false}
{"sessionId":"xxx-xxx-xxx","role":"assistant","content":"我是","isFinal":false}
...
{"sessionId":"xxx-xxx-xxx","role":"assistant","content":"","isFinal":true}
```

**注意事项：**
- Content-Type 应该是 `text/event-stream`
- 每个 chunk 都是完整的 JSON 对象
- `isFinal: true` 表示流式传输结束
- 最后一条消息的 `content` 为空，只用于标记结束
- Apifox 会自动处理 SSE 格式

### 测试 2: 带会话 ID 的流式聊天

**步骤 1：** 首次对话（不带 sessionId）
```
GET http://localhost/api/v1/ai/chat/stream?message=我的名字是张三
```

**步骤 2：** 查看日志获取 sessionId
在服务日志中找到类似这样的输出：
```
Created new conversation session in Redis: {sessionId} for user: {userId}
```

**步骤 3：** 使用相同 sessionId 继续对话
```
GET http://localhost/api/v1/ai/chat/stream?message=我叫什么名字？&sessionId={从日志获取的sessionId}
```

**预期响应：**
AI 应该能记住你的名字是"张三"

### 测试 3: 验证会话上下文保存

**步骤 1：** 流式聊天
```
GET http://localhost/api/v1/ai/chat/stream?message=你好&sessionId=test-session-123
```

**步骤 2：** 等待流式响应完成

**步骤 3：** 查询会话历史
```
GET http://localhost/api/v1/ai/sessions/test-session-123/history
Authorization: Bearer {your_token}
```

**预期响应：**
```json
{
  "code": 200,
  "data": [
    {
      "role": "user",
      "content": "你好",
      "timestamp": "2024-12-10T..."
    },
    {
      "role": "assistant",
      "content": "你好！我是SIAE的智能助手...",
      "timestamp": "2024-12-10T..."
    }
  ]
}
```

## 日志检查

启动服务后，观察日志输出：

### 正常流程日志：
```
[INFO] Received stream chat request: sessionId=null, message=你好
[INFO] Processing stream chat request - sessionId: {uuid}, userId: {userId}
[DEBUG] Stream chunk received - sessionId: {uuid}, content length: 2, content: [你好]
[DEBUG] Stream chunk received - sessionId: {uuid}, content length: 1, content: [！]
[DEBUG] Stream chunk received - sessionId: {uuid}, content length: 2, content: [我是]
...
[INFO] Stream chat completed - sessionId: {uuid}, total length: 73
[DEBUG] Saved assistant message to session: {uuid}
```

### 客户端收到的数据：
```json
{"sessionId":"xxx","role":"assistant","content":"你好","isFinal":false}
{"sessionId":"xxx","role":"assistant","content":"！","isFinal":false}
{"sessionId":"xxx","role":"assistant","content":"我是","isFinal":false}
...
{"sessionId":"xxx","role":"assistant","content":"","isFinal":true}
```

### 如果看到这些日志说明有问题：
```
[WARN] Empty response received for session: {uuid}
[ERROR] Stream chat failed - sessionId: {uuid}, error: ...
```

## 常见问题排查

### 问题 1: 响应中有重复的 "data:" 前缀
**原因：** 可能是 Apifox 的 SSE 解析问题
**解决：** 
- 确认服务已重启，使用了新的代码
- 在 Apifox 中查看"原始响应"而不是"格式化响应"

### 问题 2: 会话上下文没有保存
**检查：**
1. Redis 是否正常运行：`redis-cli ping`
2. 查看日志是否有 "Saved assistant message to session"
3. 检查 Redis 中的数据：`redis-cli KEYS "chat:session:*"`

### 问题 3: 响应内容重复
**原因：** Ollama 的流式响应可能是累积式的（每个 chunk 包含之前的内容）
**检查日志：** 看 "Stream chunk received" 的内容是否累积

## Redis 数据验证

### 查看所有会话：
```bash
redis-cli
> KEYS "chat:session:*"
```

### 查看特定会话内容：
```bash
> GET "chat:session:{sessionId}"
```

### 预期数据格式：
```json
{
  "sessionId": "xxx-xxx-xxx",
  "userId": 1,
  "messages": [
    {
      "role": "user",
      "content": "你好",
      "timestamp": "2024-12-10T..."
    },
    {
      "role": "assistant",
      "content": "你好！我是SIAE的智能助手...",
      "timestamp": "2024-12-10T..."
    }
  ],
  "createdAt": "2024-12-10T...",
  "lastAccessTime": "2024-12-10T..."
}
```

## 前端使用示例

### JavaScript/TypeScript (EventSource)

```javascript
const sessionId = 'your-session-id'; // 可选，首次对话可以不传
const message = '你好';
const token = 'your-jwt-token';

const url = `http://localhost/api/v1/ai/chat/stream?message=${encodeURIComponent(message)}${sessionId ? '&sessionId=' + sessionId : ''}`;

const eventSource = new EventSource(url, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

let fullContent = '';
let receivedSessionId = '';

eventSource.onmessage = (event) => {
  try {
    const data = JSON.parse(event.data);
    
    if (data.isFinal) {
      // 流式传输结束
      console.log('Stream completed');
      console.log('Session ID:', data.sessionId);
      console.log('Full content:', fullContent);
      eventSource.close();
    } else {
      // 接收内容片段
      receivedSessionId = data.sessionId;
      fullContent += data.content;
      
      // 更新UI显示
      updateChatUI(data.content);
    }
  } catch (error) {
    console.error('Parse error:', error);
  }
};

eventSource.onerror = (error) => {
  console.error('SSE error:', error);
  eventSource.close();
};
```

### Vue 3 示例

```vue
<script setup>
import { ref } from 'vue';

const message = ref('');
const chatContent = ref('');
const sessionId = ref('');
const isStreaming = ref(false);

const sendMessage = async () => {
  if (!message.value.trim()) return;
  
  isStreaming.value = true;
  chatContent.value = '';
  
  const url = `http://localhost/api/v1/ai/chat/stream?message=${encodeURIComponent(message.value)}${sessionId.value ? '&sessionId=' + sessionId.value : ''}`;
  
  const eventSource = new EventSource(url);
  
  eventSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      
      if (data.isFinal) {
        sessionId.value = data.sessionId;
        isStreaming.value = false;
        eventSource.close();
      } else {
        sessionId.value = data.sessionId;
        chatContent.value += data.content;
      }
    } catch (error) {
      console.error('Parse error:', error);
    }
  };
  
  eventSource.onerror = (error) => {
    console.error('SSE error:', error);
    isStreaming.value = false;
    eventSource.close();
  };
};
</script>

<template>
  <div>
    <input v-model="message" @keyup.enter="sendMessage" />
    <button @click="sendMessage" :disabled="isStreaming">发送</button>
    <div class="chat-content">{{ chatContent }}</div>
  </div>
</template>
```

## 响应格式说明

### 流式响应字段

| 字段 | 类型 | 说明 |
|------|------|------|
| sessionId | string | 会话ID，用于后续对话保持上下文 |
| role | string | 固定为 "assistant" |
| content | string | 内容片段，每次返回一个 token |
| isFinal | boolean | 是否为最后一条消息，true 表示流结束 |

### 注意事项

1. **sessionId 管理**：
   - 首次对话不传 sessionId，从响应中获取
   - 后续对话使用相同 sessionId 保持上下文

2. **内容拼接**：
   - 客户端需要自己拼接 content 字段
   - 每个 chunk 的 content 是增量内容，不是累积内容

3. **结束标记**：
   - 当 `isFinal: true` 时，表示流式传输结束
   - 最后一条消息的 content 为空字符串

4. **错误处理**：
   - 监听 `onerror` 事件处理连接错误
   - 超时时间为 30 秒（可在配置中调整）

## 下一步

如果测试后仍有问题，请提供：
1. Apifox 的原始响应内容（截图或文本）
2. 服务端日志（特别是 DEBUG 级别的日志）
3. Redis 中的会话数据

这样我可以更准确地定位问题。
