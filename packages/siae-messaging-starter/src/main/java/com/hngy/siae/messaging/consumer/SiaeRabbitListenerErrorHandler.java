package com.hngy.siae.messaging.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.StringUtils;

public class SiaeRabbitListenerErrorHandler implements RabbitListenerErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(SiaeRabbitListenerErrorHandler.class);

    @Override
    public Object handleError(Message amqpMessage,
                              org.springframework.messaging.Message<?> message,
                              ListenerExecutionFailedException exception) {
        String queue = amqpMessage.getMessageProperties().getConsumerQueue();
        String messageId = amqpMessage.getMessageProperties().getMessageId();
        MessageHeaders headers = message != null ? message.getHeaders() : null;
        String traceId = headers != null && headers.containsKey("traceId")
                ? String.valueOf(headers.get("traceId"))
                : null;

        log.error("[SIAE-MQ] Listener error -> DLQ (queue={}, messageId={}, traceId={})",
                queue,
                StringUtils.hasText(messageId) ? messageId : "unknown",
                StringUtils.hasText(traceId) ? traceId : "N/A",
                exception);

        throw new AmqpRejectAndDontRequeueException("Listener processing failed", exception);
    }
}
