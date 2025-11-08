package com.hngy.siae.media.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.MediaResultCodeEnum;
import com.hngy.siae.media.domain.entity.FileEntity;
import com.hngy.siae.media.domain.enums.FileStatus;
import com.hngy.siae.media.domain.event.FileEvent;
import com.hngy.siae.media.infrastructure.messaging.EventIdempotency;
import com.hngy.siae.media.repository.FileRepository;
import com.hngy.siae.media.service.ScanService;
import com.hngy.siae.messaging.consumer.SiaeRabbitListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文件扫描Worker
 * 消费文件上传事件，执行病毒扫描和内容审核
 *
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileScanWorker {

    private final ObjectMapper objectMapper;
    private final EventIdempotency eventIdempotency;
    private final FileRepository fileRepository;
    private final ScanService scanService;

    /**
     * 监听文件扫描队列
     * 使用 siae-messaging-starter 的 @SiaeRabbitListener 注解
     */
    @SiaeRabbitListener(queues = "media.file.scan")
    public void handleFileEvent(String message) {
        try {
            FileEvent event = objectMapper.readValue(message, FileEvent.class);

            // 只处理上传完成事件
            if (!"file.uploaded".equals(event.getEventType())) {
                log.debug("Skipping non-upload event: {}", event.getEventType());
                return;
            }

            // 幂等性检查
            if (!eventIdempotency.tryProcess(event.getEventId())) {
                log.info("Event already processed: {}", event.getEventId());
                return;
            }

            log.info("Processing file scan: fileId={}", event.getFileId());
            processFileScan(event);

            log.info("File scan completed: fileId={}", event.getFileId());

        } catch (Exception e) {
            log.error("Failed to process file scan event", e);
            // 抛出异常让 RabbitMQ 重试
            AssertUtils.fail(MediaResultCodeEnum.FILE_SCAN_FAILED);
        }
    }

    private void processFileScan(FileEvent event) {
        FileEntity file = fileRepository.selectById(event.getFileId());
        if (file == null) {
            log.warn("File not found: {}", event.getFileId());
            return;
        }

        try {
            // 执行病毒扫描
            boolean virusFree = scanService.scanForVirus(file);
            
            // 执行内容审核
            boolean contentSafe = scanService.auditContent(file);

            // 更新文件状态和安全标记
            if (virusFree && contentSafe) {
                // 扫描通过，保持 COMPLETED 状态
                updateSecurityMetadata(file, true, true);
            } else {
                // 扫描未通过，标记为 FAILED
                file.setStatus(FileStatus.FAILED);
                updateSecurityMetadata(file, virusFree, contentSafe);
            }

            fileRepository.updateById(file);
            log.info("File scan result: fileId={}, virusFree={}, contentSafe={}", 
                    file.getId(), virusFree, contentSafe);

        } catch (Exception e) {
            log.error("File scan failed: fileId={}", file.getId(), e);
            file.setStatus(FileStatus.FAILED);
            fileRepository.updateById(file);
        }
    }

    private void updateSecurityMetadata(FileEntity file, boolean virusFree, boolean contentSafe) {
        var ext = file.getExt();
        if (ext == null) {
            ext = new java.util.HashMap<>();
        }
        ext.put("virusScan", virusFree ? "clean" : "infected");
        ext.put("contentAudit", contentSafe ? "safe" : "unsafe");
        ext.put("scanTime", java.time.LocalDateTime.now().toString());
        file.setExt(ext);
    }

}
