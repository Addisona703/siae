package com.hngy.siae.ai.exception;

import com.hngy.siae.core.result.IResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI服务错误码枚举
 * <p>
 * 定义AI服务中所有可能的错误码和对应的错误消息
 * <p>
 * Requirements: 5.4, 9.4
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum AiResultCodeEnum implements IResultCode {

    /**
     * 空消息或仅空白字符
     */
    EMPTY_MESSAGE(4001, "消息内容不能为空"),

    /**
     * 无效的会话ID
     */
    INVALID_SESSION_ID(4002, "无效的会话ID"),

    /**
     * 需要认证
     */
    AUTHENTICATION_REQUIRED(4003, "请先登录"),

    /**
     * 权限不足
     */
    PERMISSION_DENIED(4004, "您没有权限执行此操作"),

    /**
     * LLM提供商错误
     */
    LLM_PROVIDER_ERROR(5001, "AI服务暂时不可用，请稍后重试"),

    /**
     * 响应超时
     */
    RESPONSE_TIMEOUT(5002, "响应超时，请重试"),

    /**
     * 工具执行错误
     */
    TOOL_EXECUTION_ERROR(5003, "数据查询失败，请稍后重试"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(5004, "AI服务正在维护中");

    private final Integer code;
    private final String message;

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
