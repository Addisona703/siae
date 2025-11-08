package com.hngy.siae.media.observability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 健康检查
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQHealthIndicator implements HealthIndicator {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public Health health() {
        try {
            // 尝试获取连接工厂信息
            var connectionFactory = rabbitTemplate.getConnectionFactory();
            var connection = connectionFactory.createConnection();
            
            if (connection.isOpen()) {
                connection.close();
                return Health.up()
                        .withDetail("rabbitmq", "RabbitMQ is available")
                        .build();
            } else {
                return Health.down()
                        .withDetail("rabbitmq", "RabbitMQ connection is closed")
                        .build();
            }
        } catch (Exception e) {
            log.error("RabbitMQ health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
