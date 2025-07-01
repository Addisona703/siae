package utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.hngy.siae.common.enums.BaseEnum;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * 通用枚举反序列化器：支持 code 和 desc 两种反序列化方式。
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
public class BaseEnumDeserializer<T extends Enum<T> & BaseEnum> extends JsonDeserializer<T> {

    private final Class<T> enumClass;

    public BaseEnumDeserializer(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public T deserialize(@NotNull JsonParser p, DeserializationContext text) throws IOException {
        String value = p.getText();

        // 尝试先按 code 查找
        try {
            int code = Integer.parseInt(value);
            T result = BaseEnum.fromCode(enumClass, code);
            if (result != null) {
                return result;
            }
        } catch (NumberFormatException ignored) {
        }

        // 再尝试按 desc 查找
        T result = BaseEnum.fromDesc(enumClass, value);
        if (result != null) {
            return result;
        }

        throw new IllegalArgumentException("无法解析枚举值：" + value + " for enum class: " + enumClass.getName());
    }
}

