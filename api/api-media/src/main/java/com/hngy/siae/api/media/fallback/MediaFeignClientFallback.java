package com.hngy.siae.api.media.fallback;

import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.request.BatchUrlDTO;
import com.hngy.siae.api.media.dto.response.BatchDeleteVO;
import com.hngy.siae.api.media.dto.response.BatchUrlVO;
import com.hngy.siae.api.media.dto.response.FileInfoVO;
import com.hngy.siae.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MediaFeignClient 降级实现
 * 当媒体服务不可用时执行降级逻辑
 *
 * @author SIAE Team
 */
@Component
@Slf4j
public class MediaFeignClientFallback implements MediaFeignClient {
    
    @Override
    public FileInfoVO getFileById(String fileId) {
        log.error("媒体服务不可用，获取文件详情失败，fileId: {}", fileId);
        throw new ServiceException(503, "媒体服务暂时不可用，请稍后重试");
    }
    
    @Override
    public String getFileUrl(String fileId, Integer expirySeconds) {
        log.error("媒体服务不可用，获取文件URL失败，fileId: {}", fileId);
        throw new ServiceException(503, "媒体服务暂时不可用，请稍后重试");
    }
    
    @Override
    public BatchUrlVO batchGetFileUrls(BatchUrlDTO request) {
        log.error("媒体服务不可用，批量获取文件URL失败，fileIds count: {}", 
                request != null && request.getFileIds() != null ? request.getFileIds().size() : 0);
        throw new ServiceException(503, "媒体服务暂时不可用，请稍后重试");
    }

    @Override
    public void deleteFile(String fileId) {
        log.error("媒体服务不可用，删除文件失败，fileId: {}", fileId);
        throw new ServiceException(503, "媒体服务暂时不可用，请稍后重试");
    }

    @Override
    public BatchDeleteVO batchDeleteFiles(java.util.List<String> fileIds) {
        log.error("媒体服务不可用，批量删除文件失败，fileIds count: {}", 
                fileIds != null ? fileIds.size() : 0);
        throw new ServiceException(503, "媒体服务暂时不可用，请稍后重试");
    }

    @Override
    public byte[] getFileBytes(String fileId) {
        log.error("媒体服务不可用，获取文件字节数据失败，fileId: {}", fileId);
        throw new ServiceException(503, "媒体服务暂时不可用，请稍后重试");
    }
}
