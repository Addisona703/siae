package com.hngy.siae.content.common.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hngy.siae.content.common.enums.BaseEnum;

import java.io.IOException;

/**
 * 通用枚举序列化器：输出为
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
public class BaseEnumSerializer<T extends Enum<T> & BaseEnum> extends JsonSerializer<T> {

    private final Class<T> enumClass;

    public BaseEnumSerializer(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//        直接返回 "type": "note"
        gen.writeString(value.getDescription());

//        "type": {
//            "code": 0,
//            "desc": "article"
//        }
//        gen.writeStartObject();
//        gen.writeNumberField("code", value.getCode());
//        gen.writeStringField("desc", value.getDescription());
//        gen.writeEndObject();
    }
}
