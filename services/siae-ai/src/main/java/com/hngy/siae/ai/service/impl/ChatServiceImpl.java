package com.hngy.siae.ai.service.impl;

import com.hngy.siae.ai.audit.AiAuditService;
import com.hngy.siae.ai.client.ProviderManager;
import com.hngy.siae.ai.config.AiProviderProperties;
import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.vo.StreamResponse;
import com.hngy.siae.ai.exception.StreamErrorHandler;
import com.hngy.siae.ai.security.PermissionChecker;
import com.hngy.siae.ai.service.ChatInputValidator;
import com.hngy.siae.ai.service.ChatService;
import com.hngy.siae.ai.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI聊天服务实现
 * <p>
 * 使用 ProviderManager 调用 LLM，使用 SessionService 管理会话。
 * 实现流式响应和会话上下文保持。
 * <p>
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 9.4
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ProviderManager providerManager;
    private final SessionService sessionService;
    private final AiProviderProperties aiProviderProperties;
    private final ChatInputValidator inputValidator;
    private final AiAuditService auditService;
    private final PermissionChecker permissionChecker;


    @Override
    public Flux<String> chatStream(String sessionId, String message, Long userId,
                                   String provider, String model) {
        // 输入验证
        inputValidator.validateMessage(message);
        inputValidator.validateSessionId(sessionId);

        // 获取或创建会话
        String effectiveSessionId = getOrCreateSession(sessionId, userId);

        // 获取有效的供应商和模型
        String effectiveProvider = providerManager.getEffectiveProvider(provider);
        String effectiveModel = providerManager.getEffectiveModel(effectiveProvider, model);

        // 记录AI交互审计日志
        String username = permissionChecker.getCurrentUsername();
        auditService.logAiInteraction(userId, username, effectiveSessionId, message, LocalDateTime.now());

        log.info("Processing stream chat - sessionId: {}, userId: {}, provider: {}, model: {}",
                effectiveSessionId, userId, effectiveProvider, effectiveModel);

        // 添加用户消息到会话
        ChatMessage userMessage = ChatMessage.userMessage(message);
        sessionService.addMessage(effectiveSessionId, userMessage);

        // 构建会话历史（包含系统提示）
        List<ChatMessage> messages = buildMessagesWithContext(effectiveSessionId);

        // 用于收集完整响应
        StringBuilder fullResponse = new StringBuilder();

        // 调用 ProviderManager 进行流式聊天
        return providerManager.chatStream(effectiveProvider, effectiveModel, messages)
                .map(content -> {
                    if (StringUtils.hasText(content)) {
                        fullResponse.append(content);
                    }
                    return content != null ? content : "";
                })
                .filter(StringUtils::hasText)
                .map(content -> StreamResponse.content(effectiveSessionId, content).toJson())
                .concatWith(Flux.defer(() -> {
                    // 流结束时保存完整响应
                    String finalResponse = fullResponse.toString();
                    log.info("Stream chat completed - sessionId: {}, total length: {}",
                            effectiveSessionId, finalResponse.length());

                    if (StringUtils.hasText(finalResponse)) {
                        ChatMessage assistantMessage = ChatMessage.assistantMessage(finalResponse, null);
                        sessionService.addMessage(effectiveSessionId, assistantMessage);
                        log.debug("Saved assistant message to session: {}", effectiveSessionId);
                    } else {
                        log.warn("Empty response received for session: {}", effectiveSessionId);
                    }

                    return Flux.just(StreamResponse.done(effectiveSessionId).toJson());
                }))
                .doOnError(error -> {
                    log.error("Stream chat failed - sessionId: {}, error: {}",
                            effectiveSessionId, error.getMessage(), error);
                })
                .onErrorResume(error -> StreamErrorHandler.handleError(effectiveSessionId, error));
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
     * 包含系统提示和会话历史
     */
    private List<ChatMessage> buildMessagesWithContext(String sessionId) {
        List<ChatMessage> messages = new ArrayList<>();

        // 添加系统提示
        String systemPrompt = aiProviderProperties.getChat().getSystemPrompt();
        if (StringUtils.hasText(systemPrompt)) {
            messages.add(ChatMessage.systemMessage(systemPrompt));
        }

        // 添加会话历史
        List<ChatMessage> history = sessionService.getMessages(sessionId);
        messages.addAll(history);

        return messages;
    }
}
