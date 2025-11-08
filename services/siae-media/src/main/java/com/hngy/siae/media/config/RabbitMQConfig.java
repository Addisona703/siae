package com.hngy.siae.media.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 队列配置
 * 自动创建媒体服务所需的队列
 *
 * @author SIAE Team
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 文件扫描队列
     */
    @Bean
    public Queue fileScanQueue() {
        return new Queue("media.file.scan", true);
    }

    /**
     * 媒体处理队列
     */
    @Bean
    public Queue fileProcessQueue() {
        return new Queue("media.file.process", true);
    }
}
