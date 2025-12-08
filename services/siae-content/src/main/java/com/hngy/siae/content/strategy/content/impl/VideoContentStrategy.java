package com.hngy.siae.content.strategy.content.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.response.FileInfoVO;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.dto.response.content.detail.VideoVO;
import com.hngy.siae.content.entity.detail.Video;
import com.hngy.siae.content.mapper.VideoMapper;
import com.hngy.siae.content.strategy.content.ContentStrategy;
import com.hngy.siae.content.strategy.content.ContentType;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 视频内容策略
 * 使用组合方式，直接注入 Mapper 和 MediaFeignClient
 * 视频元数据（时长、分辨率等）通过 Media 服务获取
 *
 * @author KEYKB
 * @date 2025/05/19
 */
@Slf4j
@ContentType(ContentTypeEnum.VIDEO)
@Component
@RequiredArgsConstructor
public class VideoContentStrategy implements ContentStrategy {

    private final VideoMapper videoMapper;
    private final MediaFeignClient mediaFeignClient;

    @Override
    public ContentDetailVO insert(Long contentId, ContentDetailDTO dto) {
        Video video = BeanConvertUtil.to(dto, Video.class);
        video.setContentId(contentId);
        int rows = videoMapper.insert(video);
        AssertUtils.isTrue(rows > 0, ContentResultCodeEnum.VIDEO_DETAIL_INSERT_FAILED);
        return buildVideoVO(video);
    }

    @Override
    public ContentDetailVO update(Long contentId, ContentDetailDTO dto) {
        // 先查询现有记录
        Video existingVideo = videoMapper.selectOne(
                new LambdaQueryWrapper<Video>().eq(Video::getContentId, contentId));
        AssertUtils.notNull(existingVideo, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        
        // 复制 DTO 属性到现有记录
        BeanConvertUtil.to(dto, existingVideo);
        existingVideo.setContentId(contentId);
        
        int rows = videoMapper.updateById(existingVideo);
        AssertUtils.isTrue(rows > 0, ContentResultCodeEnum.VIDEO_DETAIL_UPDATE_FAILED);
        return buildVideoVO(existingVideo);
    }

    @Override
    public void delete(Long contentId) {
        videoMapper.delete(new LambdaQueryWrapper<Video>().eq(Video::getContentId, contentId));
    }

    @Override
    public boolean batchDelete(List<Long> contentIds) {
        return videoMapper.delete(new LambdaQueryWrapper<Video>().in(Video::getContentId, contentIds)) > 0;
    }

    @Override
    public List<String> getMediaFileIds(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return List.of();
        }
        return videoMapper.selectList(
                new LambdaQueryWrapper<Video>()
                        .in(Video::getContentId, contentIds)
                        .isNotNull(Video::getVideoFileId)
                        .select(Video::getVideoFileId)
        ).stream()
                .map(Video::getVideoFileId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
    }

    @Override
    public ContentDetailVO getDetail(Long contentId) {
        Video video = videoMapper.selectOne(new LambdaQueryWrapper<Video>().eq(Video::getContentId, contentId));
        AssertUtils.notNull(video, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        return buildVideoVO(video);
    }
    
    /**
     * 构建 VideoVO，包含从 Media 服务获取的元数据
     * 
     * @param video 视频实体
     * @return VideoVO 包含完整信息的视频详情
     */
    private VideoVO buildVideoVO(Video video) {
        VideoVO vo = VideoVO.builder()
                .id(video.getId())
                .contentId(video.getContentId())
                .videoFileId(video.getVideoFileId())
                .playCount(video.getPlayCount())
                .createTime(video.getCreateTime())
                .updateTime(video.getUpdateTime())
                .available(true)
                .build();
        
        // 从 Media 服务获取视频元数据
        enrichWithMediaInfo(vo, video.getVideoFileId());
        
        return vo;
    }
    
    /**
     * 从 Media 服务获取视频元数据并填充到 VO
     * 如果 Media 服务不可用，标记 available 为 false
     * 使用并行调用优化性能
     * 
     * @param vo 视频 VO
     * @param videoFileId 视频文件 ID
     */
    private void enrichWithMediaInfo(VideoVO vo, String videoFileId) {
        if (videoFileId == null || videoFileId.isBlank()) {
            vo.setAvailable(false);
            return;
        }
        
        try {
            // 并行调用：同时获取文件详情和访问URL
            java.util.concurrent.CompletableFuture<FileInfoVO> fileInfoFuture = 
                java.util.concurrent.CompletableFuture.supplyAsync(() -> 
                    mediaFeignClient.getFileById(videoFileId));
            
            java.util.concurrent.CompletableFuture<String> urlFuture = 
                java.util.concurrent.CompletableFuture.supplyAsync(() -> 
                    mediaFeignClient.getFileUrl(videoFileId, 86400));
            
            // 等待两个请求完成（超时5秒）
            java.util.concurrent.CompletableFuture.allOf(fileInfoFuture, urlFuture)
                .get(5, java.util.concurrent.TimeUnit.SECONDS);
            
            // 处理文件详情
            FileInfoVO fileInfo = fileInfoFuture.get();
            if (fileInfo != null) {
                vo.setFilename(fileInfo.getFilename());
                vo.setSize(fileInfo.getSize());
                vo.setMime(fileInfo.getMime());
                
                // 从 ext 字段获取视频特有信息（时长、分辨率）
                Map<String, Object> ext = fileInfo.getExt();
                if (ext != null) {
                    if (ext.get("duration") != null) {
                        vo.setDuration(((Number) ext.get("duration")).intValue());
                    }
                    if (ext.get("resolution") != null) {
                        vo.setResolution((String) ext.get("resolution"));
                    }
                }
            }
            
            // 设置视频访问 URL
            vo.setUrl(urlFuture.get());
            
        } catch (Exception e) {
            // 降级处理：Media 服务不可用时标记为不可用
            log.warn("Failed to fetch video metadata from Media service for fileId: {}, error: {}", 
                    videoFileId, e.getMessage());
            vo.setAvailable(false);
        }
    }
}
