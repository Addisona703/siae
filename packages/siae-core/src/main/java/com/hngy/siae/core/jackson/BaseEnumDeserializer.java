package com.hngy.siae.core.jackson;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.hngy.siae.core.enums.BaseEnum;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 通用枚举反序列化器
 * 支持 code、枚举名称、description、@JsonValue 字段值 多种反序列化方式
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

        // 1. 尝试按 code 查找（数字）
        try {
            int code = Integer.parseInt(value);
            T result = BaseEnum.fromCode(enumClass, code);
            if (result != null) {
                return result;
            }
        } catch (NumberFormatException ignored) {
        }

        // 2. 尝试按枚举名称查找（大小写不敏感）
        try {
            T result = Enum.valueOf(enumClass, value.toUpperCase());
            if (result != null) {
                return result;
            }
        } catch (IllegalArgumentException ignored) {
        }

        // 3. 尝试按 @JsonValue 字段值查找
        T result = findByJsonValue(value);
        if (result != null) {
            return result;
        }

        // 4. 尝试按 description 查找
        result = BaseEnum.fromDesc(enumClass, value);
        if (result != null) {
            return result;
        }

        throw new IllegalArgumentException(
            "无法解析枚举值: '" + value + "' for enum class: " + enumClass.getName()
        );
    }

    /**
     * 按 @JsonValue 字段值查找枚举
     */
    private T findByJsonValue(String value) {
        T[] constants = enumClass.getEnumConstants();
        
        // 查找 @JsonValue 标注的字段
        for (Field field : enumClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(JsonValue.class)) {
                field.setAccessible(true);
                for (T constant : constants) {
                    try {
                        Object fieldValue = field.get(constant);
                        if (fieldValue != null && fieldValue.toString().equals(value)) {
                            return constant;
                        }
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
        }
        
        // 查找 @JsonValue 标注的方法
        for (Method method : enumClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(JsonValue.class) && method.getParameterCount() == 0) {
                method.setAccessible(true);
                for (T constant : constants) {
                    try {
                        Object methodValue = method.invoke(constant);
                        if (methodValue != null && methodValue.toString().equals(value)) {
                            return constant;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        
        return null;
    }
}
