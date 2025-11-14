package com.hngy.siae.content.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.core.converter.StringToBaseEnumConverterFactory;
import com.hngy.siae.core.jackson.EnumModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置 - 枚举转换配置
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StringToBaseEnumConverterFactory converterFactory;
    private final ObjectMapper objectMapper;

    /**
     * 注册 URL 参数和表单的枚举转换器
     * @param registry 类型转换管理器对象，管理各种类型之间的转换规则
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(converterFactory);
    }

    /**
     * 注册 JSON 的枚举序列化器和反序列化器
     */
    @PostConstruct
    public void configureObjectMapper() {
        // 扫描当前服务的枚举包和核心枚举包
        objectMapper.registerModule(
            new EnumModule(
                "com.hngy.siae.content.enums",
                "com.hngy.siae.core.enums"
            )
        );
    }
}

