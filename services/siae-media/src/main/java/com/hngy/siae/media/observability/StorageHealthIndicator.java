package com.hngy.siae.media.observability;

import com.hngy.siae.media.infrastructure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 存储服务健康检查
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StorageHealthIndicator implements HealthIndicator {

    private final StorageService storageService;

    @Override
    public Health health() {
        try {
            // 检查存储服务是否可用
            boolean available = storageService.isAvailable();
            
            if (available) {
                return Health.up()
                        .withDetail("storage", "MinIO storage is available")
                        .build();
            } else {
                return Health.down()
                        .withDetail("storage", "MinIO storage is not available")
                        .build();
            }
        } catch (Exception e) {
            log.error("Storage health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
