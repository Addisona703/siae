package com.hngy.siae.media.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 事件幂等性检查
 *
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventIdempotency {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "event:processed:";
    private static final Duration TTL = Duration.ofHours(24);

    /**
     * 检查事件是否已处理
     */
    public boolean isProcessed(String eventId) {
        String key = KEY_PREFIX + eventId;
        return redisTemplate.hasKey(key);
    }

    /**
     * 标记事件为已处理
     */
    public void markAsProcessed(String eventId) {
        String key = KEY_PREFIX + eventId;
        redisTemplate.opsForValue().set(key, "1", TTL);
        log.debug("Marked event as processed: {}", eventId);
    }

    /**
     * 尝试处理事件（原子操作）
     * @return true 如果可以处理，false 如果已处理过
     */
    public boolean tryProcess(String eventId) {
        String key = KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", TTL);
        return Boolean.TRUE.equals(success);
    }

}
