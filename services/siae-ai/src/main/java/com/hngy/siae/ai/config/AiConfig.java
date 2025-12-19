package com.hngy.siae.ai.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * AI 服务配置
 * <p>
 * 提供 WebClient 和其他基础设施 Bean。
 * LlmClient 和 ProviderManager 的配置在 {@link LlmClientConfig} 中。
 * <p>
 * Requirements: 1.1
 *
 * @author SIAE Team
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AiProviderProperties.class)
public class AiConfig {

    private final AiProviderProperties aiProviderProperties;

    /**
     * 创建 WebClient.Builder Bean
     * <p>
     * 配置超时、缓冲区大小等参数，供 LLM 客户端使用。
     */
    @Bean
    @ConditionalOnMissingBean
    public WebClient.Builder webClientBuilder() {
        int timeout = aiProviderProperties.getChat().getResponseTimeout();
        
        log.info("Creating WebClient.Builder with timeout: {}s", timeout);

        // 配置 Netty HttpClient
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout * 1000)
                .responseTimeout(Duration.ofSeconds(timeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.SECONDS)));

        // 配置 Exchange Strategies，增加缓冲区大小以支持大响应
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)) // 16MB
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies);
    }
}
