package com.hngy.siae.ai.service;

import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.vo.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI聊天服务接口
 * <p>
 * 提供AI对话的核心功能，包括同步聊天、流式聊天、会话历史管理等
 * <p>
 * Requirements: 2.1, 2.4, 6.1
 *
 * @author SIAE Team
 */
public interface ChatService {

    /**
     * 同步聊天
     * <p>
     * 发送消息并等待AI完整响应
     *
     * @param sessionId 会话ID，如果为null则创建新会话
     * @param message   用户消息内容
     * @param userId    用户ID
     * @param fileIds   附加的文件ID列表，可选，用于图片识别等多模态功能
     * @return 聊天响应，包含AI回复内容和工具调用信息
     */
    ChatResponse chat(String sessionId, String message, Long userId, List<String> fileIds);

    /**
     * 流式聊天
     * <p>
     * 发送消息并以流的方式返回AI响应，支持实时显示
     *
     * @param sessionId 会话ID，如果为null则创建新会话
     * @param message   用户消息内容
     * @param userId    用户ID
     * @param model     模型名称，可选，为null时使用默认模型
     * @param fileIds   附加的文件ID列表，可选，用于图片识别等多模态功能
     * @return 响应内容的流，每个元素是响应的一部分
     */
    Flux<String> chatStream(String sessionId, String message, Long userId, String model, List<String> fileIds);

    /**
     * 支持 Thinking 的流式聊天
     * <p>
     * 发送消息并以流的方式返回AI响应，包含思考过程
     *
     * @param sessionId 会话ID，如果为null则创建新会话
     * @param message   用户消息内容
     * @param userId    用户ID
     * @param model     模型名称，可选，为null时使用默认模型
     * @param fileIds   附加的文件ID列表，可选，用于图片识别等多模态功能
     * @return 响应内容的流，包含 thinking 和 content 字段
     */
    Flux<String> chatStreamWithThinking(String sessionId, String message, Long userId, String model, List<String> fileIds);

    /**
     * 获取会话历史
     * <p>
     * 返回指定会话的所有消息记录
     *
     * @param sessionId 会话ID
     * @return 消息列表，如果会话不存在返回空列表
     */
    List<ChatMessage> getConversationHistory(String sessionId);

    /**
     * 清除会话
     * <p>
     * 删除指定会话及其所有消息记录
     *
     * @param sessionId 会话ID
     */
    void clearConversation(String sessionId);

    /**
     * 创建新会话
     *
     * @param userId 用户ID
     * @return 新创建的会话ID
     */
    String createSession(Long userId);

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 如果会话存在返回true，否则返回false
     */
    boolean sessionExists(String sessionId);
}
