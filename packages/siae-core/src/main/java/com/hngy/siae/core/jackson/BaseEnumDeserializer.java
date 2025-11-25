package com.hngy.siae.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.hngy.siae.core.enums.BaseEnum;

import java.io.IOException;

/**
 * 通用枚举反序列化器
 * 支持 code 和 description 两种反序列化方式
 * 
 * @author KEYKB
 */
public class BaseEnumDeserializer<T extends Enum<T> & BaseEnum> extends JsonDeserializer<T> {

    private final Class<T> enumClass;

    public BaseEnumDeserializer(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();

        // 处理空字符串或null，返回null
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // 尝试先按 code 查找
        try {
            int code = Integer.parseInt(value);
            T result = BaseEnum.fromCode(enumClass, code);
            if (result != null) {
                return result;
            }
        } catch (NumberFormatException ignored) {
        }

        // 再尝试按 description 查找
        T result = BaseEnum.fromDesc(enumClass, value);
        if (result != null) {
            return result;
        }

        throw new IllegalArgumentException(
            "无法解析枚举值: '" + value + "' for enum class: " + enumClass.getName()
        );
    }
}
