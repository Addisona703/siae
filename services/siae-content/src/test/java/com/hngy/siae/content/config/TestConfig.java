package com.hngy.siae.content.config;

import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.user.client.UserFeignClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 测试配置 - 模拟外部依赖
 *
 * @author Kiro
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);
        
        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        
        return template;
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        return Mockito.mock(RedisTemplate.class);
    }

    @Bean
    @Primary
    public MediaFeignClient mediaFeignClient() {
        return Mockito.mock(MediaFeignClient.class);
    }

    @Bean
    @Primary
    public UserFeignClient userFeignClient() {
        return Mockito.mock(UserFeignClient.class);
    }
}
