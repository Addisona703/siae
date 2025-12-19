package com.hngy.siae.ai.service;

import reactor.core.publisher.Flux;

/**
 * 统一聊天服务接口
 * <p>
 * 提供统一的流式聊天功能，支持：
 * - 普通内容流式输出
 * - 思考过程流式输出（reasoning，如 DeepSeek）
 * - 工具调用和执行
 * <p>
 * 所有功能通过单一接口实现，前端通过响应类型区分不同内容。
 *
 * @author SIAE Team
 */
public interface UnifiedChatService {

    /**
     * 统一流式聊天
     * <p>
     * 发送消息并以 SSE 流的方式返回 AI 响应，支持：
     * - type=content: 普通内容
     * - type=thinking: 思考过程
     * - type=tool_call: 工具调用请求
     * - type=tool_result: 工具执行结果
     * - type=done: 完成
     * - type=error: 错误
     *
     * @param sessionId    会话ID，如果为 null 则创建新会话
     * @param message      用户消息内容
     * @param userId       用户ID
     * @param provider     供应商名称，可选
     * @param model        模型名称，可选
     * @param enableTools  是否启用工具调用
     * @return SSE 响应流，每个元素是 JSON 格式的 StreamResponse
     */
    Flux<String> chat(String sessionId, String message, Long userId,
                      String provider, String model, boolean enableTools);

    /**
     * 统一流式聊天（默认启用工具）
     */
    default Flux<String> chat(String sessionId, String message, Long userId,
                              String provider, String model) {
        return chat(sessionId, message, userId, provider, model, true);
    }
}
