package com.hngy.siae.media.service;

import com.hngy.siae.media.domain.entity.FileDerivative;
import com.hngy.siae.media.domain.entity.FileEntity;
import com.hngy.siae.media.repository.FileDerivativeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 媒体处理服务
 * 提供缩略图、预览生成等功能
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaProcessService {

    private final FileDerivativeRepository fileDerivativeRepository;

    /**
     * 生成图片缩略图
     */
    public void generateImageThumbnails(FileEntity file) {
        log.info("Generating image thumbnails: fileId={}", file.getId());

        try {
            // 生成多个尺寸的缩略图
            int[] sizes = {64, 128, 256, 512};
            
            for (int size : sizes) {
                String thumbnailKey = generateThumbnailKey(file.getStorageKey(), size);
                
                // TODO: 实际的图片处理逻辑
                // 使用ImageMagick、Thumbnailator或其他图片处理库
                
                // 保存衍生文件记录
                saveDerivative(file, "thumb", thumbnailKey, size);
                log.info("Generated thumbnail: fileId={}, size={}", file.getId(), size);
            }

        } catch (Exception e) {
            log.error("Failed to generate image thumbnails: fileId={}", file.getId(), e);
        }
    }

    /**
     * 生成视频缩略图
     */
    public void generateVideoThumbnails(FileEntity file) {
        log.info("Generating video thumbnails: fileId={}", file.getId());

        try {
            // 提取视频关键帧作为缩略图
            String thumbnailKey = generateThumbnailKey(file.getStorageKey(), 512);
            
            // TODO: 使用FFmpeg提取视频帧
            
            saveDerivative(file, "thumb", thumbnailKey, 512);
            log.info("Generated video thumbnail: fileId={}", file.getId());

        } catch (Exception e) {
            log.error("Failed to generate video thumbnails: fileId={}", file.getId(), e);
        }
    }

    /**
     * 生成视频预览
     */
    public void generateVideoPreview(FileEntity file) {
        log.info("Generating video preview: fileId={}", file.getId());

        try {
            String previewKey = file.getStorageKey().replace(".mp4", "_preview.mp4");
            
            // TODO: 使用FFmpeg生成低分辨率预览视频
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("duration", "30s");
            metadata.put("resolution", "480p");
            
            saveDerivative(file, "preview", previewKey, 0, metadata);
            log.info("Generated video preview: fileId={}", file.getId());

        } catch (Exception e) {
            log.error("Failed to generate video preview: fileId={}", file.getId(), e);
        }
    }

    /**
     * 生成音频波形图
     */
    public void generateAudioWaveform(FileEntity file) {
        log.info("Generating audio waveform: fileId={}", file.getId());

        try {
            String waveformKey = file.getStorageKey() + ".waveform.png";
            
            // TODO: 使用FFmpeg或其他工具生成波形图
            
            saveDerivative(file, "preview", waveformKey, 0);
            log.info("Generated audio waveform: fileId={}", file.getId());

        } catch (Exception e) {
            log.error("Failed to generate audio waveform: fileId={}", file.getId(), e);
        }
    }

    /**
     * 生成文档预览
     */
    public void generateDocumentPreview(FileEntity file) {
        log.info("Generating document preview: fileId={}", file.getId());

        try {
            String previewKey = file.getStorageKey().replace(".pdf", "_preview.png");
            
            // TODO: 使用PDF处理库生成预览图
            
            saveDerivative(file, "preview", previewKey, 0);
            log.info("Generated document preview: fileId={}", file.getId());

        } catch (Exception e) {
            log.error("Failed to generate document preview: fileId={}", file.getId(), e);
        }
    }

    private String generateThumbnailKey(String originalKey, int size) {
        String baseName = originalKey.substring(0, originalKey.lastIndexOf('.'));
        String extension = originalKey.substring(originalKey.lastIndexOf('.'));
        return baseName + "_thumb_" + size + extension;
    }

    private void saveDerivative(FileEntity file, String type, String storageKey, int size) {
        saveDerivative(file, type, storageKey, size, null);
    }

    private void saveDerivative(FileEntity file, String type, String storageKey, 
                               int size, Map<String, Object> metadata) {
        FileDerivative derivative = new FileDerivative();
        derivative.setId(UUID.randomUUID().toString());
        derivative.setFileId(file.getId());
        derivative.setType(type);
        derivative.setStorageKey(storageKey);
        derivative.setSize((long) size);
        
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put("originalMime", file.getMime());
        derivative.setMetadata(metadata);
        
        derivative.setCreatedAt(LocalDateTime.now());

        fileDerivativeRepository.insert(derivative);
    }

    /**
     * 判断是否可以处理该类型的文件
     */
    public boolean canProcess(String mime) {
        if (mime == null) {
            return false;
        }

        // 图片类型
        if (mime.startsWith("image/")) {
            return true;
        }

        // 视频类型
        if (mime.startsWith("video/")) {
            return true;
        }

        // 音频类型
        if (mime.startsWith("audio/")) {
            return true;
        }

        // 文档类型
        return isDocumentType(mime);
    }

    /**
     * 判断是否为文档类型
     */
    private boolean isDocumentType(String mime) {
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
