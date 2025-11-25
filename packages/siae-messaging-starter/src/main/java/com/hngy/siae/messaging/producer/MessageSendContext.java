package com.hngy.siae.messaging.producer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MessageSendContext {

    private final String exchange;
    private final String routingKey;
    private final Object payload;
    private final Map<String, Object> headers;
    private int attempt;
    private String messageId;
    private Message message;
    private CorrelationData correlationData;

    public MessageSendContext(String exchange,
                              String routingKey,
                              Object payload,
                              Map<String, Object> headers) {
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.payload = payload;
        this.headers = headers == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(headers);
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public Object getPayload() {
        return payload;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public Map<String, Object> getUnmodifiableHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Message getMessage() {
        return message;
    }

    void setMessage(Message message) {
        this.message = message;
    }

    public CorrelationData getCorrelationData() {
        return correlationData;
    }

    void setCorrelationData(CorrelationData correlationData) {
        this.correlationData = correlationData;
    }
}
