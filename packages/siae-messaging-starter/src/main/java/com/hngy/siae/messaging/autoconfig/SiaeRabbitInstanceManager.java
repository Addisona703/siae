package com.hngy.siae.messaging.autoconfig;

import com.hngy.siae.messaging.producer.MessageSendInterceptor;
import com.hngy.siae.messaging.producer.SiaeMessagingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

/**
 * 管理多套 RabbitMQ 实例的连接、模板与拓扑声明。
 */
public class SiaeRabbitInstanceManager {

    private static final Logger log = LoggerFactory.getLogger(SiaeRabbitInstanceManager.class);

    private final Map<String, InstanceContext> instances;
    private final List<MessageSendInterceptor> interceptors;
    private final MessageConverter messageConverter;

    public SiaeRabbitInstanceManager(SiaeRabbitProperties properties,
                                     List<MessageSendInterceptor> interceptors,
                                     MessageConverter messageConverter) {
        this.interceptors = interceptors == null ? List.of() : List.copyOf(interceptors);
        this.messageConverter = messageConverter;
        if (properties.getInstances().isEmpty()) {
            this.instances = Collections.emptyMap();
            return;
        }

        Map<String, InstanceContext> contexts = new LinkedHashMap<>();
        properties.getInstances().forEach((name, instance) -> {
            InstanceContext context = createInstanceContext(name, instance);
            contexts.put(name, context);
        });
        this.instances = Collections.unmodifiableMap(contexts);
    }

    public InstanceContext getInstance(String name) {
        return instances.get(name);
    }

    public Map<String, InstanceContext> getInstances() {
        return instances;
    }

    private InstanceContext createInstanceContext(String name, SiaeRabbitProperties.Instance instance) {
        CachingConnectionFactory connectionFactory = buildConnectionFactory(name, instance);
        RabbitTemplate rabbitTemplate = buildRabbitTemplate(name, connectionFactory, instance);
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);

        Declarables declarables = RabbitTopologyUtils.buildDeclarables(
                instance.getExchanges(),
                instance.getQueues(),
                instance.getBindings()
        );
        RabbitTopologyUtils.declareTopology(rabbitAdmin, declarables);

        log.info("[SIAE-MQ] Instance '{}' initialized: addresses={}, vhost={}, confirms={}, queues={}, exchanges={}",
                name,
                instance.getConnection().getAddresses(),
                instance.getConnection().getVirtualHost(),
                instance.getPublisher().isConfirms(),
                instance.getQueues().size(),
                instance.getExchanges().size());

        SiaeMessagingTemplate messagingTemplate = new SiaeMessagingTemplate(
                rabbitTemplate,
                instance.getPublisher(),
                interceptors
        );

        return new InstanceContext(connectionFactory, rabbitTemplate, messagingTemplate, rabbitAdmin, declarables);
    }

    private CachingConnectionFactory buildConnectionFactory(String name, SiaeRabbitProperties.Instance instance) {
        SiaeRabbitProperties.Connection connection = instance.getConnection();
        SiaeRabbitProperties.Publisher publisher = instance.getPublisher();
        SiaeRabbitProperties.Consumer consumer = instance.getConsumer();

        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setAddresses(connection.getAddresses());
        factory.setUsername(connection.getUsername());
        factory.setPassword(connection.getPassword());
        factory.setVirtualHost(connection.getVirtualHost());
        factory.setRequestedHeartBeat(connection.getRequestedHeartbeat());
        factory.setConnectionNameStrategy(cf -> "siae-messaging:" + name);
        factory.setChannelCacheSize(Math.max(1, consumer.getMaxConcurrency()));
        factory.setPublisherConfirmType(
                publisher.isConfirms()
                        ? CachingConnectionFactory.ConfirmType.CORRELATED
                        : CachingConnectionFactory.ConfirmType.NONE
        );
        factory.setPublisherReturns(publisher.isReturns());
        if (connection.isSslEnabled()) {
            try {
                factory.getRabbitConnectionFactory().useSslProtocol();
            } catch (NoSuchAlgorithmException | KeyManagementException ex) {
                throw new IllegalStateException("Failed to enable SSL for RabbitMQ instance " + name, ex);
            }
        }
        return factory;
    }

    private RabbitTemplate buildRabbitTemplate(String name,
                                               CachingConnectionFactory factory,
                                               SiaeRabbitProperties.Instance instance) {
        RabbitTemplate template = new RabbitTemplate(factory);
        RabbitTemplateConfigurer.configureTemplate(template, instance.getPublisher(), name);
        if (messageConverter != null) {
            template.setMessageConverter(messageConverter);
        }
        return template;
    }

    public static final class InstanceContext {

        private final CachingConnectionFactory connectionFactory;
        private final RabbitTemplate rabbitTemplate;
        private final SiaeMessagingTemplate messagingTemplate;
        private final RabbitAdmin rabbitAdmin;
        private final Declarables declarables;

        InstanceContext(CachingConnectionFactory connectionFactory,
                        RabbitTemplate rabbitTemplate,
                        SiaeMessagingTemplate messagingTemplate,
                        RabbitAdmin rabbitAdmin,
                        Declarables declarables) {
            this.connectionFactory = connectionFactory;
            this.rabbitTemplate = rabbitTemplate;
            this.messagingTemplate = messagingTemplate;
            this.rabbitAdmin = rabbitAdmin;
            this.declarables = declarables;
        }

        public CachingConnectionFactory getConnectionFactory() {
            return connectionFactory;
        }

        public RabbitTemplate getRabbitTemplate() {
            return rabbitTemplate;
        }

        public SiaeMessagingTemplate getMessagingTemplate() {
            return messagingTemplate;
        }

        public RabbitAdmin getRabbitAdmin() {
            return rabbitAdmin;
        }

        public Declarables getDeclarables() {
            return declarables;
        }
    }

    public SiaeMessagingTemplate getMessagingTemplate(String name) {
        InstanceContext context = instances.get(name);
        return context != null ? context.getMessagingTemplate() : null;
    }
}
