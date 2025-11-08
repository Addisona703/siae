package com.hngy.siae.media.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.MediaResultCodeEnum;
import com.hngy.siae.media.domain.entity.FileEntity;
import com.hngy.siae.media.domain.event.FileEvent;
import com.hngy.siae.media.infrastructure.messaging.EventIdempotency;
import com.hngy.siae.media.repository.FileRepository;
import com.hngy.siae.media.service.MediaProcessService;
import com.hngy.siae.messaging.consumer.SiaeRabbitListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 媒体处理Worker
 * 消费文件上传事件，生成缩略图和预览
 *
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediaProcessWorker {

    private final ObjectMapper objectMapper;
    private final EventIdempotency eventIdempotency;
    private final FileRepository fileRepository;
    private final MediaProcessService mediaProcessService;

    /**
     * 监听媒体处理队列
     * 使用 siae-messaging-starter 的 @SiaeRabbitListener 注解
     */
    @SiaeRabbitListener(queues = "media.file.process")
    public void handleFileEvent(String message) {
        try {
            FileEvent event = objectMapper.readValue(message, FileEvent.class);

            // 只处理上传完成事件
            if (!"file.uploaded".equals(event.getEventType())) {
                log.debug("Skipping non-upload event: {}", event.getEventType());
                return;
            }

            // 幂等性检查（使用不同的key前缀）
            String idempotencyKey = "media-process:" + event.getEventId();
            if (!eventIdempotency.tryProcess(idempotencyKey)) {
                log.info("Event already processed: {}", event.getEventId());
                return;
            }

            log.info("Processing media: fileId={}", event.getFileId());
            processMedia(event);

            log.info("Media processing completed: fileId={}", event.getFileId());

        } catch (Exception e) {
            log.error("Failed to process media event", e);
            // 抛出异常让 RabbitMQ 重试
            AssertUtils.fail(MediaResultCodeEnum.MEDIA_PROCESS_FAILED);
        }
    }

    private void processMedia(FileEvent event) {
        FileEntity file = fileRepository.selectById(event.getFileId());
        if (file == null) {
            log.warn("File not found: {}", event.getFileId());
            return;
        }

        String mime = file.getMime();
        if (mime == null) {
            log.info("No MIME type, skip media processing: fileId={}", file.getId());
            return;
        }

        try {
            // 处理图片
            if (mime.startsWith("image/")) {
                mediaProcessService.generateImageThumbnails(file);
            }
            // 处理视频
            else if (mime.startsWith("video/")) {
                mediaProcessService.generateVideoThumbnails(file);
                mediaProcessService.generateVideoPreview(file);
            }
            // 处理音频
            else if (mime.startsWith("audio/")) {
                mediaProcessService.generateAudioWaveform(file);
            }
            // 处理文档
            else if (isDocumentType(mime)) {
                mediaProcessService.generateDocumentPreview(file);
            }

            log.info("Media processing succeeded: fileId={}, mime={}", file.getId(), mime);

        } catch (Exception e) {
            log.error("Media processing failed: fileId={}", file.getId(), e);
        }
    }

    /**
     * 判断是否为文档类型
     */
    private boolean isDocumentType(String mime) {
        if (mime == null) {
            return false;
        }
        
        // PDF 文档
        if (mime.equals("application/pdf")) {
            return true;
        }
        
        // Microsoft Office 文档
        if (mime.equals("application/msword") || // .doc
            mime.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || // .docx
            mime.equals("application/vnd.ms-excel") || // .xls
            mime.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") || // .xlsx
            mime.equals("application/vnd.ms-powerpoint") || // .ppt
            mime.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) { // .pptx
            return true;
        }
        
        // OpenDocument 格式
        if (mime.equals("application/vnd.oasis.opendocument.text") || // .odt
            mime.equals("application/vnd.oasis.opendocument.spreadsheet") || // .ods
            mime.equals("application/vnd.oasis.opendocument.presentation")) { // .odp
            return true;
        }
        
        // 文本文档
        if (mime.equals("text/plain") || // .txt
            mime.equals("text/markdown") || // .md
            mime.equals("text/csv") || // .csv
            mime.equals("application/rtf")) { // .rtf
            return true;
        }
        
        return false;
    }

}
