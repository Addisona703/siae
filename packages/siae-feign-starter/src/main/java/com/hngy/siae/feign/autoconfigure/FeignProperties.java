package com.hngy.siae.feign.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Feign 配置属性类
 * <p>
 * 用于配置 Feign 客户端的行为，包括解码器、日志级别等。
 *
 * @author SIAE开发团队
 */
@Data
@ConfigurationProperties(prefix = "siae.feign")
public class FeignProperties {

    /**
     * 是否启用 Feign 自动配置，默认开启
     */
    private boolean enabled = true;

    /**
     * 是否启用 Result 解包解码器，默认开启
     * <p>
     * 开启后，Feign Client 可以直接返回 T 类型，而不需要返回 Result&lt;T&gt;
     */
    private boolean unwrapResult = true;

    /**
     * 是否启用 Result 错误解码器，默认开启
     * <p>
     * 开启后，Provider 返回的错误响应会自动转换为 BusinessException
     */
    private boolean errorDecoder = true;

    /**
     * Feign 日志级别
     * <p>
     * 可选值：NONE, BASIC, HEADERS, FULL
     */
    private String logLevel = "BASIC";

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 10000;

}
