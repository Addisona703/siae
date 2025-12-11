package com.hngy.siae.ai.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天响应VO
 * <p>
 * 用于返回AI的聊天响应结果
 * <p>
 * Requirements: 2.1, 6.1
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * AI响应内容
     */
    private String content;

    /**
     * 工具调用信息列表
     * 记录AI在生成响应过程中调用的工具
     */
    private List<ToolCallInfo> toolCalls;

    /**
     * 响应时间戳
     */
    private LocalDateTime timestamp;
}
