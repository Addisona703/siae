package com.hngy.siae.ai.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI错误响应DTO
 * <p>
 * 用于返回AI服务的错误信息，提供结构化的错误响应
 * <p>
 * Requirements: 5.4, 9.4
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiErrorResponse {

    /**
     * 错误码
     * <p>
     * 错误码定义：
     * - AI_001: 空消息或仅空白字符
     * - AI_002: 无效的会话ID
     * - AI_003: 需要认证
     * - AI_004: 权限不足
     * - AI_005: LLM提供商错误
     * - AI_006: 响应超时
     * - AI_007: 工具执行错误
     * - AI_008: 服务不可用
     */
    private String errorCode;

    /**
     * 错误消息
     * 用户友好的错误描述
     */
    private String message;

    /**
     * 详细信息
     * 可选的技术细节，用于调试
     */
    private String detail;

    /**
     * 时间戳
     * 错误发生的时间
     */
    private LocalDateTime timestamp;

    /**
     * 追踪ID
     * 用于日志追踪和问题排查
     */
    private String traceId;

    /**
     * 创建错误响应
     *
     * @param errorCode 错误码
     * @param message   错误消息
     * @return 错误响应实例
     */
    public static AiErrorResponse of(String errorCode, String message) {
        return AiErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建错误响应（带详细信息）
     *
     * @param errorCode 错误码
     * @param message   错误消息
     * @param detail    详细信息
     * @return 错误响应实例
     */
    public static AiErrorResponse of(String errorCode, String message, String detail) {
        return AiErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .detail(detail)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建错误响应（带追踪ID）
     *
     * @param errorCode 错误码
     * @param message   错误消息
     * @param detail    详细信息
     * @param traceId   追踪ID
     * @return 错误响应实例
     */
    public static AiErrorResponse of(String errorCode, String message, String detail, String traceId) {
        return AiErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .detail(detail)
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .build();
    }
}
