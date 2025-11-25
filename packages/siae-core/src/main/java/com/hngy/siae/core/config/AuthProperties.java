package com.hngy.siae.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 认证配置属性
 * 通用配置，供网关和微服务共同使用
 *
 * @author KEYKB
 */
@Data
@Component
@ConfigurationProperties(prefix = "siae.auth")
public class AuthProperties {

    /**
     * 是否启用网关认证模式 - 生产环境建议true
     */
    private boolean enableGatewayAuth = true;

    /**
     * 是否允许直接外部访问 - 生产环境建议false，开发环境可设为true
     */
    private boolean enableDirectAccess = false;

    /**
     * 网关密钥 - 用于验证请求确实来自网关
     */
    private String gatewaySecretKey = "siae-gateway-2025";

    /**
     * 内部服务调用密钥 - 用于服务间调用认证
     */
    private String internalSecretKey = "siae-internal-2025";

    /**
     * 网关密钥有效期（秒）- 防重放攻击
     */
    private int gatewaySecretValidSeconds = 300;
}