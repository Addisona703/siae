package com.hngy.siae.messaging.producer;

import com.hngy.siae.messaging.autoconfig.SiaeRabbitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SiaeMessagingTemplate {

    private static final Logger log = LoggerFactory.getLogger(SiaeMessagingTemplate.class);

    private final RabbitTemplate rabbitTemplate;
    private final List<MessageSendInterceptor> interceptors;
    private final RetryTemplate retryTemplate;
    private final int maxAttempts;

    public SiaeMessagingTemplate(RabbitTemplate rabbitTemplate,
                                 SiaeRabbitProperties.Publisher publisherProperties,
                                 List<MessageSendInterceptor> interceptors) {
        this.rabbitTemplate = Objects.requireNonNull(rabbitTemplate, "rabbitTemplate");
        this.interceptors = interceptors == null ? List.of() : List.copyOf(interceptors);
        this.retryTemplate = buildRetryTemplate(publisherProperties);
        this.maxAttempts = resolveMaxAttempts(publisherProperties, this.retryTemplate);
    }

    public void send(String exchange, String routingKey, Object payload) {
        send(exchange, routingKey, payload, Collections.emptyMap(), null);
    }

    public void send(String exchange,
                     String routingKey,
                     Object payload,
                     Map<String, Object> headers) {
        send(exchange, routingKey, payload, headers, null);
    }

    public void send(String exchange,
                     String routingKey,
                     Object payload,
                     Map<String, Object> headers,
                     MessagePostProcessor postProcessor) {
        executeSend(exchange, routingKey, payload, headers, postProcessor);
    }

    public CompletableFuture<Void> sendAsync(String exchange,
                                             String routingKey,
                                             Object payload,
                                             Map<String, Object> headers,
                                             MessagePostProcessor postProcessor) {
        return CompletableFuture.runAsync(() -> executeSend(exchange, routingKey, payload, headers, postProcessor));
    }

    public CompletableFuture<Void> sendAsync(String exchange,
                                             String routingKey,
                                             Object payload) {
        return sendAsync(exchange, routingKey, payload, Collections.emptyMap(), null);
    }

    public CompletableFuture<Void> sendAsync(String exchange,
                                             String routingKey,
                                             Object payload,
                                             Map<String, Object> headers) {
        return sendAsync(exchange, routingKey, payload, headers, null);
    }

    private void executeSend(String exchange,
                             String routingKey,
                             Object payload,
                             Map<String, Object> headers,
                             MessagePostProcessor postProcessor) {
        if (retryTemplate != null) {
            try {
                retryTemplate.execute(context -> {
                    int attempt = context.getRetryCount() + 1;
                    doSend(exchange, routingKey, payload, headers, postProcessor, attempt);
                    return null;
                }, context -> {
                    Throwable last = context.getLastThrowable();
                    throw last instanceof MessageSendException
                            ? (MessageSendException) last
                            : new MessageSendException(
                            String.format("Failed to send message after %d attempts (exchange=%s, routingKey=%s)",
                                    context.getRetryCount(),
                                    exchange,
                                    routingKey),
                            last);
                });
            } catch (MessageSendException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                log.error("[SIAE-MQ] Send failure (exchange={}, routingKey={})", exchange, routingKey, ex);
                throw new MessageSendException("Failed to send message", ex);
            }
        } else {
            try {
                doSend(exchange, routingKey, payload, headers, postProcessor, 1);
            } catch (MessageSendException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                log.error("[SIAE-MQ] Send failure (exchange={}, routingKey={})", exchange, routingKey, ex);
                throw new MessageSendException("Failed to send message", ex);
            }
        }
    }

    private void doSend(String exchange,
                        String routingKey,
                        Object payload,
                        Map<String, Object> headers,
                        MessagePostProcessor userPostProcessor,
                        int attempt) {
        Map<String, Object> headerMap = headers == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(headers);
        MessageSendContext context = new MessageSendContext(exchange, routingKey, payload, headerMap);
        context.setAttempt(attempt);

        String providedMessageId = resolveMessageId(headerMap);
        if (StringUtils.hasText(providedMessageId)) {
            context.setMessageId(providedMessageId);
        }
        if (!StringUtils.hasText(context.getMessageId())) {
            context.setMessageId(generateMessageId());
        }

        context.getHeaders().putIfAbsent("X-Retry-Count", attempt - 1);

        notifyBefore(context);

        CorrelationData correlationData = new CorrelationData(context.getMessageId());
        context.setCorrelationData(correlationData);

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload, message -> {
                message.getMessageProperties().setMessageId(context.getMessageId());
                if (!CollectionUtils.isEmpty(context.getHeaders())) {
                    context.getHeaders().forEach((key, value) -> {
                        if (value != null) {
                            message.getMessageProperties().setHeader(key, value);
                        }
                    });
                }
                if (userPostProcessor != null) {
                    Message processed = userPostProcessor.postProcessMessage(message);
                    context.setMessage(processed);
                    return processed;
                }
                context.setMessage(message);
                return message;
            }, correlationData);

            notifyAfter(context);
        } catch (RuntimeException ex) {
            notifyError(context, ex);
            throw ex;
        }
    }

    private RetryTemplate buildRetryTemplate(SiaeRabbitProperties.Publisher publisherProperties) {
        if (publisherProperties == null
                || publisherProperties.getRetry() == null
                || !publisherProperties.getRetry().isEnabled()) {
            return null;
        }
        int maxAttempts = Math.max(1, publisherProperties.getRetry().getMaxAttempts());
        if (maxAttempts <= 1) {
            return null;
        }
        SiaeRabbitProperties.Publisher.Retry retry = publisherProperties.getRetry();
        RetryTemplateBuilder builder = RetryTemplate.builder()
                .maxAttempts(maxAttempts);
        builder.exponentialBackoff(
                retry.getInitialInterval(),
                retry.getMultiplier(),
                retry.getMaxInterval()
        );
        return builder.build();
    }

    private int resolveMaxAttempts(SiaeRabbitProperties.Publisher publisherProperties, RetryTemplate template) {
        if (publisherProperties == null || publisherProperties.getRetry() == null) {
            return 1;
        }
        int configured = Math.max(1, publisherProperties.getRetry().getMaxAttempts());
        if (template == null) {
            return 1;
        }
        return configured;
    }

    private void notifyBefore(MessageSendContext context) {
        for (MessageSendInterceptor interceptor : interceptors) {
            try {
                interceptor.beforeSend(context);
            } catch (RuntimeException ex) {
                log.warn("MessageSendInterceptor.beforeSend error: {}", ex.getMessage(), ex);
            }
        }
    }

    private void notifyAfter(MessageSendContext context) {
        for (MessageSendInterceptor interceptor : interceptors) {
            try {
                interceptor.afterSend(context);
            } catch (RuntimeException ex) {
                log.warn("MessageSendInterceptor.afterSend error: {}", ex.getMessage(), ex);
            }
        }
    }

    private void notifyError(MessageSendContext context, Throwable throwable) {
        String level = context.getAttempt() >= maxAttempts ? "error" : "warn";
        if ("error".equals(level)) {
            log.error("[SIAE-MQ] Send attempt {}/{} failed (exchange={}, routingKey={}, messageId={})",
                    context.getAttempt(),
                    maxAttempts,
                    context.getExchange(),
                    context.getRoutingKey(),
                    context.getMessageId(),
                    throwable);
        } else {
            log.warn("[SIAE-MQ] Send attempt {}/{} failed (exchange={}, routingKey={}, messageId={})",
                    context.getAttempt(),
                    maxAttempts,
                    context.getExchange(),
                    context.getRoutingKey(),
                    context.getMessageId(),
                    throwable);
        }
        for (MessageSendInterceptor interceptor : interceptors) {
            try {
                interceptor.onError(context, throwable);
            } catch (RuntimeException ex) {
                log.warn("MessageSendInterceptor.onError error: {}", ex.getMessage(), ex);
            }
        }
    }

    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }

    private String resolveMessageId(Map<String, Object> headers) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        Object candidate = headers.get("messageId");
        if (!(candidate instanceof String) || !StringUtils.hasText((String) candidate)) {
            candidate = headers.get("message-id");
        }
        return candidate instanceof String ? (String) candidate : null;
    }
}
