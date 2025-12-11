package com.hngy.siae.ai.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天会话模型
 * <p>
 * 用于Redis存储的完整会话数据结构，包含会话ID、用户ID、消息列表和时间信息
 * <p>
 * Requirements: 6.1, 6.2, 6.3, 6.4
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {

    /**
     * 会话ID（UUID格式）
     */
    private String sessionId;

    /**
     * 关联的用户ID
     */
    private Long userId;

    /**
     * 消息列表
     */
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * 会话创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;
}
