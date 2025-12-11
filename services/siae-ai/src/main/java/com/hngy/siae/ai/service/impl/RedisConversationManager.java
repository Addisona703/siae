package com.hngy.siae.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hngy.siae.ai.config.AiProperties;
import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.model.ChatSession;
import com.hngy.siae.ai.service.ConversationManager;
import com.hngy.siae.ai.service.SessionPersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的对话会话管理器实现
 * <p>
 * 使用单个JSON存储整个会话数据，支持分布式部署和数据持久化
 * 会话数据自动过期，无需手动清理
 * <p>
 * Redis Key格式: chat:session:{sessionId}
 * Value格式: {"sessionId":"xxx","userId":1,"messages":[...],"createdAt":"...","lastAccessTime":"..."}
 * <p>
 * Requirements: 6.1, 6.2, 6.3, 6.4
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@Primary
@ConditionalOnProperty(name = "siae.ai.session.storage", havingValue = "redis", matchIfMissing = true)
public class RedisConversationManager implements ConversationManager {

    private static final String SESSION_KEY_PREFIX = "chat:session:";

    private final StringRedisTemplate redisTemplate;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final SessionPersistenceService persistenceService;

    public RedisConversationManager(StringRedisTemplate redisTemplate, 
                                    AiProperties aiProperties,
                                    @Lazy SessionPersistenceService persistenceService) {
        this.redisTemplate = redisTemplate;
        this.aiProperties = aiProperties;
        this.persistenceService = persistenceService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }


    @Override
    public String createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        
        ChatSession session = ChatSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .messages(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .lastAccessTime(LocalDateTime.now())
                .build();

        saveSession(sessionId, session);
        log.info("Created new conversation session in Redis: {} for user: {}", sessionId, userId);
        return sessionId;
    }

    @Override
    public void addMessage(String sessionId, ChatMessage message) {
        ChatSession session = getSession(sessionId);
        if (session == null) {
            log.warn("Attempted to add message to non-existent session: {}", sessionId);
            return;
        }

        // 添加消息
        session.getMessages().add(message);

        // 如果超过最大限制，移除最早的非系统消息
        trimMessages(session.getMessages());

        // 更新访问时间
        session.setLastAccessTime(LocalDateTime.now());

        // 保存回Redis
        saveSession(sessionId, session);
        log.debug("Added message to session: {}, role: {}", sessionId, message.getRole());
    }

    @Override
    public List<ChatMessage> getMessages(String sessionId) {
        ChatSession session = getSession(sessionId);
        if (session == null) {
            log.debug("Session not found: {}", sessionId);
            return Collections.emptyList();
        }

        // 更新访问时间并保存
        session.setLastAccessTime(LocalDateTime.now());
        saveSession(sessionId, session);

        return new ArrayList<>(session.getMessages());
    }

    @Override
    public void clearSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        Boolean deleted = redisTemplate.delete(key);
        
        // 同时删除MySQL中的记录
        persistenceService.deleteSession(sessionId);
        
        if (Boolean.TRUE.equals(deleted)) {
            log.info("Cleared conversation session from Redis and MySQL: {}", sessionId);
        } else {
            log.debug("Attempted to clear non-existent session: {}", sessionId);
        }
    }

    @Override
    public boolean sessionExists(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return true;
        }
        // Redis没有，检查MySQL
        return persistenceService.loadSession(sessionId) != null;
    }

    @Override
    public Long getUserId(String sessionId) {
        ChatSession session = getSession(sessionId);
        return session != null ? session.getUserId() : null;
    }

    @Override
    public int cleanupExpiredSessions() {
        // Redis TTL自动处理过期，无需手动清理
        log.debug("Redis handles session expiry automatically via TTL");
        return 0;
    }

    /**
     * 从Redis获取会话，如果Redis没有则从MySQL加载并回写Redis
     */
    private ChatSession getSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String json = redisTemplate.opsForValue().get(key);

        if (json != null && !json.isEmpty()) {
            try {
                return objectMapper.readValue(json, ChatSession.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize session: {}", sessionId, e);
            }
        }

        // Redis没有，尝试从MySQL加载
        ChatSession session = persistenceService.loadSession(sessionId);
        if (session != null) {
            log.info("Loaded session from MySQL and caching to Redis: {}", sessionId);
            // 回写Redis
            try {
                String sessionJson = objectMapper.writeValueAsString(session);
                int timeoutMinutes = aiProperties.getSession().getTimeoutMinutes();
                redisTemplate.opsForValue().set(key, sessionJson, timeoutMinutes, TimeUnit.MINUTES);
            } catch (JsonProcessingException e) {
                log.error("Failed to cache session to Redis: {}", sessionId, e);
            }
        }

        return session;
    }

    /**
     * 保存会话到Redis，并异步持久化到MySQL
     */
    private void saveSession(String sessionId, ChatSession session) {
        String key = SESSION_KEY_PREFIX + sessionId;
        int timeoutMinutes = aiProperties.getSession().getTimeoutMinutes();

        try {
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(key, json, timeoutMinutes, TimeUnit.MINUTES);
            
            // 异步持久化到MySQL
            persistenceService.saveSessionAsync(session);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize session: {}", sessionId, e);
        }
    }

    /**
     * 裁剪消息列表，保持在最大限制内
     * 优先移除非系统消息
     */
    private void trimMessages(List<ChatMessage> messages) {
        int maxMessages = aiProperties.getSession().getMaxMessages();
        
        while (messages.size() > maxMessages) {
            boolean removed = false;
            // 优先移除最早的非系统消息
            for (int i = 0; i < messages.size(); i++) {
                if (!ChatMessage.ROLE_SYSTEM.equals(messages.get(i).getRole())) {
                    messages.remove(i);
                    removed = true;
                    break;
                }
            }
            // 如果全是系统消息，移除最早的
            if (!removed) {
                messages.remove(0);
            }
        }
    }
}
