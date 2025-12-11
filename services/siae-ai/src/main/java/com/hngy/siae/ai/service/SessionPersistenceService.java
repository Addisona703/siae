package com.hngy.siae.ai.service;

import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.model.ChatSession;
import com.hngy.siae.ai.domain.vo.SessionListVO;

import java.util.List;

/**
 * 会话持久化服务接口
 *
 * @author SIAE Team
 */
public interface SessionPersistenceService {

    /**
     * 保存或更新会话到数据库
     */
    void saveSession(ChatSession session);

    /**
     * 异步保存会话
     */
    void saveSessionAsync(ChatSession session);

    /**
     * 从数据库加载会话
     */
    ChatSession loadSession(String sessionId);

    /**
     * 获取用户的会话列表（轻量，不含messages）
     */
    List<SessionListVO> getUserSessionList(Long userId, int limit);

    /**
     * 删除会话
     */
    void deleteSession(String sessionId);

    /**
     * 生成会话标题
     */
    String generateTitle(List<ChatMessage> messages);
}
