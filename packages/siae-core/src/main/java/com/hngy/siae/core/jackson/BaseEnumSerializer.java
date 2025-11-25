package com.hngy.siae.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hngy.siae.core.enums.BaseEnum;

import java.io.IOException;

/**
 * 通用枚举序列化器
 * 将枚举序列化为 description 字符串
 * 
 * @author KEYKB
 */
public class BaseEnumSerializer<T extends Enum<T> & BaseEnum> extends JsonSerializer<T> {

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        // 直接返回 description，如 "article"
        gen.writeString(value.getDescription());
        
        // 如果需要返回完整对象，可以使用以下代码：
        // gen.writeStartObject();
        // gen.writeNumberField("code", value.getCode());
        // gen.writeStringField("desc", value.getDescription());
        // gen.writeEndObject();
    }
}
