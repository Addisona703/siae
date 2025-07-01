package com.hngy.siae.common.feign;

import feign.Logger;
import feign.codec.Decoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 客户端的全局默认配置
 * 这个类不应该被 @SpringBootApplication 扫描到，通常通过 @EnableFeignClients 的 defaultConfiguration 属性指定
 * @EnableFeignClients(defaultConfiguration = DefaultFeignConfig.class) 放到启动类头上，指定默认配置
 */
@Configuration
public class DefaultFeignConfig {

    /**
     * 注入自定义的解码器 Bean
     * @param feignResultDecoder 我们自己实现的解码器
     * @return Decoder
     */
    @Bean
    public Decoder feignDecoder(FeignResultDecoder feignResultDecoder) {
        return feignResultDecoder;
    }

    /**
     * 配置 Feign 的日志级别
     * @return Logger.Level
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        // FULL: 记录所有请求和响应的细节，包括头信息、请求体和元数据。非常适合开发和调试阶段。
        return Logger.Level.FULL;
    }
}
