package com.hngy.siae.messaging.support.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

/**
 * 使用 Jackson 的 JSON 消息转换器，并确保默认开启 MessageId 与类型推断。
 */
public class JacksonMessageConverterAdapter extends Jackson2JsonMessageConverter implements MessageConverterAdapter {

    public JacksonMessageConverterAdapter(ObjectMapper objectMapper) {
        super(objectMapper);
        setCreateMessageIds(true);
        setAlwaysConvertToInferredType(true);
    }
}
