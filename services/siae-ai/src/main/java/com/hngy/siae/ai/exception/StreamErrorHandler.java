package com.hngy.siae.ai.exception;

import com.hngy.siae.ai.domain.vo.StreamResponse;
import com.hngy.siae.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * 流式响应错误处理器
 * <p>
 * 负责将各种异常转换为统一的流式错误响应格式。
 * <p>
 * Requirements: 4.5, 9.4
 *
 * @author SIAE Team
 */
@Slf4j
public class StreamErrorHandler {

    private StreamErrorHandler() {
        // 工具类，禁止实例化
    }

    /**
     * 将异常转换为流式错误响应
     *
     * @param sessionId 会话ID
     * @param error     异常
     * @return 包含错误信息的Flux
     */
    public static Flux<String> handleError(String sessionId, Throwable error) {
        log.error("Stream error occurred - sessionId: {}, error: {}", sessionId, error.getMessage(), error);

        StreamResponse errorResponse = createErrorResponse(sessionId, error);
        return Flux.just(errorResponse.toJson());
    }

    /**
     * 根据异常类型创建对应的错误响应
     */
    public static StreamResponse createErrorResponse(String sessionId, Throwable error) {
        // 处理业务异常
        if (error instanceof BusinessException) {
            BusinessException be = (BusinessException) error;
            return StreamResponse.error(sessionId, be.getCode(), be.getMessage());
        }

        // 处理超时异常
        if (error instanceof TimeoutException || error instanceof SocketTimeoutException) {
            return StreamResponse.error(sessionId, 
                    AiResultCodeEnum.RESPONSE_TIMEOUT.getCode(),
                    AiResultCodeEnum.RESPONSE_TIMEOUT.getMessage());
        }

        // 处理连接异常
        if (error instanceof ConnectException) {
            return StreamResponse.error(sessionId,
                    AiResultCodeEnum.SERVICE_UNAVAILABLE.getCode(),
                    AiResultCodeEnum.SERVICE_UNAVAILABLE.getMessage());
        }

        // 处理认证相关异常（通常是401错误）
        String errorMessage = error.getMessage();
        if (errorMessage != null) {
            String lowerMessage = errorMessage.toLowerCase();
            if (lowerMessage.contains("401") || lowerMessage.contains("unauthorized") 
                    || lowerMessage.contains("invalid api key")) {
                return StreamResponse.error(sessionId,
                        AiResultCodeEnum.API_KEY_INVALID.getCode(),
                        AiResultCodeEnum.API_KEY_INVALID.getMessage());
            }
            
            // 处理供应商不存在
            if (lowerMessage.contains("provider not found") || lowerMessage.contains("供应商不存在")) {
                return StreamResponse.error(sessionId,
                        AiResultCodeEnum.PROVIDER_NOT_FOUND.getCode(),
                        errorMessage);
            }
            
            // 处理模型不存在
            if (lowerMessage.contains("model not found") || lowerMessage.contains("模型不存在")
                    || lowerMessage.contains("不支持模型")) {
                return StreamResponse.error(sessionId,
                        AiResultCodeEnum.MODEL_NOT_FOUND.getCode(),
                        errorMessage);
            }
        }

        // 默认返回通用LLM错误
        return StreamResponse.error(sessionId,
                AiResultCodeEnum.LLM_PROVIDER_ERROR.getCode(),
                AiResultCodeEnum.LLM_PROVIDER_ERROR.getMessage());
    }

    /**
     * 创建供应商不存在的错误响应
     */
    public static StreamResponse providerNotFoundError(String sessionId, String provider) {
        String message = String.format("供应商 '%s' 不存在", provider);
        return StreamResponse.error(sessionId, AiResultCodeEnum.PROVIDER_NOT_FOUND.getCode(), message);
    }

    /**
     * 创建模型不存在的错误响应
     */
    public static StreamResponse modelNotFoundError(String sessionId, String provider, String model) {
        String message = String.format("供应商 '%s' 不支持模型 '%s'", provider, model);
        return StreamResponse.error(sessionId, AiResultCodeEnum.MODEL_NOT_FOUND.getCode(), message);
    }

    /**
     * 创建API Key无效的错误响应
     */
    public static StreamResponse apiKeyInvalidError(String sessionId, String provider) {
        String message = String.format("供应商 '%s' 的API Key无效", provider);
        return StreamResponse.error(sessionId, AiResultCodeEnum.API_KEY_INVALID.getCode(), message);
    }

    /**
     * 创建超时错误响应
     */
    public static StreamResponse timeoutError(String sessionId) {
        return StreamResponse.error(sessionId,
                AiResultCodeEnum.RESPONSE_TIMEOUT.getCode(),
                AiResultCodeEnum.RESPONSE_TIMEOUT.getMessage());
    }

    /**
     * 创建服务不可用错误响应
     */
    public static StreamResponse serviceUnavailableError(String sessionId) {
        return StreamResponse.error(sessionId,
                AiResultCodeEnum.SERVICE_UNAVAILABLE.getCode(),
                AiResultCodeEnum.SERVICE_UNAVAILABLE.getMessage());
    }
}
