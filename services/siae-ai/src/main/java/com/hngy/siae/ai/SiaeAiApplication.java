package com.hngy.siae.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * SIAE AI Service 启动类
 * 提供基于Spring AI的智能对话与数据查询服务
 * <p>
 * 功能模块：
 * - 自然语言对话（基于LLM）
 * - Function Calling（工具调用）
 * - 成员信息查询
 * - 获奖数据查询
 * - 会话管理
 * - SSE流式响应
 *
 * @author SIAE Team
 */
@SpringBootApplication(scanBasePackages = "com.hngy.siae")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.hngy.siae.api")
@EnableAsync
@MapperScan("com.hngy.siae.ai.mapper")
public class SiaeAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SiaeAiApplication.class, args);
    }

}
