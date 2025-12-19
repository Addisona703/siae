package com.hngy.siae.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.ai.client.LlmClient;
import com.hngy.siae.ai.client.ProviderManager;
import com.hngy.siae.ai.client.ZhipuAiClient;
import com.hngy.siae.ai.client.impl.ProviderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM 客户端配置
 * <p>
 * 配置多供应商 LLM 客户端和供应商管理器。
 * WebClient 基础配置在 {@link AiConfig} 中。
 * <p>
 * Requirements: 1.1, 3.1
 *
 * @author SIAE Team
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AiProviderProperties.class)
public class LlmClientConfig {

    private final AiProviderProperties aiProviderProperties;
    private final ObjectMapper objectMapper;

    /**
     * 创建智谱 AI 客户端
     * <p>
     * 使用 WebClient.Builder 创建客户端，继承全局超时和缓冲区配置。
     */
    @Bean
    @ConditionalOnProperty(prefix = "siae.ai.providers.zhipu", name = "api-key")
    public LlmClient zhipuAiClient(WebClient.Builder webClientBuilder) {
        AiProviderProperties.ProviderConfig config = aiProviderProperties.getProvider("zhipu");
        if (config == null || !config.isValid()) {
            log.warn("Zhipu AI configuration is invalid or missing, client will not be created");
            return null;
        }
        log.info("Creating ZhipuAiClient with base URL: {}", config.getBaseUrl());
        return new ZhipuAiClient(config, objectMapper, webClientBuilder);
    }

    /**
     * 创建供应商管理器
     * <p>
     * 收集所有可用的 LlmClient 并创建 ProviderManager。
     */
    @Bean
    @ConditionalOnMissingBean
    public ProviderManager providerManager(Map<String, LlmClient> llmClients) {
        // 过滤掉 null 客户端并按供应商名称重新映射
        Map<String, LlmClient> validClients = new HashMap<>();
        
        for (Map.Entry<String, LlmClient> entry : llmClients.entrySet()) {
            LlmClient client = entry.getValue();
            if (client != null && client.isAvailable()) {
                validClients.put(client.getProviderName(), client);
                log.info("Registered LLM client: {} ({})", 
                        client.getProviderName(), client.getDisplayName());
            }
        }

        if (validClients.isEmpty()) {
            log.warn("No valid LLM clients found, ProviderManager will have limited functionality");
        }

        return new ProviderManagerImpl(validClients, aiProviderProperties);
    }
}
