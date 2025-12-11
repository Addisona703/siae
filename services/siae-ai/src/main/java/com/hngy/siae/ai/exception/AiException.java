package com.hngy.siae.ai.exception;

import com.hngy.siae.core.exception.BusinessException;

/**
 * AI服务异常工厂
 * <p>
 * 用于创建AI服务中的各种业务异常，基于 BusinessException 实现
 * <p>
 * Requirements: 5.4, 9.4
 *
 * @author SIAE Team
 */
public class AiException {

    /**
     * 创建空消息异常
     */
    public static BusinessException emptyMessage() {
        return new BusinessException(AiResultCodeEnum.EMPTY_MESSAGE);
    }

    /**
     * 创建无效会话ID异常
     */
    public static BusinessException invalidSessionId() {
        return new BusinessException(AiResultCodeEnum.INVALID_SESSION_ID);
    }

    /**
     * 创建需要认证异常
     */
    public static BusinessException authenticationRequired() {
        return new BusinessException(AiResultCodeEnum.AUTHENTICATION_REQUIRED);
    }

    /**
     * 创建权限不足异常
     */
    public static BusinessException permissionDenied() {
        return new BusinessException(AiResultCodeEnum.PERMISSION_DENIED);
    }

    /**
     * 创建LLM提供商错误异常
     */
    public static BusinessException llmProviderError(Throwable cause) {
        return new BusinessException(AiResultCodeEnum.LLM_PROVIDER_ERROR, cause.getMessage());
    }

    /**
     * 创建响应超时异常
     */
    public static BusinessException responseTimeout() {
        return new BusinessException(AiResultCodeEnum.RESPONSE_TIMEOUT);
    }

    /**
     * 创建工具执行错误异常
     */
    public static BusinessException toolExecutionError(String message) {
        return new BusinessException(AiResultCodeEnum.TOOL_EXECUTION_ERROR, message);
    }

    /**
     * 创建服务不可用异常
     */
    public static BusinessException serviceUnavailable() {
        return new BusinessException(AiResultCodeEnum.SERVICE_UNAVAILABLE);
    }

    /**
     * 创建自定义AI异常
     */
    public static BusinessException of(AiResultCodeEnum resultCode) {
        return new BusinessException(resultCode);
    }

    /**
     * 创建自定义AI异常（带自定义消息）
     */
    public static BusinessException of(AiResultCodeEnum resultCode, String message) {
        return new BusinessException(resultCode, message);
    }
}
