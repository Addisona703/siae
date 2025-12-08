package com.hngy.siae.media.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置类
 * 
 * 配置 Spring Cache 使用 Redis 作为缓存存储
 * 定义不同类型文件的缓存策略：
 * - 公开文件 URL 缓存 7 天
 * - 私有文件 URL 缓存 23 小时
 * 
 * Requirements: 4.5
 * 
 * @author SIAE Team
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    private final MediaProperties mediaProperties;

    /**
     * 缓存名称常量
     */
    public static final String PUBLIC_FILE_URL_CACHE = "publicFileUrl";
    public static final String PRIVATE_FILE_URL_CACHE = "privateFileUrl";
    public static final String FILE_METADATA_CACHE = "fileMetadata";

    /**
     * 配置 Redis 缓存管理器
     * 
     * @param connectionFactory Redis 连接工厂
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))  // 默认 1 小时
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .disableCachingNullValues();

        // 针对不同缓存的特定配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 公开文件 URL 缓存：7 天
        if (mediaProperties.getUrl().getCacheEnabled()) {
            cacheConfigurations.put(
                    PUBLIC_FILE_URL_CACHE,
                    defaultConfig.entryTtl(Duration.ofSeconds(mediaProperties.getUrl().getPublicTtl()))
            );

            // 私有文件 URL 缓存：23 小时
            cacheConfigurations.put(
                    PRIVATE_FILE_URL_CACHE,
                    defaultConfig.entryTtl(Duration.ofSeconds(mediaProperties.getUrl().getPrivateTtl()))
            );
        }

        // 文件元数据缓存：1 小时
        cacheConfigurations.put(
                FILE_METADATA_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(1))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
