package com.hngy.siae.ai.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天调用选项
 * <p>
 * 封装 LLM 调用时的参数配置，包括温度、最大 token 数、系统提示词等。
 * <p>
 * Requirements: 3.4
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatOptions {

    /**
     * 温度参数
     * 控制生成文本的随机性，范围 0.0-2.0
     * 较低值产生更确定的输出，较高值产生更多样化的输出
     */
    private Double temperature;

    /**
     * 最大生成 token 数
     * 控制 AI 响应的最大长度
     */
    private Integer maxTokens;

    /**
     * 系统提示词
     * 用于设置 AI 的角色和行为
     */
    private String systemPrompt;

    /**
     * 响应超时时间（秒）
     */
    private Integer responseTimeout;

    /**
     * 创建默认选项
     *
     * @return 默认配置的 ChatOptions 实例
     */
    public static ChatOptions defaults() {
        return ChatOptions.builder()
                .temperature(0.7)
                .maxTokens(2000)
                .systemPrompt("你是一个智能助手")
                .responseTimeout(60)
                .build();
    }

    /**
     * 从配置创建选项
     *
     * @param temperature     温度参数
     * @param maxTokens       最大 token 数
     * @param systemPrompt    系统提示词
     * @param responseTimeout 响应超时时间
     * @return ChatOptions 实例
     */
    public static ChatOptions of(Double temperature, Integer maxTokens, 
                                  String systemPrompt, Integer responseTimeout) {
        return ChatOptions.builder()
                .temperature(temperature)
                .maxTokens(maxTokens)
                .systemPrompt(systemPrompt)
                .responseTimeout(responseTimeout)
                .build();
    }
}
