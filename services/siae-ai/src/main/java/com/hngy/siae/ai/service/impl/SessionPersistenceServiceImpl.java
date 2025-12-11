package com.hngy.siae.ai.service.impl;

import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.model.ChatSession;
import com.hngy.siae.ai.domain.vo.SessionListVO;
import com.hngy.siae.ai.entity.ChatSessionEntity;
import com.hngy.siae.ai.mapper.ChatSessionMapper;
import com.hngy.siae.ai.service.SessionPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话持久化服务实现
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionPersistenceServiceImpl implements SessionPersistenceService {

    private final ChatSessionMapper chatSessionMapper;

    private static final int TITLE_MAX_LENGTH = 50;

    @Override
    @Transactional
    public synchronized void saveSession(ChatSession session) {
        if (session == null || session.getSessionId() == null) {
            return;
        }

        String sessionId = session.getSessionId();
        
        try {
            ChatSessionEntity existing = chatSessionMapper.selectBySessionId(sessionId);
            
            if (existing != null) {
                // 更新（只有当新数据更完整时才更新）
                if (session.getMessages() != null && session.getMessages().size() >= 
                    (existing.getMessages() != null ? existing.getMessages().size() : 0)) {
                    existing.setMessages(session.getMessages());
                    existing.setTitle(generateTitle(session.getMessages()));
                    chatSessionMapper.updateById(existing);
                    log.debug("Updated session in DB: {}", sessionId);
                }
            } else {
                // 新增
                ChatSessionEntity entity = ChatSessionEntity.builder()
                        .sessionId(sessionId)
                        .userId(session.getUserId())
                        .title(generateTitle(session.getMessages()))
                        .messages(session.getMessages() != null ? session.getMessages() : new ArrayList<>())
                        .createTime(session.getCreatedAt())
                        .updateTime(session.getLastAccessTime())
                        .build();
                chatSessionMapper.insert(entity);
                log.debug("Inserted new session to DB: {}", sessionId);
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 并发插入冲突，改为更新
            log.debug("Duplicate key, switching to update: {}", sessionId);
            ChatSessionEntity existing = chatSessionMapper.selectBySessionId(sessionId);
            if (existing != null && session.getMessages() != null) {
                existing.setMessages(session.getMessages());
                existing.setTitle(generateTitle(session.getMessages()));
                chatSessionMapper.updateById(existing);
            }
        }
    }

    @Override
    @Async
    public void saveSessionAsync(ChatSession session) {
        try {
            saveSession(session);
        } catch (Exception e) {
            log.error("Async save session failed: {}", session.getSessionId(), e);
        }
    }

    @Override
    public ChatSession loadSession(String sessionId) {
        ChatSessionEntity entity = chatSessionMapper.selectBySessionId(sessionId);
        if (entity == null) {
            return null;
        }

        return ChatSession.builder()
                .sessionId(entity.getSessionId())
                .userId(entity.getUserId())
                .messages(entity.getMessages() != null ? entity.getMessages() : new ArrayList<>())
                .createdAt(entity.getCreateTime())
                .lastAccessTime(entity.getUpdateTime())
                .build();
    }

    @Override
    public List<SessionListVO> getUserSessionList(Long userId, int limit) {
        List<ChatSessionEntity> entities = chatSessionMapper.selectSessionListByUserId(userId, limit);
        
        return entities.stream()
                .map(e -> SessionListVO.builder()
                        .sessionId(e.getSessionId())
                        .title(e.getTitle())
                        .createTime(e.getCreateTime())
                        .updateTime(e.getUpdateTime())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        ChatSessionEntity entity = chatSessionMapper.selectBySessionId(sessionId);
        if (entity != null) {
            chatSessionMapper.deleteById(entity.getId());
            log.info("Deleted session from DB: {}", sessionId);
        }
    }

    @Override
    public String generateTitle(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "新对话";
        }

        // 找到第一条用户消息
        for (ChatMessage msg : messages) {
            if (ChatMessage.ROLE_USER.equals(msg.getRole()) && msg.getContent() != null) {
                String content = msg.getContent().trim();
                if (content.length() > TITLE_MAX_LENGTH) {
                    return content.substring(0, TITLE_MAX_LENGTH) + "...";
                }
                return content;
            }
        }

        return "新对话";
    }
}
