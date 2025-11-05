package com.hngy.siae.messaging.support.serializer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * 核心消息转换器适配器，便于后续扩展不同序列化方案。
 */
public interface MessageConverterAdapter extends MessageConverter {

    @Override
    Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException;

    @Override
    Object fromMessage(Message message) throws MessageConversionException;
}
