package com.hngy.siae.ai.exception;

import com.hngy.siae.core.exception.BusinessException;

/**
 * AI服务异常工厂
 * <p>
 * 用于创建AI服务中的各种业务异常，基于 BusinessException 实现
 * <p>
 * Requirements: 5.4, 9.1, 9.2, 9.3, 9.4
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
     * 创建供应商不存在异常
     * Requirements: 9.1
     */
    public static BusinessException providerNotFound(String provider) {
        return new BusinessException(AiResultCodeEnum.PROVIDER_NOT_FOUND, 
                String.format("供应商 '%s' 不存在", provider));
    }

    /**
     * 创建模型不存在异常
     * Requirements: 9.2
     */
    public static BusinessException modelNotFound(String provider, String model) {
        return new BusinessException(AiResultCodeEnum.MODEL_NOT_FOUND, 
                String.format("供应商 '%s' 不支持模型 '%s'", provider, model));
    }

    /**
     * 创建会话不存在异常
     * Requirements: 9.3
     */
    public static BusinessException sessionNotFound(String sessionId) {
        return new BusinessException(AiResultCodeEnum.SESSION_NOT_FOUND, 
                String.format("会话 '%s' 不存在", sessionId));
    }

    /**
     * 创建无效消息异常
     */
    public static BusinessException invalidMessage(String reason) {
        return new BusinessException(AiResultCodeEnum.INVALID_MESSAGE, reason);
    }

    /**
     * 创建LLM提供商错误异常
     */
    public static BusinessException llmProviderError(Throwable cause) {
        return new BusinessException(AiResultCodeEnum.LLM_PROVIDER_ERROR, cause.getMessage());
    }

    /**
     * 创建响应超时异常
     * Requirements: 9.1
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
     * Requirements: 9.3
     */
    public static BusinessException serviceUnavailable() {
        return new BusinessException(AiResultCodeEnum.SERVICE_UNAVAILABLE);
    }

    /**
     * 创建API Key无效异常
     * Requirements: 9.2
     */
    public static BusinessException apiKeyInvalid(String provider) {
        return new BusinessException(AiResultCodeEnum.API_KEY_INVALID, 
                String.format("供应商 '%s' 的API Key无效", provider));
    }

    /**
     * 创建流式响应错误异常
     * Requirements: 4.5, 9.4
     */
    public static BusinessException streamError(String message) {
        return new BusinessException(AiResultCodeEnum.STREAM_ERROR, message);
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
