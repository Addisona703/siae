package com.hngy.siae.core.converter;

import com.hngy.siae.core.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * 字符串到枚举的转换器工厂
 * 用于处理 URL 参数和表单数据
 * 支持通过 code 或 description 转换
 * 
 * @author KEYKB
 */
@Component
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(@NonNull Class<T> targetType) {
        return new StringToBaseEnumConverter(targetType);
    }

    private record StringToBaseEnumConverter<T extends Enum<T> & BaseEnum>(Class<T> enumType) 
            implements Converter<String, T> {

        @Override
        public T convert(@NonNull String source) {
            // 尝试按 code 转换
            try {
                int code = Integer.parseInt(source);
                T result = BaseEnum.fromCode(enumType, code);
                if (result != null) {
                    return result;
                }
            } catch (NumberFormatException ignored) {
            }

            // 尝试按 description 转换
            T result = BaseEnum.fromDesc(enumType, source);
            if (result != null) {
                return result;
            }

            throw new IllegalArgumentException(
                "无法将 '" + source + "' 转换为枚举类型: " + enumType.getName()
            );
        }
    }
}
