package com.hngy.siae.ai.controller;

import com.hngy.siae.ai.domain.dto.ChatRequest;
import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.vo.SessionListVO;
import com.hngy.siae.ai.service.ChatService;
import com.hngy.siae.ai.domain.vo.ChatResponse;
import com.hngy.siae.ai.service.SessionPersistenceService;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI聊天控制器
 * <p>
 * 提供AI对话的REST API接口，包括同步聊天、流式聊天、会话历史管理等
 * <p>
 * Requirements: 9.1, 9.2, 9.3
 *
 * @author SIAE Team
 */
@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "AI聊天", description = "AI智能对话接口")
public class ChatController {

    private final ChatService chatService;
    private final SecurityUtil securityUtil;
    private final SessionPersistenceService sessionPersistenceService;
    private final com.hngy.siae.ai.config.AiProperties aiProperties;

    /**
     * 同步聊天
     * <p>
     * 发送消息并等待AI完整响应
     * <p>
     * Requirements: 9.1
     *
     * @param request 聊天请求，包含消息内容和可选的会话ID
     * @return 聊天响应，包含AI回复内容和工具调用信息
     */
    @PostMapping("/chat")
    @Operation(summary = "同步聊天", description = "发送消息并等待AI完整响应，支持附加文件（如图片）")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat request: sessionId={}, message={}, fileIds={}", 
                request.getSessionId(), request.getMessage(), request.getFileIds());
        
        Long userId = securityUtil.getCurrentUserId();
        log.debug("Current user ID: {}", userId);
        
        ChatResponse response = chatService.chat(
                request.getSessionId(), 
                request.getMessage(), 
                userId,
                request.getFileIds()
        );
        
        log.info("Chat response generated: sessionId={}", response.getSessionId());
        return Result.success(response);
    }

    /**
     * 流式聊天（SSE）
     * <p>
     * 发送消息并以Server-Sent Events方式流式返回AI响应，支持实时显示
     * <p>
     * Requirements: 2.4, 9.2
     *
     * @param message   用户消息内容
     * @param sessionId 会话ID，可选，如果不提供则创建新会话
     * @param fileIds   附加的文件ID列表，可选，多个用逗号分隔
     * @return SSE流，每个事件包含响应的一部分
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式聊天", description = "使用SSE流式返回AI响应，支持附加文件（如图片）")
    public Flux<String> chatStream(
            @Parameter(description = "用户消息内容", required = true)
            @RequestParam String message,
            @Parameter(description = "会话ID，可选")
            @RequestParam(required = false) String sessionId,
            @Parameter(description = "模型名称，可选，不传则使用默认模型")
            @RequestParam(required = false) String model,
            @Parameter(description = "附加的文件ID列表，多个用逗号分隔")
            @RequestParam(required = false) String fileIds) {
        
        log.info("Received stream chat request: sessionId={}, message={}, model={}, fileIds={}", 
                sessionId, message, model, fileIds);
        
        Long userId = securityUtil.getCurrentUserId();
        log.debug("Current user ID: {}", userId);
        
        // 解析 fileIds
        java.util.List<String> fileIdList = null;
        if (fileIds != null && !fileIds.trim().isEmpty()) {
            fileIdList = java.util.Arrays.asList(fileIds.split(","));
        }
        
        return chatService.chatStream(sessionId, message, userId, model, fileIdList)
                .filter(content -> content != null && !content.isEmpty())
                .doOnComplete(() -> log.info("Stream chat completed: sessionId={}", sessionId))
                .doOnError(error -> log.error("Stream chat error: sessionId={}", sessionId, error));
    }

    /**
     * 支持 Thinking 的流式聊天（SSE）
     * <p>
     * 发送消息并以Server-Sent Events方式流式返回AI响应，包含思考过程
     *
     * @param message   用户消息内容
     * @param sessionId 会话ID，可选
     * @param fileIds   附加的文件ID列表，多个用逗号分隔
     * @return SSE流，每个事件包含 thinking 或 content 字段
     */
    @GetMapping(value = "/chat/stream/thinking", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式聊天（含思考过程）", description = "使用SSE流式返回AI响应，包含思考过程，支持附加文件（如图片）")
    public Flux<String> chatStreamWithThinking(
            @Parameter(description = "用户消息内容", required = true)
            @RequestParam String message,
            @Parameter(description = "会话ID，可选")
            @RequestParam(required = false) String sessionId,
            @Parameter(description = "模型名称，可选，不传则使用默认模型")
            @RequestParam(required = false) String model,
            @Parameter(description = "附加的文件ID列表，多个用逗号分隔")
            @RequestParam(required = false) String fileIds) {
        
        log.info("Received stream chat with thinking request: sessionId={}, message={}, model={}, fileIds={}", 
                sessionId, message, model, fileIds);
        
        Long userId = securityUtil.getCurrentUserId();
        log.debug("Current user ID: {}", userId);
        
        // 解析 fileIds
        java.util.List<String> fileIdList = null;
        if (fileIds != null && !fileIds.trim().isEmpty()) {
            fileIdList = java.util.Arrays.asList(fileIds.split(","));
        }
        
        return chatService.chatStreamWithThinking(sessionId, message, userId, model, fileIdList)
                .filter(content -> content != null && !content.isEmpty())
                .doOnComplete(() -> log.info("Stream chat with thinking completed: sessionId={}", sessionId))
                .doOnError(error -> log.error("Stream chat with thinking error: sessionId={}", sessionId, error));
    }

    /**
     * 获取会话历史
     * <p>
     * 返回指定会话的所有消息记录
     * <p>
     * Requirements: 9.3
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @GetMapping("/sessions/{sessionId}/history")
    @Operation(summary = "获取会话历史", description = "返回指定会话的所有消息记录")
    public Result<List<ChatMessage>> getHistory(
            @Parameter(description = "会话ID", required = true)
            @PathVariable String sessionId) {
        
        log.info("Fetching conversation history: sessionId={}", sessionId);
        
        List<ChatMessage> history = chatService.getConversationHistory(sessionId);
        
        log.info("Retrieved {} messages for session: {}", history.size(), sessionId);
        return Result.success(history);
    }

    /**
     * 清除会话
     * <p>
     * 删除指定会话及其所有消息记录
     * <p>
     * Requirements: 9.3
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "清除会话", description = "删除指定会话及其所有消息记录")
    public Result<Void> clearSession(
            @Parameter(description = "会话ID", required = true)
            @PathVariable String sessionId) {
        
        log.info("Clearing conversation session: sessionId={}", sessionId);
        
        chatService.clearConversation(sessionId);
        
        log.info("Session cleared successfully: sessionId={}", sessionId);
        return Result.success();
    }

    /**
     * 获取当前用户的会话列表
     * <p>
     * 返回轻量级会话列表，不包含消息内容
     *
     * @param limit 返回数量限制，默认20
     * @return 会话列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取会话列表", description = "获取当前用户的历史会话列表（不含消息内容）")
    public Result<List<SessionListVO>> getSessionList(
            @Parameter(description = "返回数量限制")
            @RequestParam(defaultValue = "20") int limit) {
        
        Long userId = securityUtil.getCurrentUserId();
        log.info("Fetching session list for user: {}, limit: {}", userId, limit);
        
        List<SessionListVO> sessions = sessionPersistenceService.getUserSessionList(userId, limit);
        
        log.info("Retrieved {} sessions for user: {}", sessions.size(), userId);
        return Result.success(sessions);
    }

    /**
     * 获取可用模型列表
     *
     * @return 可用模型列表
     */
    @GetMapping("/models")
    @Operation(summary = "获取可用模型列表", description = "返回配置的可用AI模型列表")
    public Result<List<String>> getAvailableModels() {
        return Result.success(aiProperties.getAvailableModels());
    }
}
