package utils;

import com.hngy.siae.common.enums.BaseEnum;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

/**
 * 字符串枚举转换器工厂
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */

@Component
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T extends BaseEnum> @NotNull Converter<String, T> getConverter(@NotNull Class<T> targetType) {
        return new StringToBaseEnumConverter(targetType);
    }

    private record StringToBaseEnumConverter<T extends BaseEnum>(Class<T> enumType) implements Converter<String, T> {

        @Override
            public T convert(@NotNull String source) {
                for (T constant : enumType.getEnumConstants()) {
                    if (String.valueOf(constant.getCode()).equals(source) || constant.getDescription().equals(source)) {
                        return constant;
                    }
                }
                throw new IllegalArgumentException("无法将 " + source + " 转换为枚举类型：" + enumType.getName());
            }
        }
}

