package com.hngy.siae.ai.service;

import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.model.ChatSession;
import com.hngy.siae.ai.domain.vo.SessionListVO;

import java.util.List;

/**
 * 会话服务接口
 * <p>
 * 直接操作 MySQL 的会话管理服务，负责会话的创建、查询、更新和删除操作。
 * 替代原有的 ConversationManager 和 SessionPersistenceService，简化架构。
 * <p>
 * Requirements: 5.1, 5.4
 *
 * @author SIAE Team
 */
public interface SessionService {

    /**
     * 创建新会话
     *
     * @param userId 用户ID
     * @return 新创建的会话ID（UUID格式）
     */
    String createSession(Long userId);

    /**
     * 获取会话
     *
     * @param sessionId 会话ID
     * @return 会话对象，如果不存在则返回 null
     */
    ChatSession getSession(String sessionId);

    /**
     * 添加消息到会话
     * <p>
     * 如果会话消息数量超过配置的最大限制，将自动移除最早的消息。
     * 如果是第一条用户消息，将自动生成会话标题。
     *
     * @param sessionId 会话ID
     * @param message   要添加的消息
     */
    void addMessage(String sessionId, ChatMessage message);

    /**
     * 获取会话消息列表
     *
     * @param sessionId 会话ID
     * @return 消息列表，如果会话不存在则返回空列表
     */
    List<ChatMessage> getMessages(String sessionId);

    /**
     * 获取用户会话列表（轻量，不含 messages）
     *
     * @param userId 用户ID
     * @param limit  返回数量限制
     * @return 会话列表
     */
    List<SessionListVO> getUserSessions(Long userId, int limit);

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     */
    void deleteSession(String sessionId);

    /**
     * 更新会话标题
     *
     * @param sessionId 会话ID
     * @param title     新标题
     */
    void updateTitle(String sessionId, String title);

    /**
     * 检查会话是否属于指定用户
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @return 如果会话属于该用户返回 true，否则返回 false
     */
    boolean isOwnedByUser(String sessionId, Long userId);

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 如果会话存在返回 true，否则返回 false
     */
    boolean sessionExists(String sessionId);

    /**
     * 生成会话标题
     * <p>
     * 从消息列表中提取第一条用户消息作为标题
     *
     * @param messages 消息列表
     * @return 生成的标题
     */
    String generateTitle(List<ChatMessage> messages);
}
