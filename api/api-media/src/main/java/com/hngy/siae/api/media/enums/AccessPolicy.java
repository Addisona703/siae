package com.hngy.siae.api.media.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 访问策略枚举
 *
 * @author SIAE Team
 */
@Getter
public enum AccessPolicy {
    
    /**
     * 公开访问
     */
    PUBLIC("PUBLIC", "公开访问"),
    
    /**
     * 私有访问
     */
    PRIVATE("PRIVATE", "私有访问");

    @JsonValue
    private final String code;
    
    private final String description;

    AccessPolicy(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
