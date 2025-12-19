package com.hngy.siae.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.ai.config.AiProviderProperties;
import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.model.ChatSession;
import com.hngy.siae.ai.domain.vo.SessionListVO;
import com.hngy.siae.ai.entity.ChatSessionEntity;
import com.hngy.siae.ai.mapper.ChatSessionMapper;
import com.hngy.siae.ai.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 会话服务实现
 * <p>
 * 直接操作 MySQL，不使用 Redis 缓存。
 * 实现所有 CRUD 操作，使用 JSON 格式存储消息列表。
 * <p>
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final ChatSessionMapper chatSessionMapper;
    private final AiProviderProperties aiProviderProperties;

    private static final int TITLE_MAX_LENGTH = 50;
    private static final String DEFAULT_TITLE = "新对话";

    /**
     * 根据 sessionId 查询会话（使用 MyBatis-Plus 方式，确保 TypeHandler 生效）
     */
    private ChatSessionEntity findBySessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        return chatSessionMapper.selectOne(
                new LambdaQueryWrapper<ChatSessionEntity>()
                        .eq(ChatSessionEntity::getSessionId, sessionId)
        );
    }

    @Override
    @Transactional
    public String createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        ChatSessionEntity entity = ChatSessionEntity.builder()
                .sessionId(sessionId)
                .userId(userId)
                .title(DEFAULT_TITLE)
                .messages(new ArrayList<>())
                .createTime(now)
                .updateTime(now)
                .build();

        chatSessionMapper.insert(entity);
        log.info("Created new session: sessionId={}, userId={}", sessionId, userId);

        return sessionId;
    }

    @Override
    public ChatSession getSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }

        ChatSessionEntity entity = findBySessionId(sessionId);
        if (entity == null) {
            return null;
        }

        return convertToSession(entity);
    }

    @Override
    @Transactional
    public void addMessage(String sessionId, ChatMessage message) {
        if (sessionId == null || sessionId.isBlank() || message == null) {
            log.warn("Invalid parameters for addMessage: sessionId={}, message={}", sessionId, message);
            return;
        }

        ChatSessionEntity entity = findBySessionId(sessionId);
        if (entity == null) {
            log.warn("Session not found: {}", sessionId);
            return;
        }

        // 获取当前消息列表
        List<ChatMessage> messages = entity.getMessages();
        if (messages == null) {
            messages = new ArrayList<>();
        }

        // 设置消息时间戳（如果未设置）
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }

        // 添加新消息
        messages.add(message);

        // 检查是否超过最大消息数限制
        int maxMessages = aiProviderProperties.getSession().getMaxMessages();
        if (messages.size() > maxMessages) {
            // 移除最早的消息（保留系统消息）
            messages = trimMessages(messages, maxMessages);
            log.debug("Trimmed messages for session {}, new size: {}", sessionId, messages.size());
        }

        // 更新标题（如果是第一条用户消息且标题为默认值）
        String title = entity.getTitle();
        if (DEFAULT_TITLE.equals(title) && ChatMessage.ROLE_USER.equals(message.getRole())) {
            title = generateTitle(messages);
        }

        // 更新数据库
        entity.setMessages(messages);
        entity.setTitle(title);
        entity.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.updateById(entity);

        log.debug("Added message to session {}, total messages: {}", sessionId, messages.size());
    }

    @Override
    public List<ChatMessage> getMessages(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return new ArrayList<>();
        }

        ChatSessionEntity entity = findBySessionId(sessionId);
        if (entity == null || entity.getMessages() == null) {
            log.warn("No messages found for session: {}", sessionId);
            return new ArrayList<>();
        }

        List<ChatMessage> messages = new ArrayList<>(entity.getMessages());
        log.info("Retrieved {} messages for session: {}", messages.size(), sessionId);
        // 打印每条消息的角色和内容长度
        messages.forEach(msg -> log.debug("Message - role: {}, content length: {}", 
                msg.getRole(), msg.getContent() != null ? msg.getContent().length() : 0));
        
        return messages;
    }

    @Override
    public List<SessionListVO> getUserSessions(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return new ArrayList<>();
        }

        List<ChatSessionEntity> entities = chatSessionMapper.selectSessionListByUserId(userId, limit);

        return entities.stream()
                .map(this::convertToSessionListVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        ChatSessionEntity entity = findBySessionId(sessionId);
        if (entity != null) {
            chatSessionMapper.deleteById(entity.getId());
            log.info("Deleted session: {}", sessionId);
        }
    }

    @Override
    @Transactional
    public void updateTitle(String sessionId, String title) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        ChatSessionEntity entity = findBySessionId(sessionId);
        if (entity != null) {
            // 截断过长的标题
            String truncatedTitle = title;
            if (title != null && title.length() > TITLE_MAX_LENGTH) {
                truncatedTitle = title.substring(0, TITLE_MAX_LENGTH) + "...";
            }

            entity.setTitle(truncatedTitle);
            entity.setUpdateTime(LocalDateTime.now());
            chatSessionMapper.updateById(entity);
            log.info("Updated session title: sessionId={}, title={}", sessionId, truncatedTitle);
        }
    }

    @Override
    public boolean isOwnedByUser(String sessionId, Long userId) {
        if (sessionId == null || sessionId.isBlank() || userId == null) {
            return false;
        }

        ChatSessionEntity entity = findBySessionId(sessionId);
        if (entity == null) {
            return false;
        }
        
        // 开发环境：如果 userId 是默认值 1，允许访问所有会话
        if (userId.equals(1L)) {
            log.debug("Dev mode: allowing access to session {} for default user", sessionId);
            return true;
        }
        
        return userId.equals(entity.getUserId());
    }

    @Override
    public boolean sessionExists(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }

        ChatSessionEntity entity = findBySessionId(sessionId);
        return entity != null;
    }

    @Override
    public String generateTitle(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return DEFAULT_TITLE;
        }

        // 找到第一条用户消息
        for (ChatMessage msg : messages) {
            if (ChatMessage.ROLE_USER.equals(msg.getRole()) && msg.getContent() != null) {
                String content = msg.getContent().trim();
                if (content.isEmpty()) {
                    continue;
                }
                if (content.length() > TITLE_MAX_LENGTH) {
                    return content.substring(0, TITLE_MAX_LENGTH) + "...";
                }
                return content;
            }
        }

        return DEFAULT_TITLE;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将实体转换为会话模型
     */
    private ChatSession convertToSession(ChatSessionEntity entity) {
        return ChatSession.builder()
                .sessionId(entity.getSessionId())
                .userId(entity.getUserId())
                .messages(entity.getMessages() != null ? entity.getMessages() : new ArrayList<>())
                .createdAt(entity.getCreateTime())
                .lastAccessTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 将实体转换为会话列表 VO
     */
    private SessionListVO convertToSessionListVO(ChatSessionEntity entity) {
        return SessionListVO.builder()
                .sessionId(entity.getSessionId())
                .title(entity.getTitle())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 裁剪消息列表，保留最新的消息
     * 系统消息始终保留
     */
    private List<ChatMessage> trimMessages(List<ChatMessage> messages, int maxSize) {
        if (messages.size() <= maxSize) {
            return messages;
        }

        List<ChatMessage> result = new ArrayList<>();

        // 首先保留所有系统消息
        List<ChatMessage> systemMessages = messages.stream()
                .filter(m -> ChatMessage.ROLE_SYSTEM.equals(m.getRole()))
                .collect(Collectors.toList());
        result.addAll(systemMessages);

        // 计算可以保留的非系统消息数量
        int remainingSlots = maxSize - systemMessages.size();
        if (remainingSlots <= 0) {
            return result;
        }

        // 获取非系统消息，保留最新的
        List<ChatMessage> nonSystemMessages = messages.stream()
                .filter(m -> !ChatMessage.ROLE_SYSTEM.equals(m.getRole()))
                .collect(Collectors.toList());

        int startIndex = Math.max(0, nonSystemMessages.size() - remainingSlots);
        result.addAll(nonSystemMessages.subList(startIndex, nonSystemMessages.size()));

        return result;
    }
}
