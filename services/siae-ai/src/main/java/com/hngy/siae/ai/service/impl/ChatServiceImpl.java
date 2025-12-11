package com.hngy.siae.ai.service.impl;

import com.hngy.siae.ai.audit.AiAuditService;
import com.hngy.siae.ai.config.AiProperties;
import com.hngy.siae.ai.exception.AiException;
import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.security.PermissionChecker;
import com.hngy.siae.ai.service.ChatInputValidator;
import com.hngy.siae.ai.service.ChatService;
import com.hngy.siae.ai.service.ConversationManager;
import com.hngy.siae.ai.domain.vo.ChatResponse;
import com.hngy.siae.api.media.client.MediaFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI聊天服务实现
 * <p>
 * 使用 Spring AI Ollama API，支持工具调用和会话管理
 * <p>
 * Requirements: 2.1, 2.2, 2.4, 5.1, 6.1
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ConversationManager conversationManager;
    private final AiProperties aiProperties;
    private final RetryTemplate aiRetryTemplate;
    private final ChatInputValidator inputValidator;
    private final AiAuditService auditService;
    private final PermissionChecker permissionChecker;
    private final OllamaThinkingService ollamaThinkingService;
    private final MediaFeignClient mediaFeignClient;

    @Override
    public ChatResponse chat(String sessionId, String message, Long userId, List<String> fileIds) {
        // 输入验证
        inputValidator.validateMessage(message);
        inputValidator.validateSessionId(sessionId);
        
        // 获取或创建会话
        String effectiveSessionId = getOrCreateSession(sessionId, userId);
        
        // 如果有附加文件，在消息中添加文件信息提示
        String enhancedMessage = enhanceMessageWithFiles(message, fileIds);
        
        // 记录AI交互审计日志
        String username = permissionChecker.getCurrentUsername();
        auditService.logAiInteraction(userId, username, effectiveSessionId, enhancedMessage, LocalDateTime.now());
        
        log.info("Processing chat request - sessionId: {}, userId: {}, fileIds: {}", 
                effectiveSessionId, userId, fileIds);
        
        try {
            // 添加用户消息到会话
            ChatMessage userMessage = ChatMessage.userMessage(enhancedMessage);
            conversationManager.addMessage(effectiveSessionId, userMessage);
            
            // 构建会话历史
            List<ChatMessage> history = conversationManager.getMessages(effectiveSessionId);
            
            // 使用 ChatClient 调用（带重试）
            String responseContent = aiRetryTemplate.execute(context -> {
                log.debug("Calling AI model with tools, attempt: {}", context.getRetryCount() + 1);
                
                // 构建消息列表
                List<Message> messages = buildChatMessages(history);
                
                // 调用 ChatClient（工具已在配置中注册为默认工具）
                return chatClient.prompt()
                        .messages(messages)
                        .user(enhancedMessage)
                        .call()
                        .content();
            });
            
            // 添加AI响应到会话
            ChatMessage assistantMessage = ChatMessage.assistantMessage(responseContent, null);
            conversationManager.addMessage(effectiveSessionId, assistantMessage);
            
            log.info("Chat completed - sessionId: {}", effectiveSessionId);
            
            return ChatResponse.builder()
                    .sessionId(effectiveSessionId)
                    .content(responseContent)
                    .toolCalls(Collections.emptyList())
                    .timestamp(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Chat failed - sessionId: {}, error: {}", effectiveSessionId, e.getMessage(), e);
            throw AiException.llmProviderError(e);
        }
    }
    
    /**
     * 增强消息：如果有附加文件，添加文件信息提示
     */
    private String enhanceMessageWithFiles(String message, List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return message;
        }
        
        StringBuilder enhanced = new StringBuilder(message);
        enhanced.append("\n\n[用户附加了 ").append(fileIds.size()).append(" 个文件]");
        for (int i = 0; i < fileIds.size(); i++) {
            enhanced.append("\n文件").append(i + 1).append(" ID: ").append(fileIds.get(i));
        }
        
        return enhanced.toString();
    }
    
    /**
     * 获取图片文件的 Base64 编码列表
     * 用于多模态模型（如 Gemma3）的图片分析
     * 会自动压缩过大的图片以避免请求体过大
     */
    private List<String> getImageBase64List(List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return null;
        }
        
        List<String> base64List = new ArrayList<>();
        for (String fileId : fileIds) {
            try {
                byte[] imageBytes = mediaFeignClient.getFileBytes(fileId);
                if (imageBytes != null && imageBytes.length > 0) {
                    log.info("Loaded image: fileId={}, original size={} bytes ({} KB)", 
                            fileId, imageBytes.length, imageBytes.length / 1024);
                    
                    // 压缩图片以避免请求体过大
                    // byte[] compressedBytes = com.hngy.siae.ai.util.ImageCompressor.compress(imageBytes);
                    
                    // 暂时使用原始图片，不压缩
                    byte[] compressedBytes = imageBytes;
                    
                    String base64 = Base64.getEncoder().encodeToString(compressedBytes);
                    base64List.add(base64);
                    log.info("Image ready: fileId={}, final size={} bytes ({} KB), base64 length={}", 
                            fileId, compressedBytes.length, compressedBytes.length / 1024, base64.length());
                }
            } catch (Exception e) {
                log.warn("Failed to load image: fileId={}, error={}", fileId, e.getMessage());
            }
        }
        
        return base64List.isEmpty() ? null : base64List;
    }

    @Override
    public Flux<String> chatStream(String sessionId, String message, Long userId, String model, List<String> fileIds) {
        // 输入验证
        inputValidator.validateMessage(message);
        inputValidator.validateSessionId(sessionId);
        
        // 获取或创建会话
        String effectiveSessionId = getOrCreateSession(sessionId, userId);
        
        // 如果有附加文件，在消息中添加文件信息提示
        String enhancedMessage = enhanceMessageWithFiles(message, fileIds);
        
        // 记录AI交互审计日志
        String username = permissionChecker.getCurrentUsername();
        auditService.logAiInteraction(userId, username, effectiveSessionId, enhancedMessage, LocalDateTime.now());
        
        // 确定使用的模型
        String effectiveModel = StringUtils.hasText(model) ? model : aiProperties.getModel();
        log.info("Processing stream chat request - sessionId: {}, userId: {}, model: {}, fileIds: {}", 
                effectiveSessionId, userId, effectiveModel, fileIds);
        
        // 添加用户消息到会话
        ChatMessage userMessage = ChatMessage.userMessage(enhancedMessage);
        conversationManager.addMessage(effectiveSessionId, userMessage);
        
        // 构建会话历史
        List<ChatMessage> history = conversationManager.getMessages(effectiveSessionId);
        
        // 用于收集完整响应
        StringBuilder fullResponse = new StringBuilder();
        
        // 构建消息列表
        List<Message> messages = buildChatMessages(history);
        
        // 使用 ChatClient 流式调用（工具已在配置中注册为默认工具）
        return chatClient.prompt()
                .messages(messages)
                .user(enhancedMessage)
                .stream()
                .content()
                .map(content -> {
                    log.debug("Stream chunk received - sessionId: {}, content length: {}", 
                            effectiveSessionId, content != null ? content.length() : 0);
                    if (StringUtils.hasText(content)) {
                        fullResponse.append(content);
                    }
                    return content != null ? content : "";
                })
                .filter(content -> StringUtils.hasText(content))
                .map(content -> {
                    log.debug("Stream chunk received - sessionId: {}, content length: {}", 
                            effectiveSessionId, content != null ? content.length() : 0);
                    if (StringUtils.hasText(content)) {
                        fullResponse.append(content);
                    }
                    return content != null ? content : "";
                })
                .filter(content -> StringUtils.hasText(content))
                .map(content -> buildStreamResponse(effectiveSessionId, content, false))
                .concatWith(Flux.defer(() -> {
                    // 流结束时保存完整响应
                    String finalResponse = fullResponse.toString();
                    log.info("Stream chat completed - sessionId: {}, total length: {}", 
                            effectiveSessionId, finalResponse.length());
                    
                    if (StringUtils.hasText(finalResponse)) {
                        ChatMessage assistantMessage = ChatMessage.assistantMessage(finalResponse, null);
                        conversationManager.addMessage(effectiveSessionId, assistantMessage);
                        log.debug("Saved assistant message to session: {}", effectiveSessionId);
                    } else {
                        log.warn("Empty response received for session: {}", effectiveSessionId);
                    }
                    
                    return Flux.just(buildStreamResponse(effectiveSessionId, "", true));
                }))
                .doOnError(error -> {
                    log.error("Stream chat failed - sessionId: {}, error: {}", 
                            effectiveSessionId, error.getMessage(), error);
                })
                .onErrorMap(error -> AiException.llmProviderError(error));
    }

    /**
     * 构建流式响应 JSON
     */
    private String buildStreamResponse(String sessionId, String content, boolean isFinal) {
        String escapedContent = content
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        
        return String.format("{\"sessionId\":\"%s\",\"role\":\"assistant\",\"content\":\"%s\",\"isFinal\":%b}", 
                sessionId, escapedContent, isFinal);
    }

    @Override
    public List<ChatMessage> getConversationHistory(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return Collections.emptyList();
        }
        return conversationManager.getMessages(sessionId);
    }

    @Override
    public void clearConversation(String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            conversationManager.clearSession(sessionId);
            log.info("Conversation cleared - sessionId: {}", sessionId);
        }
    }

    @Override
    public String createSession(Long userId) {
        return conversationManager.createSession(userId);
    }

    @Override
    public boolean sessionExists(String sessionId) {
        return StringUtils.hasText(sessionId) && conversationManager.sessionExists(sessionId);
    }

    /**
     * 支持 Thinking 的流式聊天
     */
    @Override
    public Flux<String> chatStreamWithThinking(String sessionId, String message, Long userId, String model, List<String> fileIds) {
        // 输入验证
        inputValidator.validateMessage(message);
        inputValidator.validateSessionId(sessionId);
        
        // 获取或创建会话
        String effectiveSessionId = getOrCreateSession(sessionId, userId);
        
        // 如果有附加文件，在消息中添加文件信息提示
        String enhancedMessage = enhanceMessageWithFiles(message, fileIds);
        
        // 记录AI交互审计日志
        String username = permissionChecker.getCurrentUsername();
        auditService.logAiInteraction(userId, username, effectiveSessionId, enhancedMessage, LocalDateTime.now());
        
        // 确定使用的模型
        String effectiveModel = StringUtils.hasText(model) ? model : aiProperties.getModel();
        
        // 检查：如果有图片但模型不支持视觉，返回错误提示
        boolean hasImages = fileIds != null && !fileIds.isEmpty();
        if (hasImages && !aiProperties.isVisionModel(effectiveModel)) {
            log.warn("Model {} does not support vision, but images were provided", effectiveModel);
            return Flux.just(buildUnifiedStreamResponse(effectiveSessionId, "error", 
                    "当前模型 " + effectiveModel + " 不支持图片分析，请选择支持视觉的模型（如 gemma3、llava 等）", true));
        }
        
        log.info("Processing stream chat with thinking - sessionId: {}, userId: {}, model: {}, fileIds: {}", 
                effectiveSessionId, userId, effectiveModel, fileIds);
        
        // 添加用户消息到会话
        ChatMessage userMessage = ChatMessage.userMessage(enhancedMessage);
        conversationManager.addMessage(effectiveSessionId, userMessage);
        
        // 构建会话历史
        List<ChatMessage> history = conversationManager.getMessages(effectiveSessionId);
        List<Map<String, Object>> historyMaps = new ArrayList<>();
        for (ChatMessage msg : history) {
            if (!msg.getContent().equals(message)) { // 排除当前消息
                Map<String, Object> m = new HashMap<>();
                m.put("role", msg.getRole());
                m.put("content", msg.getContent());
                historyMaps.add(m);
            }
        }
        
        // 获取图片的 Base64 编码列表
        List<String> imageBase64List = getImageBase64List(fileIds);
        
        // 构建消息（包含图片）
        List<Map<String, Object>> messages = ollamaThinkingService.buildMessages(
                aiProperties.getSystemPrompt(), historyMaps, message, imageBase64List);
        
        // 用于收集完整响应
        StringBuilder fullThinking = new StringBuilder();
        StringBuilder fullContent = new StringBuilder();
        
        return ollamaThinkingService.chatStreamWithThinking(messages, effectiveModel)
                .map(chunk -> {
                    if (chunk.hasThinking()) {
                        fullThinking.append(chunk.getThinking());
                        return buildUnifiedStreamResponse(effectiveSessionId, "thinking", chunk.getThinking(), false);
                    } else if (chunk.hasContent()) {
                        fullContent.append(chunk.getContent());
                        return buildUnifiedStreamResponse(effectiveSessionId, "content", chunk.getContent(), false);
                    } else if (chunk.hasToolCall()) {
                        return buildUnifiedStreamResponse(effectiveSessionId, "tool_call", chunk.getToolCall(), false);
                    } else if (chunk.hasToolResult()) {
                        return buildUnifiedStreamResponse(effectiveSessionId, "tool_result", chunk.getToolResult(), false);
                    }
                    return "";
                })
                .filter(s -> !s.isEmpty())
                .concatWith(Flux.defer(() -> {
                    // 流结束时保存完整响应
                    String finalContent = fullContent.toString();
                    String finalThinking = fullThinking.toString();
                    log.info("Stream chat with thinking completed - sessionId: {}, thinking length: {}, content length: {}", 
                            effectiveSessionId, finalThinking.length(), finalContent.length());
                    
                    if (StringUtils.hasText(finalContent)) {
                        ChatMessage assistantMessage = ChatMessage.assistantMessage(finalContent, null);
                        conversationManager.addMessage(effectiveSessionId, assistantMessage);
                    }
                    
                    return Flux.just(buildUnifiedStreamResponse(effectiveSessionId, "done", "", true));
                }))
                .doOnError(error -> {
                    log.error("Stream chat with thinking failed - sessionId: {}, error: {}", 
                            effectiveSessionId, error.getMessage(), error);
                })
                .onErrorMap(error -> AiException.llmProviderError(error));
    }

    /**
     * 构建统一格式的流式响应 JSON
     * 格式: {"sessionId":"xxx", "type":"thinking|content|tool_call|tool_result|done", "text":"...", "isFinal":false}
     */
    private String buildUnifiedStreamResponse(String sessionId, String type, String text, boolean isFinal) {
        String escapedText = escapeJson(text);
        return String.format("{\"sessionId\":\"%s\",\"type\":\"%s\",\"text\":\"%s\",\"isFinal\":%b}",
                sessionId, type, escapedText, isFinal);
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 获取或创建会话
     */
    private String getOrCreateSession(String sessionId, Long userId) {
        if (StringUtils.hasText(sessionId) && conversationManager.sessionExists(sessionId)) {
            return sessionId;
        }
        return conversationManager.createSession(userId);
    }
    
    /**
     * 构建 ChatClient 消息列表
     */
    private List<Message> buildChatMessages(List<ChatMessage> history) {
        List<Message> messages = new ArrayList<>();
        
        // 添加系统提示
        messages.add(new SystemMessage(aiProperties.getSystemPrompt()));
        
        // 添加会话历史（排除当前用户消息，因为会在 ChatClient 调用时单独添加）
        for (ChatMessage chatMessage : history) {
            if (ChatMessage.ROLE_USER.equals(chatMessage.getRole())) {
                messages.add(new UserMessage(chatMessage.getContent()));
            } else if (ChatMessage.ROLE_ASSISTANT.equals(chatMessage.getRole())) {
                messages.add(new AssistantMessage(chatMessage.getContent()));
            }
        }
        
        return messages;
    }
}
