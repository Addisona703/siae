package com.hngy.siae.ai.client;

import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.vo.ProviderInfo;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 供应商管理器接口
 * <p>
 * 负责管理和调用不同的 LLM 供应商，提供统一的聊天接口。
 * 支持多供应商配置、默认供应商回退、供应商和模型验证等功能。
 * <p>
 * Requirements: 2.1, 2.2, 2.3, 2.4
 *
 * @author SIAE Team
 */
public interface ProviderManager {

    /**
     * 流式聊天
     * <p>
     * 向指定供应商的 LLM 发送消息并以流式方式接收响应。
     * 如果未指定供应商或模型，将使用配置的默认值。
     *
     * @param provider 供应商名称，为空则使用默认供应商
     * @param model    模型名称，为空则使用供应商默认模型
     * @param messages 消息历史
     * @return 响应内容流
     */
    Flux<String> chatStream(String provider, String model, List<ChatMessage> messages);

    /**
     * 获取指定供应商的客户端
     *
     * @param provider 供应商名称
     * @return LLM 客户端
     * @throws IllegalArgumentException 如果供应商不存在
     */
    LlmClient getClient(String provider);

    /**
     * 获取所有可用供应商和模型
     * <p>
     * 返回所有已配置且可用的供应商信息，包括模型列表和默认模型。
     *
     * @return 供应商信息映射，key 为供应商名称
     */
    Map<String, ProviderInfo> getAvailableProviders();

    /**
     * 验证供应商和模型是否有效
     * <p>
     * 检查指定的供应商是否存在且可用，以及模型是否在该供应商的可用列表中。
     *
     * @param provider 供应商名称
     * @param model    模型名称，可为空（使用默认模型）
     * @return 组合是否有效
     */
    boolean isValidProviderAndModel(String provider, String model);

    /**
     * 获取默认供应商名称
     *
     * @return 默认供应商名称
     */
    String getDefaultProvider();

    /**
     * 获取指定供应商的默认模型
     *
     * @param provider 供应商名称
     * @return 默认模型名称，如果供应商不存在则返回 null
     */
    String getDefaultModel(String provider);

    /**
     * 获取有效的供应商名称
     * <p>
     * 如果指定的供应商无效，返回默认供应商。
     *
     * @param provider 供应商名称，可为空
     * @return 有效的供应商名称
     */
    String getEffectiveProvider(String provider);

    /**
     * 获取有效的模型名称
     * <p>
     * 如果指定的模型无效，返回供应商的默认模型。
     *
     * @param provider 供应商名称
     * @param model    模型名称，可为空
     * @return 有效的模型名称
     */
    String getEffectiveModel(String provider, String model);
}
