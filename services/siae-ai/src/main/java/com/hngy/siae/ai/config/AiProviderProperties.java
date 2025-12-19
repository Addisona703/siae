package com.hngy.siae.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.*;

/**
 * AI 多供应商配置属性
 * <p>
 * 支持配置多个 LLM 供应商（如智谱 AI、OpenAI 等），
 * 每个供应商可配置独立的 API Key、Base URL 和可用模型列表。
 * <p>
 * 配置前缀: siae.ai
 * <p>
 * Requirements: 1.1, 1.2, 1.3, 8.2
 *
 * @author SIAE Team
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "siae.ai")
public class AiProviderProperties {

    /**
     * 默认供应商名称
     * 当用户未指定供应商时使用此配置
     * 默认: zhipu
     */
    private String defaultProvider = "zhipu";

    /**
     * 供应商配置映射
     * key: 供应商名称（如 zhipu, openai）
     * value: 供应商配置
     */
    private Map<String, ProviderConfig> providers = new HashMap<>();

    /**
     * 聊天参数配置
     */
    private ChatConfig chat = new ChatConfig();

    /**
     * 会话配置
     */
    private SessionConfig session = new SessionConfig();

    /**
     * 供应商配置
     */
    @Data
    public static class ProviderConfig {

        /**
         * API 密钥
         * 用于供应商认证
         */
        private String apiKey;

        /**
         * API 基础 URL
         * 例如: https://open.bigmodel.cn/api/paas/v4
         */
        private String baseUrl;

        /**
         * 可用模型列表
         */
        private List<String> models = new ArrayList<>();

        /**
         * 默认模型
         * 当用户未指定模型时使用
         */
        private String defaultModel;

        /**
         * 供应商显示名称
         * 用于前端展示
         */
        private String displayName;

        /**
         * 检查配置是否有效
         * @return 配置是否包含必要字段
         */
        public boolean isValid() {
            return apiKey != null && !apiKey.isBlank()
                    && baseUrl != null && !baseUrl.isBlank();
        }

        /**
         * 获取有效的默认模型
         * 如果未配置默认模型，返回模型列表中的第一个
         * @return 默认模型名称，如果无可用模型则返回 null
         */
        public String getEffectiveDefaultModel() {
            if (defaultModel != null && !defaultModel.isBlank()) {
                return defaultModel;
            }
            if (models != null && !models.isEmpty()) {
                return models.get(0);
            }
            return null;
        }

        /**
         * 检查模型是否可用
         * @param model 模型名称
         * @return 模型是否在可用列表中
         */
        public boolean isModelAvailable(String model) {
            if (model == null || model.isBlank()) {
                return false;
            }
            return models != null && models.contains(model);
        }
    }

    /**
     * 聊天参数配置
     */
    @Data
    public static class ChatConfig {

        /**
         * 温度参数
         * 控制生成文本的随机性，范围 0.0-2.0
         * 较低值产生更确定的输出，较高值产生更多样化的输出
         * 默认: 0.7
         */
        @Min(value = 0, message = "temperature 最小值为 0")
        @Max(value = 2, message = "temperature 最大值为 2")
        private Double temperature = 0.7;

        /**
         * 最大生成 token 数
         * 控制 AI 响应的最大长度
         * 默认: 2000
         */
        @Min(value = 100, message = "maxTokens 最小值为 100")
        @Max(value = 8000, message = "maxTokens 最大值为 8000")
        private Integer maxTokens = 2000;

        /**
         * 系统提示词
         * 用于设置 AI 的角色和行为
         */
        private String systemPrompt = "你是一个智能助手";

        /**
         * 响应超时时间（秒）
         * 默认: 60 秒
         */
        @Min(value = 5, message = "responseTimeout 最小值为 5 秒")
        @Max(value = 300, message = "responseTimeout 最大值为 300 秒")
        private Integer responseTimeout = 60;
    }

    /**
     * 会话配置
     */
    @Data
    public static class SessionConfig {

        /**
         * 会话中保留的最大消息数
         * 超过此数量时，较早的消息将被移除
         * 默认: 20
         */
        @Min(value = 5, message = "maxMessages 最小值为 5")
        @Max(value = 100, message = "maxMessages 最大值为 100")
        private Integer maxMessages = 20;
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取指定供应商配置
     * @param name 供应商名称
     * @return 供应商配置，如果不存在则返回 null
     */
    public ProviderConfig getProvider(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return providers.get(name.toLowerCase());
    }

    /**
     * 获取默认供应商配置
     * @return 默认供应商配置，如果未配置则返回 null
     */
    public ProviderConfig getDefaultProviderConfig() {
        return getProvider(defaultProvider);
    }

    /**
     * 获取所有可用供应商名称
     * 仅返回配置有效的供应商
     * @return 可用供应商名称集合
     */
    public Set<String> getAvailableProviders() {
        Set<String> available = new HashSet<>();
        for (Map.Entry<String, ProviderConfig> entry : providers.entrySet()) {
            if (entry.getValue() != null && entry.getValue().isValid()) {
                available.add(entry.getKey());
            }
        }
        return available;
    }

    /**
     * 检查供应商是否可用
     * @param providerName 供应商名称
     * @return 供应商是否配置有效
     */
    public boolean isProviderAvailable(String providerName) {
        ProviderConfig config = getProvider(providerName);
        return config != null && config.isValid();
    }

    /**
     * 检查供应商和模型组合是否有效
     * @param providerName 供应商名称
     * @param model 模型名称
     * @return 组合是否有效
     */
    public boolean isValidProviderAndModel(String providerName, String model) {
        ProviderConfig config = getProvider(providerName);
        if (config == null || !config.isValid()) {
            return false;
        }
        // 如果未指定模型，使用默认模型，视为有效
        if (model == null || model.isBlank()) {
            return true;
        }
        return config.isModelAvailable(model);
    }

    /**
     * 获取有效的供应商名称
     * 如果指定的供应商无效，返回默认供应商
     * @param providerName 供应商名称，可为 null
     * @return 有效的供应商名称
     */
    public String getEffectiveProvider(String providerName) {
        if (providerName != null && !providerName.isBlank() && isProviderAvailable(providerName)) {
            return providerName.toLowerCase();
        }
        return defaultProvider;
    }

    /**
     * 获取有效的模型名称
     * @param providerName 供应商名称
     * @param model 模型名称，可为 null
     * @return 有效的模型名称
     */
    public String getEffectiveModel(String providerName, String model) {
        ProviderConfig config = getProvider(providerName);
        if (config == null) {
            return null;
        }
        if (model != null && !model.isBlank() && config.isModelAvailable(model)) {
            return model;
        }
        return config.getEffectiveDefaultModel();
    }
}
