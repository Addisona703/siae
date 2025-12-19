package com.hngy.siae.ai.service;

import reactor.core.publisher.Flux;

/**
 * AI聊天服务接口
 * <p>
 * 提供流式聊天功能，支持多供应商和模型选择。
 * 简化后仅保留流式聊天接口，移除同步聊天和 Thinking 模式。
 * <p>
 * Requirements: 4.1, 6.4, 7.2, 7.3
 *
 * @author SIAE Team
 */
public interface ChatService {

    /**
     * 流式聊天
     * <p>
     * 发送消息并以 SSE 流的方式返回 AI 响应，支持实时显示。
     * 支持会话上下文，保持对话连贯性。
     *
     * @param sessionId 会话ID，如果为 null 则创建新会话
     * @param message   用户消息内容
     * @param userId    用户ID
     * @param provider  供应商名称，可选，为 null 时使用默认供应商
     * @param model     模型名称，可选，为 null 时使用供应商默认模型
     * @return SSE 响应流，每个元素是 JSON 格式的响应片段
     */
    Flux<String> chatStream(String sessionId, String message, Long userId, 
                           String provider, String model);
}
