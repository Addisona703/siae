package com.hngy.siae.messaging.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.messaging.consumer.SiaeRabbitListenerErrorHandler;
import com.hngy.siae.messaging.producer.MessageSendInterceptor;
import com.hngy.siae.messaging.producer.SiaeMessagingTemplate;
import com.hngy.siae.messaging.support.headers.DefaultMessageHeaderEnricher;
import com.hngy.siae.messaging.support.headers.MessageHeaderEnricher;
import com.hngy.siae.messaging.support.health.SiaeRabbitHealthIndicator;
import com.hngy.siae.messaging.support.interceptor.HeaderEnrichingInterceptor;
import com.hngy.siae.messaging.support.interceptor.MessagingMetricsInterceptor;
import com.hngy.siae.messaging.support.logging.RabbitStartupLogger;
import com.hngy.siae.messaging.support.refresh.SiaeRabbitConnectionRefresher;
import com.hngy.siae.messaging.support.refresh.SiaeRabbitEnvironmentChangeListener;
import com.hngy.siae.messaging.support.serializer.JacksonMessageConverterAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.CacheMode;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@AutoConfiguration
@ConditionalOnClass({RabbitTemplate.class, CachingConnectionFactory.class})
@ConditionalOnProperty(prefix = "siae.messaging.rabbit", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SiaeRabbitProperties.class)
@EnableRabbit
public class SiaeRabbitAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SiaeRabbitAutoConfiguration.class);

    private final SiaeRabbitProperties properties;

    public SiaeRabbitAutoConfiguration(SiaeRabbitProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter siaeMessageConverter(ObjectMapper objectMapper) {
        return new JacksonMessageConverterAdapter(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @Primary
    public CachingConnectionFactory siaeRabbitConnectionFactory() {
        SiaeRabbitProperties.Connection connection = properties.getConnection();
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setCacheMode(CacheMode.CHANNEL);
        factory.setChannelCacheSize(Math.max(1, properties.getConsumer().getMaxConcurrency()));
        factory.setPublisherConfirmType(
                properties.getPublisher().isConfirms()
                        ? CachingConnectionFactory.ConfirmType.CORRELATED
                        : CachingConnectionFactory.ConfirmType.NONE
        );
        factory.setPublisherReturns(properties.getPublisher().isReturns());
        factory.setRequestedHeartBeat(connection.getRequestedHeartbeat());
        factory.setAddresses(connection.getAddresses());
        factory.setUsername(connection.getUsername());
        factory.setPassword(connection.getPassword());
        factory.setVirtualHost(connection.getVirtualHost());
        factory.setConnectionNameStrategy(cf -> "siae-messaging");
        if (connection.isSslEnabled()) {
            try {
                factory.getRabbitConnectionFactory().useSslProtocol();
            } catch (NoSuchAlgorithmException | KeyManagementException ex) {
                throw new IllegalStateException("Failed to enable SSL for RabbitMQ connection", ex);
            }
        }

        log.info("[SIAE-MQ] RabbitMQ connection initialized: addresses={}, vhost={}, confirms={}, returns={}",
                connection.getAddresses(),
                connection.getVirtualHost(),
                properties.getPublisher().isConfirms(),
                properties.getPublisher().isReturns());
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitTemplate siaeRabbitTemplate(CachingConnectionFactory connectionFactory,
                                             MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setChannelTransacted(false);
        if (properties.getPublisher().isConfirms()) {
            connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        } else {
            connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.NONE);
        }
        template.setMessageConverter(messageConverter);
        RabbitTemplateConfigurer.configureTemplate(template, properties.getPublisher(), "default");
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitAdmin siaeRabbitAdmin(CachingConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        rabbitAdmin.setIgnoreDeclarationExceptions(true);
        return rabbitAdmin;
    }

    @Bean(name = "siaeRabbitDeclarables")
    @ConditionalOnMissingBean(name = "siaeRabbitDeclarables")
    public Declarables siaeRabbitDeclarables() {
        return RabbitTopologyUtils.buildDeclarables(
                properties.getExchanges(),
                properties.getQueues(),
                properties.getBindings()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public SimpleRabbitListenerContainerFactory siaeListenerContainerFactory(CachingConnectionFactory connectionFactory,
                                                                             MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        SiaeRabbitProperties.Consumer consumer = properties.getConsumer();
        int concurrentConsumers = Math.max(1, consumer.getConcurrency());
        int maxConcurrentConsumers = Math.max(concurrentConsumers, consumer.getMaxConcurrency());
        factory.setConcurrentConsumers(concurrentConsumers);
        factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
        factory.setPrefetchCount(Math.max(1, consumer.getPrefetch()));
        factory.setAcknowledgeMode(consumer.getAcknowledgeMode());
        factory.setMissingQueuesFatal(false);
        factory.setDefaultRequeueRejected(false);

        SiaeRabbitProperties.Consumer.Retry retryProps = consumer.getRetry();
        if (retryProps.isEnabled() && retryProps.getMaxAttempts() > 1) {
            MessageRecoverer recoverer = (message, cause) -> {
                log.error("[SIAE-MQ] Message exhausted retries -> DLQ (queue={}, messageId={})",
                        message.getMessageProperties().getConsumerQueue(),
                        message.getMessageProperties().getMessageId(),
                        cause);
                throw new AmqpRejectAndDontRequeueException("Message retries exhausted", cause);
            };
            RetryOperationsInterceptor interceptor = RetryInterceptorBuilder.stateless()
                    .maxAttempts(Math.max(1, retryProps.getMaxAttempts()))
                    .backOffOptions(
                            Math.max(1L, retryProps.getInitialInterval()),
                            Math.max(1.0d, retryProps.getMultiplier()),
                            Math.max(retryProps.getInitialInterval(), retryProps.getMaxInterval()))
                    .recoverer(recoverer)
                    .build();
            factory.setAdviceChain(interceptor);
        }

        log.info("[SIAE-MQ] Listener container configured: concurrency={}~{}, prefetch={}, ackMode={}",
                consumer.getConcurrency(),
                consumer.getMaxConcurrency(),
                consumer.getPrefetch(),
                consumer.getAcknowledgeMode());
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public SiaeMessagingTemplate siaeMessagingTemplate(RabbitTemplate rabbitTemplate,
                                                       ObjectProvider<MessageSendInterceptor> interceptorProvider) {
        List<MessageSendInterceptor> interceptors = interceptorProvider.orderedStream().collect(Collectors.toList());
        return new SiaeMessagingTemplate(rabbitTemplate, properties.getPublisher(), interceptors);
    }

    @Bean
    public SiaeRabbitInstanceManager siaeRabbitInstanceManager(ObjectProvider<MessageSendInterceptor> interceptorProvider,
                                                               MessageConverter messageConverter) {
        List<MessageSendInterceptor> interceptors = interceptorProvider.orderedStream().collect(Collectors.toList());
        return new SiaeRabbitInstanceManager(properties, interceptors, messageConverter);
    }

    @Bean(name = "siaeRabbitListenerErrorHandler")
    @ConditionalOnMissingBean(name = "siaeRabbitListenerErrorHandler")
    public SiaeRabbitListenerErrorHandler siaeRabbitListenerErrorHandler() {
        return new SiaeRabbitListenerErrorHandler();
    }

    @Bean
    @ConditionalOnMissingBean(MessageHeaderEnricher.class)
    public MessageHeaderEnricher messageHeaderEnricher() {
        return new DefaultMessageHeaderEnricher();
    }

    @Bean
    @ConditionalOnMissingBean(HeaderEnrichingInterceptor.class)
    public HeaderEnrichingInterceptor headerEnrichingInterceptor(MessageHeaderEnricher enricher) {
        return new HeaderEnrichingInterceptor(enricher);
    }

    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnMissingBean(MessagingMetricsInterceptor.class)
    public MessagingMetricsInterceptor messagingMetricsInterceptor(ObjectProvider<MeterRegistry> registryProvider) {
        MeterRegistry registry = registryProvider.getIfAvailable();
        return new MessagingMetricsInterceptor(registry);
    }

    @Bean
    public SiaeRabbitConnectionRefresher siaeRabbitConnectionRefresher(CachingConnectionFactory connectionFactory) {
        return new SiaeRabbitConnectionRefresher(connectionFactory, properties);
    }

    @Bean
    @ConditionalOnClass(EnvironmentChangeEvent.class)
    public SiaeRabbitEnvironmentChangeListener siaeRabbitEnvironmentChangeListener(Environment environment,
                                                                                   SiaeRabbitConnectionRefresher refresher) {
        return new SiaeRabbitEnvironmentChangeListener(environment, refresher);
    }

    @Bean(name = "siaeRabbitHealthIndicator")
    @ConditionalOnClass(HealthIndicator.class)
    @ConditionalOnMissingBean(name = "siaeRabbitHealthIndicator")
    public HealthIndicator siaeRabbitHealthIndicator(CachingConnectionFactory connectionFactory) {
        return new SiaeRabbitHealthIndicator(connectionFactory);
    }

    @Bean
    public RabbitStartupLogger rabbitStartupLogger(CachingConnectionFactory connectionFactory,
                                                   SiaeRabbitInstanceManager instanceManager) {
        return new RabbitStartupLogger(connectionFactory, instanceManager);
    }
}
