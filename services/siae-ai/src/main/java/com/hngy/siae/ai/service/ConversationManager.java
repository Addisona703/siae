package com.hngy.siae.ai.service;

import com.hngy.siae.ai.domain.model.ChatMessage;

import java.util.List;

/**
 * 对话会话管理器接口
 * <p>
 * 负责管理AI对话会话的生命周期，包括会话创建、消息存储、会话清理等
 * <p>
 * Requirements: 6.1, 6.2, 6.3, 6.4
 *
 * @author SIAE Team
 */
public interface ConversationManager {

    /**
     * 创建新会话
     *
     * @param userId 用户ID
     * @return 新创建的会话ID（UUID格式）
     */
    String createSession(Long userId);

    /**
     * 添加消息到会话
     * <p>
     * 如果会话消息数量超过配置的最大限制，将自动移除最早的消息
     *
     * @param sessionId 会话ID
     * @param message   要添加的消息
     */
    void addMessage(String sessionId, ChatMessage message);

    /**
     * 获取会话中的所有消息
     *
     * @param sessionId 会话ID
     * @return 消息列表，如果会话不存在则返回空列表
     */
    List<ChatMessage> getMessages(String sessionId);

    /**
     * 清除指定会话
     *
     * @param sessionId 会话ID
     */
    void clearSession(String sessionId);

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 如果会话存在返回true，否则返回false
     */
    boolean sessionExists(String sessionId);

    /**
     * 获取会话关联的用户ID
     *
     * @param sessionId 会话ID
     * @return 用户ID，如果会话不存在返回null
     */
    Long getUserId(String sessionId);

    /**
     * 清理过期会话
     * <p>
     * 移除所有超过配置超时时间未活动的会话
     *
     * @return 被清理的会话数量
     */
    int cleanupExpiredSessions();
}
