package com.hngy.siae.notification.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 发送状态枚举
 */
@Getter
public enum SendStatus {
    
    PENDING(0, "待发送"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败");
    
    @EnumValue  // MyBatis-Plus 注解，标记数据库存储的值
    private final Integer code;
    
    @JsonValue  // Jackson 注解，JSON 序列化时使用 name
    private final String name;
    
    SendStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据 code 获取枚举
     */
    public static SendStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SendStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的发送状态: " + code);
    }
}
