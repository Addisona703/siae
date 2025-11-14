package com.hngy.siae.notification.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 通知类型枚举
 */
@Getter
public enum NotificationType {
    
    SYSTEM(1, "系统通知"),
    ANNOUNCEMENT(2, "公告"),
    REMIND(3, "提醒");
    
    @EnumValue  // MyBatis-Plus 注解，标记数据库存储的值
    private final Integer code;
    
    @JsonValue  // Jackson 注解，JSON 序列化时使用 name
    private final String name;
    
    NotificationType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据 code 获取枚举
     */
    public static NotificationType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NotificationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的通知类型: " + code);
    }
}
