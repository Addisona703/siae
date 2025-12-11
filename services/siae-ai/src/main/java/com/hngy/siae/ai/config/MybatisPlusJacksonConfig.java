package com.hngy.siae.ai.config;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * 配置 MyBatis-Plus JacksonTypeHandler 使用 Spring 的 ObjectMapper
 * 复用 siae-web-starter 中 JacksonConfig 配置的 ObjectMapper（已支持 LocalDateTime）
 *
 * @author SIAE Team
 */
@Configuration
@RequiredArgsConstructor
public class MybatisPlusJacksonConfig {

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        // 让 MyBatis-Plus 的 JacksonTypeHandler 使用 Spring 配置的 ObjectMapper
        JacksonTypeHandler.setObjectMapper(objectMapper);
    }
}
