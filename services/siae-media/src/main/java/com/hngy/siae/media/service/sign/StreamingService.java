package com.hngy.siae.media.service.sign;

import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.MediaResultCodeEnum;
import com.hngy.siae.media.domain.entity.FileEntity;
import com.hngy.siae.media.infrastructure.storage.StorageService;
import com.hngy.siae.media.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 流式播放服务
 * 支持视频、音频的流式播放
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingService {

    private final FileRepository fileRepository;
    private final StorageService storageService;
    private final SignService signService;

    /**
     * 生成流式播放URL
     * 支持视频和音频的实时播放，无需下载
     */
    public String generateStreamingUrl(String fileId, String tenantId, String userId, int expirySeconds) {
        FileEntity file = fileRepository.selectById(fileId);
        AssertUtils.notNull(file, MediaResultCodeEnum.FILE_NOT_FOUND);

        // 验证租户和权限
        AssertUtils.isTrue(file.getTenantId().equals(tenantId), MediaResultCodeEnum.UNAUTHORIZED_FILE_ACCESS);

        // 验证文件类型是否支持流式播放
        AssertUtils.isTrue(isStreamable(file.getMime()), MediaResultCodeEnum.STREAMING_TYPE_NOT_SUPPORTED);

        // 生成支持Range请求的预签名URL
        String streamingUrl = storageService.generatePresignedDownloadUrl(
                file.getBucket(),
                file.getStorageKey(),
                expirySeconds
        );

        log.info("Generated streaming URL: fileId={}, mime={}", fileId, file.getMime());
        return streamingUrl;
    }

    /**
     * 生成HLS播放列表URL（用于自适应流式播放）
     */
    public Map<String, String> generateHlsUrls(String fileId, String tenantId, String userId, int expirySeconds) {
        FileEntity file = fileRepository.selectById(fileId);
        AssertUtils.notNull(file, MediaResultCodeEnum.FILE_NOT_FOUND);

        Map<String, String> urls = new HashMap<>();
        
        // 主播放列表
        String masterPlaylist = file.getStorageKey().replace(".mp4", "/master.m3u8");
        urls.put("master", storageService.generatePresignedDownloadUrl(
                file.getBucket(), masterPlaylist, expirySeconds));

        // 不同质量的播放列表
        String[] qualities = {"360p", "480p", "720p", "1080p"};
        for (String quality : qualities) {
            String playlistKey = file.getStorageKey().replace(".mp4", "/" + quality + ".m3u8");
            urls.put(quality, storageService.generatePresignedDownloadUrl(
                    file.getBucket(), playlistKey, expirySeconds));
        }

        log.info("Generated HLS URLs: fileId={}", fileId);
        return urls;
    }

    /**
     * 检查文件是否支持流式播放
     */
    private boolean isStreamable(String mime) {
        if (mime == null) {
            return false;
        }
        return mime.startsWith("video/") || mime.startsWith("audio/");
    }

    /**
     * 获取流式播放元数据
     */
    public Map<String, Object> getStreamingMetadata(String fileId) {
        FileEntity file = fileRepository.selectById(fileId);
        AssertUtils.notNull(file, MediaResultCodeEnum.FILE_NOT_FOUND);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileId", file.getId());
        metadata.put("mime", file.getMime());
        metadata.put("size", file.getSize());
        metadata.put("streamable", isStreamable(file.getMime()));
        
        // 从扩展属性中获取视频信息
        if (file.getExt() != null) {
            metadata.put("duration", file.getExt().get("duration"));
            metadata.put("resolution", file.getExt().get("resolution"));
            metadata.put("bitrate", file.getExt().get("bitrate"));
        }

        return metadata;
    }

}
