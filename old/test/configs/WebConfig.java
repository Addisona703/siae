package com.hngy.siae.common.configs;

import com.hngy.siae.common.utils.StringToBaseEnumConverterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;

/**
 * web配置
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StringToBaseEnumConverterFactory converterFactory;

    /***
     * 注册枚举转换工厂
     * @param registry 类型转换管理器对象，管理各种类型之间的转换规则
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(converterFactory);
    }
}

