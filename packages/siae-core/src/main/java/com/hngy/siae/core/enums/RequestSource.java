package com.hngy.siae.core.enums;

import lombok.Getter;

/**
 * 请求来源枚举
 *
 * @author KEYKB
 */
@Getter
public enum RequestSource {
    /**
     * 外部请求经过网关 - 生产环境的主要请求类型
     */
    EXTERNAL_VIA_GATEWAY("gateway", "来自网关的外部请求"),

    /**
     * 内部服务间调用 - 用于服务间通信
     */
    INTERNAL_SERVICE_CALL("internal", "内部服务间调用"),

    /**
     * 直接外部访问 - 仅开发环境，生产环境应禁用
     */
    DIRECT_EXTERNAL("direct", "直接外部访问");

    private final String code;
    private final String description;

    RequestSource(String code, String description) {
        this.code = code;
        this.description = description;
    }
}