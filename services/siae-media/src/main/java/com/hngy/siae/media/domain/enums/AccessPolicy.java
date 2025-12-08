package com.hngy.siae.media.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.Getter;

/**
 * 文件访问策略枚举
 * 定义文件的访问权限类型
 *
 * @author SIAE Team
 */
@Getter
public enum AccessPolicy implements BaseEnum {

    /**
     * 公开访问 - 任何人都可以访问，不需要签名
     * 适用场景：用户头像、已发布文章图片、公开资源
     * 生成永久 URL，不带签名参数
     */
    PUBLIC(1, "PUBLIC", "公开访问"),

    /**
     * 私有访问 - 需要权限验证和签名
     * 适用场景：草稿图片、私密文档、需要权限的资源
     * 生成临时签名 URL，带有过期时间
     */
    PRIVATE(2, "PRIVATE", "私有访问");

    /**
     * 枚举编码（用于数据库存储和 BaseEnum 接口）
     */
    private final int code;

    /**
     * 枚举值（用于 JSON 序列化和数据库存储）
     */
    @EnumValue
    @JsonValue
    private final String value;

    /**
     * 枚举描述（用于 BaseEnum 接口）
     */
    private final String description;

    AccessPolicy(int code, String value, String description) {
        this.code = code;
        this.value = value;
        this.description = description;
    }

    /**
     * 是否需要签名
     * PUBLIC 文件不需要签名，返回永久 URL
     * PRIVATE 文件需要签名，返回临时 URL
     *
     * @return true 如果需要签名，false 如果不需要
     */
    public boolean requiresSignature() {
        return this != PUBLIC;
    }

    /**
     * 是否公开访问
     *
     * @return true 如果是公开访问，false 如果不是
     */
    public boolean isPublic() {
        return this == PUBLIC;
    }

    /**
     * 根据值获取枚举实例
     *
     * @param value 枚举值（PUBLIC/PRIVATE）
     * @return 对应的枚举实例，如果不存在则返回 null
     */
    public static AccessPolicy fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (AccessPolicy policy : values()) {
            if (policy.value.equals(value)) {
                return policy;
            }
        }
        return null;
    }

}
