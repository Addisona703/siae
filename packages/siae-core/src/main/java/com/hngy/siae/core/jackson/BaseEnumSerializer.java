package com.hngy.siae.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hngy.siae.core.enums.BaseEnum;

import java.io.IOException;

/**
 * 通用枚举序列化器
 * 统一使用 code（数字）序列化，保持前后端一致性
 * 
 * @author KEYKB
 */
public class BaseEnumSerializer<T extends Enum<T> & BaseEnum> extends JsonSerializer<T> {

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        // 统一使用 code 序列化
        gen.writeNumber(value.getCode());
    }
}
