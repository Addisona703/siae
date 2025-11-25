package com.hngy.siae.attendance.config;

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
 * 测试配置 - 模拟 Redis
 *
 * @author SIAE Team
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);
        
        when(template.opsForValue()).thenReturn(valueOps);
        // 总是返回 true 来模拟成功获取锁
        when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        
        return template;
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        // 为 security-starter 提供 RedisTemplate
        return Mockito.mock(RedisTemplate.class);
    }
}
