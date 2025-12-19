package com.hngy.siae.ai.controller;

import com.hngy.siae.ai.client.ProviderManager;
import com.hngy.siae.ai.domain.dto.UpdateSessionTitleRequest;
import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.vo.ProviderInfo;
import com.hngy.siae.ai.domain.vo.SessionListVO;
import com.hngy.siae.ai.service.ChatService;
import com.hngy.siae.ai.service.SessionService;
import com.hngy.siae.ai.service.UnifiedChatService;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
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
import java.util.Map;

/**
 * AI聊天控制器
 * <p>
 * 提供AI对话的REST API接口，包括流式聊天、供应商查询、会话管理等。
 * 支持统一接口处理普通聊天、工具调用和思考过程显示。
 * <p>
 * Requirements: 7.1, 7.2, 7.3, 7.4
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
    private final UnifiedChatService unifiedChatService;
    private final SessionService sessionService;
    private final ProviderManager providerManager;
    private final SecurityUtil securityUtil;

    /**
     * 统一流式聊天（SSE）- 支持工具调用和思考过程
     * <p>
     * 响应类型说明：
     * - type=content: 普通内容
     * - type=thinking: 思考过程（如 DeepSeek 的 reasoning）
     * - type=tool_call: 工具调用请求
     * - type=tool_result: 工具执行结果
     * - type=done: 完成
     * - type=error: 错误
     */
    @GetMapping(value = "/chat/unified", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "统一流式聊天", description = "支持工具调用和思考过程的统一聊天接口")
    @SiaeAuthorize("isAuthenticated()")
    public Flux<String> unifiedChat(
            @Parameter(description = "用户消息内容", required = true)
            @RequestParam String message,
            @Parameter(description = "会话ID，可选，不传则创建新会话")
            @RequestParam(required = false) String sessionId,
            @Parameter(description = "供应商名称，可选，不传则使用默认供应商")
            @RequestParam(required = false) String provider,
            @Parameter(description = "模型名称，可选，不传则使用供应商默认模型")
            @RequestParam(required = false) String model,
            @Parameter(description = "是否启用工具调用，默认true")
            @RequestParam(defaultValue = "true") boolean enableTools) {
        
        log.info("Received unified chat request: sessionId={}, message={}, provider={}, model={}, tools={}", 
                sessionId, message, provider, model, enableTools);
        
        Long userId = securityUtil.getCurrentUserId();
        
        return unifiedChatService.chat(sessionId, message, userId, provider, model, enableTools)
                .filter(content -> content != null && !content.isEmpty())
                .doOnComplete(() -> log.info("Unified chat completed: sessionId={}", sessionId))
                .doOnError(error -> log.error("Unified chat error: sessionId={}", sessionId, error));
    }

    /**
     * 流式聊天（SSE）- 基础版本
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式聊天", description = "使用SSE流式返回AI响应，支持选择供应商和模型")
    @SiaeAuthorize("isAuthenticated()")
    public Flux<String> chatStream(
            @Parameter(description = "用户消息内容", required = true)
            @RequestParam String message,
            @Parameter(description = "会话ID，可选，不传则创建新会话")
            @RequestParam(required = false) String sessionId,
            @Parameter(description = "供应商名称，可选，不传则使用默认供应商")
            @RequestParam(required = false) String provider,
            @Parameter(description = "模型名称，可选，不传则使用供应商默认模型")
            @RequestParam(required = false) String model) {
        
        log.info("Received stream chat request: sessionId={}, message={}, provider={}, model={}", 
                sessionId, message, provider, model);
        
        Long userId = securityUtil.getCurrentUserId();
        log.debug("Current user ID: {}", userId);
        
        return chatService.chatStream(sessionId, message, userId, provider, model)
                .filter(content -> content != null && !content.isEmpty())
                .doOnComplete(() -> log.info("Stream chat completed: sessionId={}", sessionId))
                .doOnError(error -> log.error("Stream chat error: sessionId={}", sessionId, error));
    }

    /**
     * 获取可用供应商和模型列表
     */
    @GetMapping("/providers")
    @Operation(summary = "获取可用供应商和模型", description = "返回所有可用的AI供应商及其模型列表")
    @SiaeAuthorize("isAuthenticated()")
    public Result<Map<String, ProviderInfo>> getProviders() {
        log.info("Fetching available providers");
        Map<String, ProviderInfo> providers = providerManager.getAvailableProviders();
        log.info("Retrieved {} providers", providers.size());
        return Result.success(providers);
    }

    /**
     * 获取用户会话列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取会话列表", description = "获取当前用户的历史会话列表（不含消息内容）")
    @SiaeAuthorize("isAuthenticated()")
    public Result<List<SessionListVO>> getSessions(
            @Parameter(description = "返回数量限制")
            @RequestParam(defaultValue = "20") int limit) {
        
        Long userId = securityUtil.getCurrentUserId();
        log.info("Fetching session list for user: {}, limit: {}", userId, limit);
        
        List<SessionListVO> sessions = sessionService.getUserSessions(userId, limit);
        
        log.info("Retrieved {} sessions for user: {}", sessions.size(), userId);
        return Result.success(sessions);
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/sessions/{sessionId}/history")
    @Operation(summary = "获取会话历史", description = "返回指定会话的所有消息记录")
    @SiaeAuthorize("isAuthenticated()")
    public Result<List<ChatMessage>> getHistory(
            @Parameter(description = "会话ID", required = true)
            @PathVariable String sessionId) {
        
        Long userId = securityUtil.getCurrentUserId();
        log.info("Fetching conversation history: sessionId={}, userId={}", sessionId, userId);
        
        if (!sessionService.isOwnedByUser(sessionId, userId)) {
            log.warn("User {} attempted to access session {} that doesn't belong to them", userId, sessionId);
            return Result.error("无权访问该会话");
        }
        
        List<ChatMessage> history = sessionService.getMessages(sessionId);
        
        log.info("Retrieved {} messages for session: {}", history.size(), sessionId);
        return Result.success(history);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除会话", description = "删除指定会话及其所有消息记录")
    @SiaeAuthorize("isAuthenticated()")
    public Result<Void> deleteSession(
            @Parameter(description = "会话ID", required = true)
            @PathVariable String sessionId) {
        
        Long userId = securityUtil.getCurrentUserId();
        log.info("Deleting session: sessionId={}, userId={}", sessionId, userId);
        
        if (!sessionService.isOwnedByUser(sessionId, userId)) {
            log.warn("User {} attempted to delete session {} that doesn't belong to them", userId, sessionId);
            return Result.error("无权删除该会话");
        }
        
        sessionService.deleteSession(sessionId);
        
        log.info("Session deleted successfully: sessionId={}", sessionId);
        return Result.success();
    }

    /**
     * 修改会话标题
     */
    @PutMapping("/sessions/{sessionId}/title")
    @Operation(summary = "修改会话标题", description = "修改指定会话的标题")
    @SiaeAuthorize("isAuthenticated()")
    public Result<Void> updateTitle(
            @Parameter(description = "会话ID", required = true)
            @PathVariable String sessionId,
            @Valid @RequestBody UpdateSessionTitleRequest request) {
        
        Long userId = securityUtil.getCurrentUserId();
        log.info("Updating session title: sessionId={}, userId={}, newTitle={}", 
                sessionId, userId, request.getTitle());
        
        if (!sessionService.isOwnedByUser(sessionId, userId)) {
            log.warn("User {} attempted to update session {} that doesn't belong to them", userId, sessionId);
            return Result.error("无权修改该会话");
        }
        
        sessionService.updateTitle(sessionId, request.getTitle());
        
        log.info("Session title updated successfully: sessionId={}", sessionId);
        return Result.success();
    }
}
