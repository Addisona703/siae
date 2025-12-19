package com.hngy.siae.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.ai.audit.AiAuditService;
import com.hngy.siae.ai.client.LlmClient;
import com.hngy.siae.ai.client.ProviderManager;
import com.hngy.siae.ai.client.StreamResponseParser;
import com.hngy.siae.ai.client.ToolCallAggregator;
import com.hngy.siae.ai.config.AiProviderProperties;
import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.model.ChatOptions;
import com.hngy.siae.ai.domain.model.ToolCall;
import com.hngy.siae.ai.domain.vo.StreamResponse;
import com.hngy.siae.ai.domain.vo.ToolCallInfo;
import com.hngy.siae.ai.exception.StreamErrorHandler;
import com.hngy.siae.ai.security.PermissionChecker;
import com.hngy.siae.ai.service.ChatInputValidator;
import com.hngy.siae.ai.service.SessionService;
import com.hngy.siae.ai.service.UnifiedChatService;
import com.hngy.siae.ai.tool.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统一聊天服务实现
 * <p>
 * 支持普通聊天、工具调用和思考过程显示的统一接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedChatServiceImpl implements UnifiedChatService {

    private final ProviderManager providerManager;
    private final SessionService sessionService;
    private final AiProviderProperties aiProviderProperties;
    private final ChatInputValidator inputValidator;
    private final AiAuditService auditService;
    private final PermissionChecker permissionChecker;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    private static final int MAX_TOOL_ITERATIONS = 5;

    @Override
    public Flux<String> chat(String sessionId, String message, Long userId,
                             String provider, String model, boolean enableTools) {
        // 输入验证
        inputValidator.validateMessage(message);
        inputValidator.validateSessionId(sessionId);

        // 获取或创建会话
        String effectiveSessionId = getOrCreateSession(sessionId, userId);

        // 获取有效的供应商和模型
        String effectiveProvider = providerManager.getEffectiveProvider(provider);
        String effectiveModel = providerManager.getEffectiveModel(effectiveProvider, model);

        // 记录审计日志
        String username = permissionChecker.getCurrentUsername();
        auditService.logAiInteraction(userId, username, effectiveSessionId, message, LocalDateTime.now());

        log.info("Processing unified chat - sessionId: {}, userId: {}, provider: {}, model: {}, tools: {}",
                effectiveSessionId, userId, effectiveProvider, effectiveModel, enableTools);

        // 添加用户消息到会话
        ChatMessage userMessage = ChatMessage.userMessage(message);
        sessionService.addMessage(effectiveSessionId, userMessage);

        // 构建会话历史
        List<ChatMessage> messages = buildMessagesWithContext(effectiveSessionId);

        // 获取工具定义
        List<Map<String, Object>> tools = enableTools ? toolRegistry.getToolDefinitionsForLlm() : null;

        // 执行聊天（支持工具调用循环）
        return executeChat(effectiveSessionId, effectiveProvider, effectiveModel, messages, tools, 0)
                .doOnError(error -> log.error("Unified chat failed - sessionId: {}, error: {}",
                        effectiveSessionId, error.getMessage(), error))
                .onErrorResume(error -> StreamErrorHandler.handleError(effectiveSessionId, error));
    }

    /**
     * 执行聊天（支持工具调用迭代）
     */
    private Flux<String> executeChat(String sessionId, String provider, String model,
                                      List<ChatMessage> messages, List<Map<String, Object>> tools,
                                      int iteration) {
        if (iteration >= MAX_TOOL_ITERATIONS) {
            log.warn("Max tool iterations reached for session: {}", sessionId);
            return Flux.just(StreamResponse.done(sessionId).toJson());
        }

        LlmClient client = providerManager.getClient(provider);
        ChatOptions options = buildChatOptions();

        // 用于收集响应
        StringBuilder contentBuilder = new StringBuilder();
        StringBuilder reasoningBuilder = new StringBuilder();
        ToolCallAggregator toolCallAggregator = new ToolCallAggregator();

        return client.chatStreamUnified(model, messages, options, tools)
                .flatMap(result -> {
                    List<String> responses = new ArrayList<>();

                    // 处理思考过程
                    if (result.hasReasoning()) {
                        reasoningBuilder.append(result.reasoning());
                        responses.add(StreamResponse.thinking(sessionId, result.reasoning()).toJson());
                    }

                    // 处理普通内容
                    if (result.hasContent()) {
                        contentBuilder.append(result.content());
                        responses.add(StreamResponse.content(sessionId, result.content()).toJson());
                    }

                    // 收集工具调用增量
                    if (result.hasToolCalls()) {
                        toolCallAggregator.addDeltas(result.toolCallDeltas());
                    }

                    return Flux.fromIterable(responses);
                })
                .concatWith(Flux.defer(() -> {
                    // 流结束后处理
                    String content = contentBuilder.toString();
                    String reasoning = reasoningBuilder.toString();
                    List<ToolCall> toolCalls = toolCallAggregator.getToolCalls();

                    log.info("Stream completed - sessionId: {}, content: {}, reasoning: {}, toolCalls: {}",
                            sessionId, content.length(), reasoning.length(), toolCalls.size());

                    // 如果有工具调用，执行工具并继续对话
                    if (!toolCalls.isEmpty()) {
                        return handleToolCalls(sessionId, provider, model, messages, tools,
                                content, toolCalls, iteration);
                    }

                    // 保存助手消息
                    if (StringUtils.hasText(content)) {
                        ChatMessage assistantMessage = ChatMessage.assistantMessage(content, null);
                        sessionService.addMessage(sessionId, assistantMessage);
                    }

                    return Flux.just(StreamResponse.done(sessionId).toJson());
                }));
    }

    /**
     * 处理工具调用
     * 注意：工具调用的中间消息只用于 LLM 上下文，不保存到数据库
     */
    private Flux<String> handleToolCalls(String sessionId, String provider, String model,
                                          List<ChatMessage> messages, List<Map<String, Object>> tools,
                                          String assistantContent, List<ToolCall> toolCalls,
                                          int iteration) {
        List<ChatMessage> newMessages = new ArrayList<>(messages);

        // 创建带工具调用的助手消息（仅用于 LLM 上下文，不保存到数据库）
        List<ToolCallInfo> toolCallInfos = new ArrayList<>();
        for (ToolCall tc : toolCalls) {
            ToolCallInfo info = ToolCallInfo.builder()
                    .id(tc.id())
                    .toolName(tc.name())
                    .argumentsJson(tc.arguments())
                    .build();
            toolCallInfos.add(info);
        }

        ChatMessage assistantMessage = ChatMessage.builder()
                .role(ChatMessage.ROLE_ASSISTANT)
                .content(assistantContent)
                .toolCalls(toolCallInfos)
                .timestamp(LocalDateTime.now())
                .build();
        newMessages.add(assistantMessage);
        // 不再保存工具调用的助手消息到数据库

        // 使用响应式方式执行工具调用
        return Flux.fromIterable(toolCalls)
                .concatMap(tc -> executeToolReactive(sessionId, tc, newMessages))
                .concatWith(Flux.defer(() -> 
                    executeChat(sessionId, provider, model, newMessages, tools, iteration + 1)
                ));
    }

    /**
     * 响应式执行单个工具调用
     * 注意：工具结果消息只用于 LLM 上下文，不保存到数据库
     */
    private Flux<String> executeToolReactive(String sessionId, ToolCall tc, List<ChatMessage> newMessages) {
        Map<String, Object> args = parseArguments(tc.arguments());
        
        // 先发送工具调用事件
        String toolCallEvent = StreamResponse.toolCall(sessionId, tc.id(), tc.name(), args).toJson();
        
        // 在弹性线程池中执行阻塞的工具调用
        Mono<ToolExecutionResult> executionMono = Mono.fromCallable(() -> {
            long startTime = System.currentTimeMillis();
            Object result;
            boolean success = true;
            String error = null;

            try {
                result = toolRegistry.executeTool(tc.name(), args);
                log.info("Tool {} executed successfully: {}", tc.name(), result);
            } catch (Exception e) {
                result = "Error: " + e.getMessage();
                success = false;
                error = e.getMessage();
                log.error("Tool {} execution failed: {}", tc.name(), e.getMessage());
            }

            long executionTime = System.currentTimeMillis() - startTime;
            return new ToolExecutionResult(result, success, error, executionTime);
        }).subscribeOn(Schedulers.boundedElastic());

        return Flux.just(toolCallEvent)
                .concatWith(executionMono.flatMapMany(execResult -> {
                    // 发送工具结果事件
                    String toolResultEvent = StreamResponse.toolResult(sessionId, tc.id(), tc.name(),
                            execResult.result, execResult.success, execResult.error).toJson();

                    // 添加工具结果消息到 LLM 上下文（不保存到数据库）
                    String resultJson;
                    try {
                        resultJson = objectMapper.writeValueAsString(execResult.result);
                    } catch (JsonProcessingException e) {
                        resultJson = String.valueOf(execResult.result);
                    }

                    ChatMessage toolMessage = ChatMessage.toolMessage(tc.id(), resultJson);
                    synchronized (newMessages) {
                        newMessages.add(toolMessage);
                    }
                    // 不再保存工具消息到数据库

                    return Flux.just(toolResultEvent);
                }));
    }

    /**
     * 工具执行结果
     */
    private record ToolExecutionResult(Object result, boolean success, String error, long executionTime) {}

    /**
     * 解析工具参数
     */
    private Map<String, Object> parseArguments(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(argumentsJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse tool arguments: {}", argumentsJson);
            return Map.of();
        }
    }

    /**
     * 获取或创建会话
     */
    private String getOrCreateSession(String sessionId, Long userId) {
        if (StringUtils.hasText(sessionId) && sessionService.sessionExists(sessionId)) {
            return sessionId;
        }
        return sessionService.createSession(userId);
    }

    /**
     * 构建包含上下文的消息列表
     */
    private List<ChatMessage> buildMessagesWithContext(String sessionId) {
        List<ChatMessage> messages = new ArrayList<>();

        String systemPrompt = aiProviderProperties.getChat().getSystemPrompt();
        if (StringUtils.hasText(systemPrompt)) {
            messages.add(ChatMessage.systemMessage(systemPrompt));
        }

        List<ChatMessage> history = sessionService.getMessages(sessionId);
        messages.addAll(history);

        return messages;
    }

    /**
     * 构建聊天选项
     */
    private ChatOptions buildChatOptions() {
        var chatConfig = aiProviderProperties.getChat();
        return ChatOptions.builder()
                .temperature(chatConfig.getTemperature())
                .maxTokens(chatConfig.getMaxTokens())
                .responseTimeout(chatConfig.getResponseTimeout())
                .build();
    }
}
