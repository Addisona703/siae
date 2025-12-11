package com.hngy.siae.ai.service;

import com.hngy.siae.ai.exception.AiException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 聊天输入验证器
 * <p>
 * 提供消息内容和会话ID的验证功能
 * <p>
 * Requirements: 2.3
 *
 * @author SIAE Team
 */
@Component
public class ChatInputValidator {

    /**
     * UUID格式正则表达式
     */
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    /**
     * 验证消息内容
     * <p>
     * 拒绝空消息和仅包含空白字符的消息
     *
     * @param message 消息内容
     * @throws AiException 如果消息无效（错误码: AI_001）
     */
    public void validateMessage(String message) {
        if (!StringUtils.hasText(message)) {
            throw AiException.emptyMessage();
        }
        
        // 检查是否仅包含空白字符
        if (message.trim().isEmpty()) {
            throw AiException.emptyMessage();
        }
    }

    /**
     * 验证会话ID格式
     * <p>
     * 会话ID必须是有效的UUID格式
     *
     * @param sessionId 会话ID
     * @throws AiException 如果会话ID格式无效（错误码: AI_002）
     */
    public void validateSessionId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            // 空会话ID是允许的，会创建新会话
            return;
        }
        
        if (!isValidUuid(sessionId)) {
            throw AiException.invalidSessionId();
        }
    }

    /**
     * 验证会话ID格式（严格模式）
     * <p>
     * 会话ID必须存在且是有效的UUID格式
     *
     * @param sessionId 会话ID
     * @throws AiException 如果会话ID为空或格式无效（错误码: AI_002）
     */
    public void validateSessionIdStrict(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw AiException.invalidSessionId();
        }
        
        if (!isValidUuid(sessionId)) {
            throw AiException.invalidSessionId();
        }
    }

    /**
     * 检查字符串是否为有效的UUID格式
     *
     * @param str 要检查的字符串
     * @return 如果是有效的UUID格式返回true，否则返回false
     */
    public boolean isValidUuid(String str) {
        if (!StringUtils.hasText(str)) {
            return false;
        }
        
        // 首先使用正则表达式快速检查格式
        if (!UUID_PATTERN.matcher(str).matches()) {
            return false;
        }
        
        // 然后尝试解析以确保是有效的UUID
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 检查消息是否为空或仅包含空白字符
     *
     * @param message 消息内容
     * @return 如果消息为空或仅包含空白字符返回true，否则返回false
     */
    public boolean isEmptyOrWhitespace(String message) {
        return !StringUtils.hasText(message) || message.trim().isEmpty();
    }
}
