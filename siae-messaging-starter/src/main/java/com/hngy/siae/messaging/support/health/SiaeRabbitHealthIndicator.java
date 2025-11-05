package com.hngy.siae.messaging.support.health;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

public class SiaeRabbitHealthIndicator extends AbstractHealthIndicator {

    private final CachingConnectionFactory connectionFactory;

    public SiaeRabbitHealthIndicator(CachingConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try (Connection connection = connectionFactory.createConnection()) {
            builder.up()
                    .withDetail("address", connectionFactory.getHost() + ":" + connectionFactory.getPort())
                    .withDetail("vhost", connectionFactory.getVirtualHost())
                    .withDetail("publisherConfirms", connectionFactory.isPublisherConfirms())
                    .withDetail("publisherReturns", connectionFactory.isPublisherReturns());
        } catch (Exception ex) {
            builder.down(ex);
        }
    }
}
