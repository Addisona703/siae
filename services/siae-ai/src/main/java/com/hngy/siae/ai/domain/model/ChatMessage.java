package com.hngy.siae.ai.domain.model;

import com.hngy.siae.ai.domain.vo.ToolCallInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息模型
 * <p>
 * 表示对话中的单条消息，包含角色、内容、时间戳和工具调用信息
 * <p>
 * Requirements: 2.1, 6.1
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * 消息角色
     * user: 用户消息
     * assistant: AI助手消息
     * system: 系统消息
     * tool: 工具执行结果消息
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 工具调用信息列表
     * 仅assistant角色的消息可能包含此信息
     */
    private List<ToolCallInfo> toolCalls;

    /**
     * 工具调用ID（仅tool角色消息使用）
     */
    private String toolCallId;

    /**
     * 消息角色常量
     */
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_TOOL = "tool";

    /**
     * 创建用户消息
     *
     * @param content 消息内容
     * @return 用户消息实例
     */
    public static ChatMessage userMessage(String content) {
        return ChatMessage.builder()
                .role(ROLE_USER)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建AI助手消息
     *
     * @param content   消息内容
     * @param toolCalls 工具调用信息
     * @return AI助手消息实例
     */
    public static ChatMessage assistantMessage(String content, List<ToolCallInfo> toolCalls) {
        return ChatMessage.builder()
                .role(ROLE_ASSISTANT)
                .content(content)
                .timestamp(LocalDateTime.now())
                .toolCalls(toolCalls)
                .build();
    }

    /**
     * 创建系统消息
     *
     * @param content 消息内容
     * @return 系统消息实例
     */
    public static ChatMessage systemMessage(String content) {
        return ChatMessage.builder()
                .role(ROLE_SYSTEM)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建工具执行结果消息
     *
     * @param toolCallId 工具调用ID
     * @param content    执行结果内容
     * @return 工具结果消息实例
     */
    public static ChatMessage toolMessage(String toolCallId, String content) {
        return ChatMessage.builder()
                .role(ROLE_TOOL)
                .toolCallId(toolCallId)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
