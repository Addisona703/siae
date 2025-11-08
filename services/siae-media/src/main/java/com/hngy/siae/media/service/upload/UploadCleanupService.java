package com.hngy.siae.media.service.upload;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.media.domain.entity.FileEntity;
import com.hngy.siae.media.domain.entity.MultipartPart;
import com.hngy.siae.media.domain.entity.Upload;
import com.hngy.siae.media.domain.enums.FileStatus;
import com.hngy.siae.media.domain.enums.UploadStatus;
import com.hngy.siae.media.infrastructure.storage.StorageService;
import com.hngy.siae.media.repository.FileRepository;
import com.hngy.siae.media.repository.MultipartPartRepository;
import com.hngy.siae.media.repository.UploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上传清理服务
 * 处理上传超时和异常情况的清理
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadCleanupService {

    private final UploadRepository uploadRepository;
    private final FileRepository fileRepository;
    private final MultipartPartRepository multipartPartRepository;
    private final StorageService storageService;

    /**
     * 清理过期的上传会话
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cleanupExpiredUploads() {
        log.info("Starting cleanup of expired uploads");

        try {
            // 查询所有过期的上传会话
            LambdaQueryWrapper<Upload> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(Upload::getExpireAt, LocalDateTime.now())
                   .in(Upload::getStatus, UploadStatus.INIT, UploadStatus.IN_PROGRESS);

            List<Upload> expiredUploads = uploadRepository.selectList(wrapper);
            log.info("Found {} expired uploads to clean up", expiredUploads.size());

            for (Upload upload : expiredUploads) {
                try {
                    cleanupUpload(upload);
                } catch (Exception e) {
                    log.error("Failed to cleanup upload: {}", upload.getUploadId(), e);
                }
            }

            log.info("Completed cleanup of expired uploads");
        } catch (Exception e) {
            log.error("Error during expired uploads cleanup", e);
        }
    }

    /**
     * 清理单个上传会话
     */
    private void cleanupUpload(Upload upload) {
        log.info("Cleaning up expired upload: {}", upload.getUploadId());

        // 1. 查询文件实体
        FileEntity fileEntity = fileRepository.selectById(upload.getFileId());
        if (fileEntity == null) {
            log.warn("File entity not found for upload: {}", upload.getUploadId());
            return;
        }

        // 2. 清理对象存储中的临时文件
        try {
            if (storageService.objectExists(fileEntity.getBucket(), fileEntity.getStorageKey())) {
                storageService.deleteObject(fileEntity.getBucket(), fileEntity.getStorageKey());
                log.info("Deleted temporary object: {}/{}", fileEntity.getBucket(), fileEntity.getStorageKey());
            }
        } catch (Exception e) {
            log.error("Failed to delete temporary object for upload: {}", upload.getUploadId(), e);
        }

        // 3. 删除分片记录
        if (upload.getMultipart()) {
            try {
                LambdaQueryWrapper<MultipartPart> partWrapper = new LambdaQueryWrapper<>();
                partWrapper.eq(MultipartPart::getUploadId, upload.getUploadId());
                int deletedParts = multipartPartRepository.delete(partWrapper);
                log.info("Deleted {} multipart parts for upload: {}", deletedParts, upload.getUploadId());
            } catch (Exception e) {
                log.error("Failed to delete multipart parts for upload: {}", upload.getUploadId(), e);
            }
        }

        // 4. 更新上传会话状态为已过期
        upload.setStatus(UploadStatus.EXPIRED);
        uploadRepository.updateById(upload);

        // 5. 更新文件状态为失败
        fileEntity.setStatus(FileStatus.FAILED);
        fileRepository.updateById(fileEntity);

        log.info("Successfully cleaned up expired upload: {}", upload.getUploadId());
    }

    /**
     * 清理失败的上传
     * 每天凌晨2点执行一次
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cleanupFailedUploads() {
        log.info("Starting cleanup of failed uploads");

        try {
            // 查询7天前失败的上传会话
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7);
            
            LambdaQueryWrapper<Upload> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(Upload::getCreatedAt, cutoffTime)
                   .in(Upload::getStatus, UploadStatus.ABORTED, UploadStatus.EXPIRED);

            List<Upload> failedUploads = uploadRepository.selectList(wrapper);
            log.info("Found {} failed uploads to clean up", failedUploads.size());

            for (Upload upload : failedUploads) {
                try {
                    deleteUploadRecord(upload);
                } catch (Exception e) {
                    log.error("Failed to delete upload record: {}", upload.getUploadId(), e);
                }
            }

            log.info("Completed cleanup of failed uploads");
        } catch (Exception e) {
            log.error("Error during failed uploads cleanup", e);
        }
    }

    /**
     * 删除上传记录
     */
    private void deleteUploadRecord(Upload upload) {
        log.info("Deleting upload record: {}", upload.getUploadId());

        // 1. 删除分片记录
        if (upload.getMultipart()) {
            LambdaQueryWrapper<MultipartPart> partWrapper = new LambdaQueryWrapper<>();
            partWrapper.eq(MultipartPart::getUploadId, upload.getUploadId());
            multipartPartRepository.delete(partWrapper);
        }

        // 2. 删除上传会话记录
        uploadRepository.deleteById(upload.getUploadId());

        // 3. 删除关联的失败文件记录
        FileEntity fileEntity = fileRepository.selectById(upload.getFileId());
        if (fileEntity != null && fileEntity.getStatus() == FileStatus.FAILED) {
            fileRepository.deleteById(fileEntity.getId());
            log.info("Deleted failed file record: {}", fileEntity.getId());
        }

        log.info("Successfully deleted upload record: {}", upload.getUploadId());
    }

}
