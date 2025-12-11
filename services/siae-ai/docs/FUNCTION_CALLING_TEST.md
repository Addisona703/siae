# Function Calling (工具调用) 测试指南

## 修改说明

已修复 AI 无法调用工具的问题：

### 问题原因
- 工具函数虽然定义了（`@Bean`），但没有注册到 `Prompt` 中
- AI 模型不知道有哪些工具可以调用

### 解决方案
1. 在 `ToolConfig` 中使用 `FunctionCallbackWrapper` 注册所有工具函数
2. 在 `ChatClientConfig` 中将 `functionCallbacks` Bean 注入到 `OpenAiChatModel`
3. AI 现在可以根据用户问题自动选择合适的工具调用

## 可用工具

### 1. queryMembers - 查询成员信息
**功能**：按姓名、部门、职位查询成员

**参数**：
- `name` (可选): 成员姓名，支持模糊匹配
- `department` (可选): 部门名称，如：技术部、宣传部
- `position` (可选): 职位名称，如：部长、副部长、干事

**示例问题**：
- "查询张三的信息"
- "技术部有哪些成员？"
- "谁是部长？"
- "查询所有干事"

### 2. getMemberStatistics - 获取成员统计
**功能**：获取成员总数及分布统计

**参数**：无

**示例问题**：
- "有多少成员？"
- "各部门人数分布"
- "成员统计信息"

### 3. queryMemberAwards - 查询获奖记录
**功能**：查询指定成员的获奖记录

**参数**：
- `memberName` (可选): 成员姓名
- `studentId` (可选): 学号

**示例问题**：
- "张三获得过哪些奖项？"
- "查询学号 2021001 的获奖记录"
- "李四的获奖情况"

### 4. getAwardStatistics - 获取获奖统计
**功能**：按类型、等级、时间统计获奖数据

**参数**：
- `typeId` (可选): 奖项类型ID
- `levelId` (可选): 奖项等级ID
- `startDate` (可选): 开始日期 (yyyy-MM-dd)
- `endDate` (可选): 结束日期 (yyyy-MM-dd)

**示例问题**：
- "今年有多少人获奖？"
- "国家级奖项有多少？"
- "2024年的获奖统计"

## 测试步骤

### 测试 1: 查询成员信息

**请求**：
```
GET http://localhost/api/v1/ai/chat/stream?message=查询用户id为1的成员信息
Authorization: Bearer {your_token}
```

**预期行为**：
1. AI 识别到需要查询成员信息
2. 自动调用 `queryMembers` 工具
3. 返回成员的详细信息（姓名、学号、部门、职位等）

**日志检查**：
```
[INFO] Tool invoked: queryMembers - name: null, department: null, position: null, user: {username}
[INFO] queryMembers returned 1 members for user: {username}
```

### 测试 2: 查询获奖记录

**请求**：
```
GET http://localhost/api/v1/ai/chat/stream?message=张三获得过哪些奖项？
Authorization: Bearer {your_token}
```

**预期行为**：
1. AI 识别到需要查询获奖记录
2. 自动调用 `queryMemberAwards` 工具，参数 `memberName: "张三"`
3. 返回该成员的所有获奖信息

**日志检查**：
```
[INFO] Tool invoked: queryMemberAwards - memberName: 张三, studentId: null, user: {username}
[INFO] queryMemberAwards returned X awards for user: {username}
```

### 测试 3: 获取统计信息

**请求**：
```
GET http://localhost/api/v1/ai/chat/stream?message=有多少成员？
Authorization: Bearer {your_token}
```

**预期行为**：
1. AI 识别到需要统计信息
2. 自动调用 `getMemberStatistics` 工具
3. 返回成员总数及分布统计

**日志检查**：
```
[INFO] Tool invoked: getMemberStatistics, user: {username}
[INFO] getMemberStatistics returned statistics with totalMembers: X for user: {username}
```

## 工具调用流程

```
用户提问
    ↓
AI 分析问题
    ↓
识别需要调用的工具
    ↓
提取工具参数
    ↓
调用工具函数
    ↓
获取数据结果
    ↓
AI 整理并返回答案
```

