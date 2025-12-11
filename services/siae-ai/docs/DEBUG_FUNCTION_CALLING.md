# Function Calling 调试指南

## 问题：Qwen3:4b 不调用工具

虽然 Qwen3 支持 Function Calling，但通过 Ollama 的 OpenAI 兼容接口可能存在问题。

## 可能的原因

### 1. Ollama 的 OpenAI 兼容模式限制

Ollama 的 `/v1/chat/completions` 接口可能不完全支持 `tools` 参数。

**验证方法**：
```bash
curl http://localhost:11434/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen3:4b",
    "messages": [{"role": "user", "content": "查询张三的信息"}],
    "tools": [{
      "type": "function",
      "function": {
        "name": "queryMembers",
        "description": "查询成员信息",
        "parameters": {
          "type": "object",
          "properties": {
            "name": {"type": "string"}
          }
        }
      }
    }]
  }'
```

如果返回错误或忽略 tools，说明 Ollama 的 OpenAI 兼容模式不支持。

### 2. Spring AI 的配置问题

Spring AI 可能需要特定的配置才能正确发送工具信息。

## 解决方案

### 方案 1: 使用 Ollama 原生 API（推荐）

Ollama 有自己的原生 API，支持工具调用：

```bash
curl http://localhost:11434/api/chat \
  -d '{
    "model": "qwen3:4b",
    "messages": [{"role": "user", "content": "查询张三的信息"}],
    "tools": [{
      "type": "function",
      "function": {
        "name": "queryMembers",
        "description": "查询成员信息",
        "parameters": {
          "type": "object",
          "properties": {
            "name": {"type": "string", "description": "成员姓名"}
          }
        }
      }
    }],
    "stream": false
  }'
```

**需要修改代码**：
- 不使用 `OpenAiChatModel`
- 直接使用 `RestTemplate` 或 `WebClient` 调用 Ollama API
- 手动处理工具调用逻辑

### 方案 2: 启用 Spring AI 的调试日志

在 `application-dev.yaml` 中添加：

```yaml
logging:
  level:
    org.springframework.ai: TRACE
    org.springframework.ai.openai: TRACE
```

重启服务，查看 Spring AI 发送给 Ollama 的实际请求。

### 方案 3: 检查 Spring AI 版本

当前使用的是 `1.0.0-M4`（Milestone 4），可能存在 bug。

尝试升级到最新版本：
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <version>1.0.0-M5</version> <!-- 或更新版本 -->
</dependency>
```

### 方案 4: 使用 Ollama 的 Function Calling 格式

Ollama 可能需要特定的格式。修改系统提示词：

```yaml
system-prompt: |
  你是SIAE的智能助手。
  
  当用户询问成员信息时，使用以下格式调用工具：
  
  <function_call>
  {
    "name": "queryMembers",
    "arguments": {
      "name": "张三"
    }
  }
  </function_call>
  
  可用工具：
  - queryMembers(name, department, position): 查询成员信息
  - getMemberStatistics(): 获取成员统计
  - queryMemberAwards(memberName, studentId): 查询获奖记录
  - getAwardStatistics(typeId, levelId, startDate, endDate): 获取获奖统计
```

然后在代码中解析这个格式并手动调用工具。

### 方案 5: 使用支持更好的模型

某些模型对工具调用的支持更好：

```bash
# Qwen2.5 系列对工具调用支持更好
ollama pull qwen2.5:7b

# Mistral 也有很好的工具调用支持
ollama pull mistral:7b
```

修改配置：
```yaml
siae:
  ai:
    model: qwen2.5:7b
```

## 临时解决方案：手动工具调用

如果以上方案都不行，可以实现一个简单的工具调用解析器：

```java
@Service
public class ManualToolCallService {
    
    public String processWithTools(String userMessage, String aiResponse) {
        // 检查 AI 响应中是否包含工具调用意图
        if (aiResponse.contains("查询") && aiResponse.contains("成员")) {
            // 提取参数
            String name = extractName(userMessage);
            
            // 调用工具
            List<MemberInfo> members = queryMembers(name, null, null);
            
            // 格式化结果
            return formatMemberInfo(members);
        }
        
        return aiResponse;
    }
}
```

## 推荐的调试步骤

1. **启用 TRACE 日志**，查看 Spring AI 发送的请求
2. **使用 curl 测试** Ollama 的工具调用支持
3. **尝试不同的模型**（qwen2.5:7b）
4. **检查 Ollama 版本**：`ollama --version`（建议 >= 0.3.0）
5. **查看 Ollama 日志**：`ollama logs`

## 验证工具是否被调用

在工具函数中添加日志：

```java
@Bean
public Function<QueryMembersRequest, List<MemberInfo>> queryMembers() {
    return request -> {
        System.out.println("========================================");
        System.out.println("🔧 TOOL CALLED: queryMembers");
        System.out.println("Parameters: " + request);
        System.out.println("========================================");
        
        // ... 原有逻辑
    };
}
```

如果看到这个输出，说明工具被调用了。

## 最终建议

如果 Ollama 的 OpenAI 兼容模式确实不支持工具调用，建议：

1. **使用 Ollama 原生 API**（需要修改代码）
2. **使用真正的 OpenAI API**（需要 API key）
3. **使用阿里通义千问**（已经在配置中，只需切换 provider）

切换到通义千问：
```yaml
siae:
  ai:
    provider: qwen
    api-key: ${QWEN_API_KEY}  # 需要申请
    model: qwen-plus
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
```

通义千问的 OpenAI 兼容接口完全支持 Function Calling。
