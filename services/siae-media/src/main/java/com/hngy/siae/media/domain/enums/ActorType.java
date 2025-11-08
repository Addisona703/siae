package com.hngy.siae.media.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 操作者类型枚举
 *
 * @author SIAE Team
 */
@Getter
public enum ActorType {
    
    SERVICE("service", "服务"),
    USER("user", "用户"),
    SYSTEM("system", "系统");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    ActorType(String value, String description) {
        this.value = value;
        this.description = description;
    }

}
