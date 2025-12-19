package com.hngy.siae.ai.client;

import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.model.ChatOptions;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * LLM 客户端接口
 * <p>
 * 统一的 LLM 调用接口，不同供应商（如智谱 AI、OpenAI、DeepSeek）实现此接口。
 * 提供流式聊天能力，支持多模型调用、工具调用和思考过程显示。
 * <p>
 * Requirements: 3.1
 *
 * @author SIAE Team
 */
public interface LlmClient {

    /**
     * 流式聊天（基础版本）
     * <p>
     * 向 LLM 发送消息并以流式方式接收响应。
     * 返回的 Flux 会逐步发出响应内容片段。
     *
     * @param model    模型名称，如 "glm-4-flash"、"gpt-3.5-turbo"
     * @param messages 消息列表，包含对话历史
     * @param options  调用选项，包含温度、最大 token 数等参数
     * @return 响应内容流，每个元素为一个内容片段
     */
    Flux<String> chatStream(String model, List<ChatMessage> messages, ChatOptions options);

    /**
     * 统一流式聊天（支持工具调用和思考过程）
     * <p>
     * 向 LLM 发送消息并以流式方式接收响应，支持：
     * - 普通内容流式输出
     * - 思考过程流式输出（reasoning_content，如 DeepSeek）
     * - 工具调用检测和增量解析
     *
     * @param model    模型名称
     * @param messages 消息列表，包含对话历史
     * @param options  调用选项
     * @param tools    工具定义列表，可为 null
     * @return 解析后的响应流
     */
    default Flux<StreamResponseParser.ParseResult> chatStreamUnified(
            String model, 
            List<ChatMessage> messages, 
            ChatOptions options,
            List<Map<String, Object>> tools) {
        // 默认实现：转换基础流式响应
        return chatStream(model, messages, options)
                .map(content -> new StreamResponseParser.ParseResult(content, null, null, null));
    }

    /**
     * 获取供应商名称
     * <p>
     * 返回此客户端对应的供应商标识符，如 "zhipu"、"openai"。
     *
     * @return 供应商名称
     */
    String getProviderName();

    /**
     * 检查客户端是否可用
     * <p>
     * 验证客户端配置是否有效，包括 API Key 和 Base URL 是否已配置。
     *
     * @return 客户端是否可用
     */
    boolean isAvailable();

    /**
     * 获取供应商显示名称
     * <p>
     * 返回用于前端展示的供应商名称，如 "智谱AI"、"OpenAI"。
     *
     * @return 供应商显示名称
     */
    default String getDisplayName() {
        return getProviderName();
    }

    /**
     * 检查模型是否支持思考过程（reasoning）
     *
     * @param model 模型名称
     * @return 是否支持思考过程
     */
    default boolean supportsReasoning(String model) {
        return false;
    }

    /**
     * 检查模型是否支持工具调用
     *
     * @param model 模型名称
     * @return 是否支持工具调用
     */
    default boolean supportsToolCalls(String model) {
        return true;
    }
}
