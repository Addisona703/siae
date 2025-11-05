package com.hngy.siae.messaging.support.logging;

import com.hngy.siae.messaging.autoconfig.SiaeRabbitInstanceManager;
import com.hngy.siae.messaging.autoconfig.SiaeRabbitInstanceManager.InstanceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;

import java.util.Map;

public class RabbitStartupLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(RabbitStartupLogger.class);

    private final CachingConnectionFactory connectionFactory;
    private final SiaeRabbitInstanceManager instanceManager;

    public RabbitStartupLogger(CachingConnectionFactory connectionFactory,
                               SiaeRabbitInstanceManager instanceManager) {
        this.connectionFactory = connectionFactory;
        this.instanceManager = instanceManager;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("[SIAE-MQ] ✅ Connected to {} (vhost={}, publisherConfirms={}, publisherReturns={})",
                formatAddress(connectionFactory),
                connectionFactory.getVirtualHost(),
                connectionFactory.isPublisherConfirms(),
                connectionFactory.isPublisherReturns());

        Map<String, InstanceContext> instances = instanceManager.getInstances();
        if (CollectionUtils.isEmpty(instances)) {
            log.info("[SIAE-MQ] Default connection active; no additional RabbitMQ instances configured.");
            return;
        }

        instances.forEach((name, context) -> log.info(
                "[SIAE-MQ] Instance '{}' connected -> addresses={}, queues={}, exchanges={}",
                name,
                formatAddress(context.getConnectionFactory()),
                context.getDeclarables().getDeclarables().stream()
                        .filter(d -> d instanceof org.springframework.amqp.core.Queue).count(),
                context.getDeclarables().getDeclarables().stream()
                        .filter(d -> d instanceof org.springframework.amqp.core.Exchange).count()
        ));
    }

    private String formatAddress(CachingConnectionFactory factory) {
        return factory.getHost() + ":" + factory.getPort();
    }
}
