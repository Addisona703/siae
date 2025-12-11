package com.hngy.siae.ai.tool;

import com.hngy.siae.ai.exception.AiException;
import com.hngy.siae.core.exception.BusinessException;

/**
 * 工具执行异常工厂
 * <p>
 * 当AI工具函数执行过程中发生错误时抛出此异常。
 * 用于封装工具执行过程中的各种错误，如参数验证失败、远程调用失败等。
 * <p>
 * Requirements: 5.2, 5.4
 *
 * @author SIAE Team
 */
public class ToolExecutionException extends Throwable {

    /**
     * 创建工具执行异常
     *
     * @param message 错误消息
     */
    public static BusinessException of(String message) {
        return AiException.toolExecutionError(message);
    }

    /**
     * 创建工具执行异常
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public static BusinessException of(String message, Throwable cause) {
        return AiException.toolExecutionError(message + (cause != null ? ": " + cause.getMessage() : ""));
    }
}
