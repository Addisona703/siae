package com.hngy.siae.messaging.support.headers;

import com.hngy.siae.messaging.producer.MessageSendContext;

/**
 * 提供在发送前统一补全消息头的能力。
 */
public interface MessageHeaderEnricher {

    void enrich(MessageSendContext context);
}
