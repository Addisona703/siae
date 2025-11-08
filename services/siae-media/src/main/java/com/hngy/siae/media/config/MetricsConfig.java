package com.hngy.siae.media.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 指标监控配置，被 Prometheus 采集，然后在 Grafana 中可视化展示
 */
@Configuration
public class MetricsConfig {

    /**
     * 文件上传计数器
     */
    @Bean
    public Counter fileUploadCounter(MeterRegistry registry) {
        return Counter.builder("media.file.upload.total")
                .description("Total number of file uploads")
                .tag("service", "media")
                .register(registry);
    }

    /**
     * 文件上传失败计数器
     */
    @Bean
    public Counter fileUploadFailureCounter(MeterRegistry registry) {
        return Counter.builder("media.file.upload.failure.total")
                .description("Total number of failed file uploads")
                .tag("service", "media")
                .register(registry);
    }

    /**
     * 文件下载计数器
     */
    @Bean
    public Counter fileDownloadCounter(MeterRegistry registry) {
        return Counter.builder("media.file.download.total")
                .description("Total number of file downloads")
                .tag("service", "media")
                .register(registry);
    }

    /**
     * 文件扫描计数器
     */
    @Bean
    public Counter fileScanCounter(MeterRegistry registry) {
        return Counter.builder("media.file.scan.total")
                .description("Total number of file scans")
                .tag("service", "media")
                .register(registry);
    }

    /**
     * 文件扫描失败计数器
     */
    @Bean
    public Counter fileScanFailureCounter(MeterRegistry registry) {
        return Counter.builder("media.file.scan.failure.total")
                .description("Total number of failed file scans")
                .tag("service", "media")
                .register(registry);
    }

    /**
     * 媒体处理计数器
     */
    @Bean
    public Counter mediaProcessCounter(MeterRegistry registry) {
        return Counter.builder("media.process.total")
                .description("Total number of media processing tasks")
                .tag("service", "media")
                .register(registry);
    }

    /**
     * 媒体处理失败计数器
     */
    @Bean
    public Counter mediaProcessFailureCounter(MeterRegistry registry) {
        return Counter.builder("media.process.failure.total")
                .description("Total number of failed media processing tasks")
                .tag("service", "media")
                .register(registry);
    }

    /**
     * 配额超限计数器
     */
    @Bean
    public Counter quotaExceededCounter(MeterRegistry registry) {
        return Counter.builder("media.quota.exceeded.total")
                .description("Total number of quota exceeded events")
                .tag("service", "media")
                .register(registry);
    }

    /**
     * 文件上传时长计时器
     */
    @Bean
    public Timer fileUploadTimer(MeterRegistry registry) {
        return Timer.builder("media.file.upload.duration")
                .description("Duration of file upload operations")
                .tag("service", "media")
                .register(registry);
    }

    /**
     * 文件扫描时长计时器
     */
    @Bean
    public Timer fileScanTimer(MeterRegistry registry) {
        return Timer.builder("media.file.scan.duration")
                .description("Duration of file scan operations")
                .tag("service", "media")
                .register(registry);
    }

    /**
     * 媒体处理时长计时器
     */
    @Bean
    public Timer mediaProcessTimer(MeterRegistry registry) {
        return Timer.builder("media.process.duration")
                .description("Duration of media processing operations")
                .tag("service", "media")
                .register(registry);
    }
}
