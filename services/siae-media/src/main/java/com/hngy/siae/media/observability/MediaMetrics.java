package com.hngy.siae.media.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 媒体服务指标工具类
 */
@Component
@RequiredArgsConstructor
public class MediaMetrics {

    private final MeterRegistry meterRegistry;

    /**
     * 记录文件上传成功
     */
    public void recordFileUpload(String tenantId, long fileSize) {
        Counter.builder("media.file.upload.total")
                .tag("tenant", tenantId)
                .register(meterRegistry)
                .increment();

        meterRegistry.gauge("media.file.size.bytes", fileSize);
    }

    /**
     * 记录文件上传失败
     */
    public void recordFileUploadFailure(String tenantId, String reason) {
        Counter.builder("media.file.upload.failure.total")
                .tag("tenant", tenantId)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录文件下载
     */
    public void recordFileDownload(String tenantId, long fileSize) {
        Counter.builder("media.file.download.total")
                .tag("tenant", tenantId)
                .register(meterRegistry)
                .increment();

        Counter.builder("media.file.download.bytes.total")
                .tag("tenant", tenantId)
                .register(meterRegistry)
                .increment(fileSize);
    }

    /**
     * 记录文件扫描
     */
    public void recordFileScan(String tenantId, String scanType, boolean success) {
        if (success) {
            Counter.builder("media.file.scan.total")
                    .tag("tenant", tenantId)
                    .tag("type", scanType)
                    .register(meterRegistry)
                    .increment();
        } else {
            Counter.builder("media.file.scan.failure.total")
                    .tag("tenant", tenantId)
                    .tag("type", scanType)
                    .register(meterRegistry)
                    .increment();
        }
    }

    /**
     * 记录媒体处理
     */
    public void recordMediaProcess(String tenantId, String mediaType, boolean success) {
        if (success) {
            Counter.builder("media.process.total")
                    .tag("tenant", tenantId)
                    .tag("type", mediaType)
                    .register(meterRegistry)
                    .increment();
        } else {
            Counter.builder("media.process.failure.total")
                    .tag("tenant", tenantId)
                    .tag("type", mediaType)
                    .register(meterRegistry)
                    .increment();
        }
    }

    /**
     * 记录配额超限
     */
    public void recordQuotaExceeded(String tenantId, String quotaType) {
        Counter.builder("media.quota.exceeded.total")
                .tag("tenant", tenantId)
                .tag("type", quotaType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录存储使用量
     */
    public void recordStorageUsage(String tenantId, long usedBytes, long totalBytes) {
        meterRegistry.gauge("media.storage.used.bytes", 
                java.util.Collections.singletonList(io.micrometer.core.instrument.Tag.of("tenant", tenantId)), 
                usedBytes);
        meterRegistry.gauge("media.storage.total.bytes", 
                java.util.Collections.singletonList(io.micrometer.core.instrument.Tag.of("tenant", tenantId)), 
                totalBytes);
    }

    /**
     * 记录文件数量
     */
    public void recordFileCount(String tenantId, long count) {
        meterRegistry.gauge("media.file.count", 
                java.util.Collections.singletonList(io.micrometer.core.instrument.Tag.of("tenant", tenantId)), 
                count);
    }

    /**
     * 创建计时器样本
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 记录上传时长
     */
    public void recordUploadDuration(Timer.Sample sample, String tenantId) {
        sample.stop(Timer.builder("media.file.upload.duration")
                .tag("tenant", tenantId)
                .register(meterRegistry));
    }

    /**
     * 记录扫描时长
     */
    public void recordScanDuration(Timer.Sample sample, String tenantId, String scanType) {
        sample.stop(Timer.builder("media.file.scan.duration")
                .tag("tenant", tenantId)
                .tag("type", scanType)
                .register(meterRegistry));
    }

    /**
     * 记录处理时长
     */
    public void recordProcessDuration(Timer.Sample sample, String tenantId, String mediaType) {
        sample.stop(Timer.builder("media.process.duration")
                .tag("tenant", tenantId)
                .tag("type", mediaType)
                .register(meterRegistry));
    }
}
