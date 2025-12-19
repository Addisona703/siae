package com.hngy.siae.ai.client.impl;

import com.hngy.siae.ai.client.LlmClient;
import com.hngy.siae.ai.client.ProviderManager;
import com.hngy.siae.ai.config.AiProviderProperties;
import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.model.ChatOptions;
import com.hngy.siae.ai.domain.vo.ProviderInfo;
import com.hngy.siae.ai.exception.AiException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * 供应商管理器实现
 * <p>
 * 管理多个 LlmClient 实例，提供统一的聊天接口和供应商管理功能。
 * <p>
 * Requirements: 2.1, 2.2, 2.3, 2.4, 9.1, 9.2, 9.3
 *
 * @author SIAE Team
 */
@Slf4j
public class ProviderManagerImpl implements ProviderManager {

    private final Map<String, LlmClient> clients;
    private final AiProviderProperties properties;

    /**
     * 构造函数
     *
     * @param clients    LLM 客户端映射，key 为供应商名称
     * @param properties AI 配置属性
     */
    public ProviderManagerImpl(Map<String, LlmClient> clients, AiProviderProperties properties) {
        this.clients = clients != null ? clients : new HashMap<>();
        this.properties = properties;
        log.info("ProviderManager initialized with {} clients: {}", 
                this.clients.size(), this.clients.keySet());
    }

    @Override
    public Flux<String> chatStream(String provider, String model, List<ChatMessage> messages) {
        // 获取有效的供应商和模型
        String effectiveProvider = getEffectiveProvider(provider);
        String effectiveModel = getEffectiveModel(effectiveProvider, model);

        log.debug("Chat stream request - provider: {} -> {}, model: {} -> {}", 
                provider, effectiveProvider, model, effectiveModel);

        // 获取客户端
        LlmClient client = clients.get(effectiveProvider);
        if (client == null || !client.isAvailable()) {
            log.error("Provider not available: {}", effectiveProvider);
            return Flux.error(AiException.providerNotFound(effectiveProvider));
        }

        // 验证模型是否可用
        AiProviderProperties.ProviderConfig config = properties.getProvider(effectiveProvider);
        if (config != null && !config.isModelAvailable(effectiveModel)) {
            log.error("Model not available: {} for provider: {}", effectiveModel, effectiveProvider);
            return Flux.error(AiException.modelNotFound(effectiveProvider, effectiveModel));
        }

        // 构建调用选项
        ChatOptions options = buildChatOptions();

        // 调用 LLM
        return client.chatStream(effectiveModel, messages, options);
    }

    @Override
    public LlmClient getClient(String provider) {
        String effectiveProvider = getEffectiveProvider(provider);
        LlmClient client = clients.get(effectiveProvider);
        if (client == null || !client.isAvailable()) {
            throw new IllegalArgumentException("Provider not available: " + effectiveProvider);
        }
        return client;
    }

    @Override
    public Map<String, ProviderInfo> getAvailableProviders() {
        Map<String, ProviderInfo> result = new LinkedHashMap<>();

        for (Map.Entry<String, LlmClient> entry : clients.entrySet()) {
            String providerName = entry.getKey();
            LlmClient client = entry.getValue();
            
            AiProviderProperties.ProviderConfig config = properties.getProvider(providerName);
            if (config == null) {
                continue;
            }

            ProviderInfo info = ProviderInfo.builder()
                    .name(providerName)
                    .displayName(client.getDisplayName())
                    .models(config.getModels() != null ? new ArrayList<>(config.getModels()) : new ArrayList<>())
                    .defaultModel(config.getEffectiveDefaultModel())
                    .available(client.isAvailable())
                    .build();

            result.put(providerName, info);
        }

        return result;
    }

    @Override
    public boolean isValidProviderAndModel(String provider, String model) {
        if (provider == null || provider.isBlank()) {
            return false;
        }

        LlmClient client = clients.get(provider.toLowerCase());
        if (client == null || !client.isAvailable()) {
            return false;
        }

        // 如果未指定模型，使用默认模型，视为有效
        if (model == null || model.isBlank()) {
            return true;
        }

        // 检查模型是否在可用列表中
        return properties.isValidProviderAndModel(provider, model);
    }

    @Override
    public String getDefaultProvider() {
        return properties.getDefaultProvider();
    }

    @Override
    public String getDefaultModel(String provider) {
        if (provider == null || provider.isBlank()) {
            provider = getDefaultProvider();
        }

        AiProviderProperties.ProviderConfig config = properties.getProvider(provider);
        if (config == null) {
            return null;
        }

        return config.getEffectiveDefaultModel();
    }

    @Override
    public String getEffectiveProvider(String provider) {
        // 如果指定了供应商且可用，使用指定的
        if (provider != null && !provider.isBlank()) {
            String normalized = provider.toLowerCase();
            LlmClient client = clients.get(normalized);
            if (client != null && client.isAvailable()) {
                return normalized;
            }
            log.warn("Requested provider '{}' not available, falling back to default", provider);
        }

        // 使用默认供应商
        String defaultProvider = properties.getDefaultProvider();
        LlmClient defaultClient = clients.get(defaultProvider);
        
        if (defaultClient != null && defaultClient.isAvailable()) {
            return defaultProvider;
        }

        // 如果默认供应商也不可用，尝试找一个可用的
        for (Map.Entry<String, LlmClient> entry : clients.entrySet()) {
            if (entry.getValue().isAvailable()) {
                log.warn("Default provider '{}' not available, using '{}'", 
                        defaultProvider, entry.getKey());
                return entry.getKey();
            }
        }

        // 没有可用的供应商
        log.error("No available provider found");
        return defaultProvider;
    }

    @Override
    public String getEffectiveModel(String provider, String model) {
        AiProviderProperties.ProviderConfig config = properties.getProvider(provider);
        if (config == null) {
            return model;
        }

        // 如果指定了模型且可用，使用指定的
        if (model != null && !model.isBlank() && config.isModelAvailable(model)) {
            return model;
        }

        // 使用默认模型
        return config.getEffectiveDefaultModel();
    }

    /**
     * 构建聊天选项
     */
    private ChatOptions buildChatOptions() {
        AiProviderProperties.ChatConfig chatConfig = properties.getChat();
        return ChatOptions.builder()
                .temperature(chatConfig.getTemperature())
                .maxTokens(chatConfig.getMaxTokens())
                .systemPrompt(chatConfig.getSystemPrompt())
                .responseTimeout(chatConfig.getResponseTimeout())
                .build();
    }
}
