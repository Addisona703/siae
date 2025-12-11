package com.hngy.siae.ai.config;

import com.hngy.siae.ai.tool.AwardQueryTool;
import com.hngy.siae.ai.tool.ContentQueryTool;
import com.hngy.siae.ai.tool.MemberQueryTool;
import com.hngy.siae.ai.tool.WeatherTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * Spring AI 配置
 * <p>
 * Requirements: 1.1, 1.3, 1.4
 *
 * @author SIAE Team
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    private final AiProperties aiProperties;

    /**
     * 创建重试模板
     */
    @Bean
    @ConditionalOnMissingBean(name = "aiRetryTemplate")
    public RetryTemplate aiRetryTemplate() {
        AiProperties.Retry retryConfig = aiProperties.getRetry();
        
        log.info("Creating AI RetryTemplate with maxAttempts: {}, initialInterval: {}ms", 
                retryConfig.getMaxAttempts(), 
                retryConfig.getInitialInterval());
        
        return RetryTemplate.builder()
                .maxAttempts(retryConfig.getMaxAttempts())
                .exponentialBackoff(
                        retryConfig.getInitialInterval(),
                        retryConfig.getMultiplier(),
                        retryConfig.getMaxInterval()
                )
                .retryOn(java.net.SocketTimeoutException.class)
                .retryOn(java.net.ConnectException.class)
                .retryOn(java.io.IOException.class)
                .build();
    }

    /**
     * 创建 RestTemplate 用于外部 API 调用
     */
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 创建 ChatClient，配置默认工具
     */
    @Bean
    @ConditionalOnMissingBean
    public ChatClient chatClient(OllamaChatModel chatModel, 
                                AwardQueryTool awardQueryTool, 
                                MemberQueryTool memberQueryTool,
                                ContentQueryTool contentQueryTool,
                                WeatherTool weatherTool) {
        log.info("Creating ChatClient with tools: award, member, content, weather");
        
        return ChatClient.builder(chatModel)
                .defaultTools(awardQueryTool, memberQueryTool, contentQueryTool, weatherTool)
                .build();
    }
}
