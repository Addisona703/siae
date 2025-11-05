package com.hngy.siae.messaging.support.refresh;

import com.hngy.siae.messaging.autoconfig.SiaeRabbitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * 负责在配置刷新时更新连接参数并重建连接。
 */
public class SiaeRabbitConnectionRefresher {

    private static final Logger log = LoggerFactory.getLogger(SiaeRabbitConnectionRefresher.class);

    private final CachingConnectionFactory connectionFactory;
    private final SiaeRabbitProperties properties;

    public SiaeRabbitConnectionRefresher(CachingConnectionFactory connectionFactory,
                                         SiaeRabbitProperties properties) {
        this.connectionFactory = connectionFactory;
        this.properties = properties;
    }

    public synchronized void refreshIfNecessary(SiaeRabbitProperties.Connection updatedConnection) {
        SiaeRabbitProperties.Connection current = properties.getConnection();
        if (!hasConnectionChanged(current, updatedConnection)) {
            return;
        }

        applyConnection(updatedConnection);
        properties.getConnection().setAddresses(updatedConnection.getAddresses());
        properties.getConnection().setUsername(updatedConnection.getUsername());
        properties.getConnection().setPassword(updatedConnection.getPassword());
        properties.getConnection().setVirtualHost(updatedConnection.getVirtualHost());
        properties.getConnection().setRequestedHeartbeat(updatedConnection.getRequestedHeartbeat());
        properties.getConnection().setSslEnabled(updatedConnection.isSslEnabled());

        connectionFactory.resetConnection();

        log.info("[SIAE-MQ] 🔄 RabbitMQ connection parameters refreshed -> {} (vhost={})",
                connectionFactory.getHost() + ":" + connectionFactory.getPort(),
                connectionFactory.getVirtualHost());
    }

    private boolean hasConnectionChanged(SiaeRabbitProperties.Connection original,
                                         SiaeRabbitProperties.Connection updated) {
        return !Objects.equals(original.getAddresses(), updated.getAddresses())
                || !Objects.equals(original.getUsername(), updated.getUsername())
                || !Objects.equals(original.getPassword(), updated.getPassword())
                || !Objects.equals(original.getVirtualHost(), updated.getVirtualHost())
                || original.getRequestedHeartbeat() != updated.getRequestedHeartbeat()
                || original.isSslEnabled() != updated.isSslEnabled();
    }

    private void applyConnection(SiaeRabbitProperties.Connection connection) {
        connectionFactory.setAddresses(connection.getAddresses());
        connectionFactory.setUsername(connection.getUsername());
        connectionFactory.setPassword(connection.getPassword());
        connectionFactory.setVirtualHost(connection.getVirtualHost());
        connectionFactory.setRequestedHeartBeat(connection.getRequestedHeartbeat());
        if (connection.isSslEnabled()) {
            try {
                connectionFactory.getRabbitConnectionFactory().useSslProtocol();
            } catch (NoSuchAlgorithmException | KeyManagementException ex) {
                throw new IllegalStateException("Failed to enable SSL during refresh", ex);
            }
        }
    }
}
