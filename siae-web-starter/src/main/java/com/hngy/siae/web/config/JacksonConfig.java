package com.hngy.siae.web.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.hngy.siae.web.properties.WebProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson序列化配置
 * 支持配置化的日期格式、时区等设置
 * 
 * @author SIAE开发团队
 */
@Configuration
@ConditionalOnProperty(prefix = "siae.web.jackson", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class JacksonConfig {

    private final WebProperties webProperties;

    /**
     * Jackson 自定义配置
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            WebProperties.Jackson jacksonConfig = webProperties.getJackson();
            
            // 设置日期格式和时区
            builder.simpleDateFormat(jacksonConfig.getDateFormat());
            builder.timeZone(TimeZone.getTimeZone(jacksonConfig.getTimeZone()));
            
            // 配置 Java 8 时间模块
            JavaTimeModule timeModule = new JavaTimeModule();
            
            // LocalDateTime 格式化
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(jacksonConfig.getDateFormat());
            timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
            timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
            
            // LocalDate 格式化
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
            timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
            
            // LocalTime 格式化
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
            timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));
            
            builder.modules(timeModule);
            
            // 其他配置
            builder.featuresToDisable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
            );
            
            // 是否序列化 null 值
            if (!jacksonConfig.isSerializeNulls()) {
                builder.featuresToEnable(SerializationFeature.WRITE_NULL_MAP_VALUES);
            }
        };
    }

    /**
     * 自定义 ObjectMapper（如果需要更细粒度的控制）
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.build();
        
        // 忽略未知属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 允许空对象
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        return mapper;
    }
}