## 响应格式

### 流式响应（带工具调用）

```json
{"sessionId":"xxx","role":"assistant","content":"让我","isFinal":false}
{"sessionId":"xxx","role":"assistant","content":"帮你","isFinal":false}
{"sessionId":"xxx","role":"assistant","content":"查询","isFinal":false}
...
{"sessionId":"xxx","role":"assistant","content":"根据查询结果，用户ID为1的成员信息如下：\n姓名：张三\n学号：2021001\n部门：技术部\n职位：部长","isFinal":false}
...
{"sessionId":"xxx","role":"assistant","content":"","isFinal":true}
```

## 权限控制

工具调用会进行权限检查：

1. **queryMembers** - 需要成员查询权限
2. **getMemberStatistics** - 需要管理员权限
3. **queryMemberAwards** - 需要成员查询权限
4. **getAwardStatistics** - 需要管理员权限

如果权限不足，会抛出异常：
```
权限不足：您没有权限执行此操作
```

## 调试技巧

### 1. 查看工具注册日志

启动服务时，查看日志：
```
[INFO] Registered function callback: queryMembers
[INFO] Registered function callback: getMemberStatistics
[INFO] Registered function callback: queryMemberAwards
[INFO] Registered function callback: getAwardStatistics
[INFO] Creating OpenAiChatModel with model: qwen3:4b, temperature: 0.7, maxTokens: 2000, functions: 4
```

### 2. 查看工具调用日志

当 AI 调用工具时：
```
[INFO] Tool invoked: queryMembers - name: 张三, department: null, position: null, user: admin
[INFO] queryMembers returned 1 members for user: admin
```

### 3. 如果工具没有被调用

可能原因：
1. **问题不够明确**：AI 无法判断需要调用哪个工具
   - 解决：使用更明确的问题，如"查询张三的信息"而不是"张三"
   
2. **Ollama 模型不支持 Function Calling**：
   - 检查：查看日志是否有 "Registering X functions" 
   - 解决：确认使用的模型支持工具调用（qwen3 支持）

3. **工具没有注册**：
   - 检查：查看启动日志是否有 "Registered function callback"
   - 解决：确认工具类有 `@Configuration` 注解，函数有 `@Bean` 注解

### 4. 测试工具是否正常工作

可以直接调用 Feign 客户端测试：
```java
// 在测试类中
@Autowired
private AiUserFeignClient aiUserFeignClient;

@Test
void testQueryMembers() {
    List<MemberInfo> members = aiUserFeignClient.searchMembers("张三", null, null);
    assertNotNull(members);
}
```

## 常见问题

### Q1: AI 说"无法查询"或"没有权限"
**A**: 检查：
1. 用户是否有相应权限
2. Feign 客户端是否正常工作
3. siae-user 服务是否正常运行

### Q2: 工具调用了但返回空结果
**A**: 检查：
1. 数据库中是否有对应数据
2. 查询参数是否正确
3. Feign 客户端的 URL 配置是否正确

### Q3: AI 没有调用工具，直接回答
**A**: 可能原因：
1. 模型认为不需要调用工具就能回答
2. 问题不够明确，模型无法判断
3. 模型不支持 Function Calling

解决方法：
- 使用更明确的问题
- 在系统提示词中强调使用工具
- 确认模型支持 Function Calling

## 系统提示词优化

如果 AI 不经常使用工具，可以在配置中优化系统提示词：

```yaml
siae:
  ai:
    system-prompt: |
      你是SIAE（学生创新与创业协会）的智能助手。
      
      当用户询问成员信息、获奖记录等数据时，你必须使用提供的工具函数查询实际数据，
      不要编造或猜测信息。
      
      可用工具：
      - queryMembers: 查询成员信息
      - getMemberStatistics: 获取成员统计
      - queryMemberAwards: 查询获奖记录
      - getAwardStatistics: 获取获奖统计
      
      请用简洁、专业的中文回答问题。
```

## 下一步

测试完成后，如果还有问题：
1. 提供完整的请求和响应日志
2. 提供 AI 的回答内容
3. 说明预期行为和实际行为的差异
