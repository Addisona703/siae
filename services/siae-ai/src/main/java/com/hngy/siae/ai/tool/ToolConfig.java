package com.hngy.siae.ai.tool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 工具配置类
 * 收集所有工具 Bean 供 ToolRegistry 使用
 */
@Configuration
public class ToolConfig {

    @Bean
    public List<Object> toolBeans(
            MemberQueryTool memberQueryTool,
            AwardQueryTool awardQueryTool,
            ContentQueryTool contentQueryTool,
            WeatherTool weatherTool
    ) {
        return List.of(memberQueryTool, awardQueryTool, contentQueryTool, weatherTool);
    }
}
