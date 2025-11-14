package com.hngy.siae.media.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.core.converter.StringToBaseEnumConverterFactory;
import com.hngy.siae.core.jackson.EnumModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 枚举转换配置
 * 
 * @author KEYKB
 */
@Configuration
@RequiredArgsConstructor
public class EnumConfig implements WebMvcConfigurer {

    private final StringToBaseEnumConverterFactory converterFactory;
    private final ObjectMapper objectMapper;

    /**
     * 注册 URL 参数和表单的枚举转换器
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
        // 扫描核心枚举包
        objectMapper.registerModule(
            new EnumModule("com.hngy.siae.core.enums")
        );
    }
}
