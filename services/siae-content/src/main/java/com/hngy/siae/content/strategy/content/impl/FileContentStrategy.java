package com.hngy.siae.content.strategy.content.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.response.FileInfoVO;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.dto.response.content.detail.FileVO;
import com.hngy.siae.content.entity.detail.File;
import com.hngy.siae.content.mapper.FileMapper;
import com.hngy.siae.content.strategy.content.ContentStrategy;
import com.hngy.siae.content.strategy.content.ContentType;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件内容策略
 * 使用组合方式，直接注入 Mapper 和 MediaFeignClient
 * 文件元数据（文件名、大小、类型等）通过 Media 服务获取
 *
 * @author KEYKB
 * @date 2025/05/19
 */
@Slf4j
@ContentType(ContentTypeEnum.FILE)
@Component
@RequiredArgsConstructor
public class FileContentStrategy implements ContentStrategy {

    private final FileMapper fileMapper;
    private final MediaFeignClient mediaFeignClient;

    @Override
    public ContentDetailVO insert(Long contentId, ContentDetailDTO dto) {
        File file = BeanConvertUtil.to(dto, File.class);
        file.setContentId(contentId);
        int rows = fileMapper.insert(file);
        AssertUtils.isTrue(rows > 0, ContentResultCodeEnum.FILE_DETAIL_INSERT_FAILED);
        return buildFileVO(file);
    }

    @Override
    public ContentDetailVO update(Long contentId, ContentDetailDTO dto) {
        // 先查询现有记录
        File existingFile = fileMapper.selectOne(
                new LambdaQueryWrapper<File>().eq(File::getContentId, contentId));
        AssertUtils.notNull(existingFile, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        
        // 复制 DTO 属性到现有记录
        BeanConvertUtil.to(dto, existingFile);
        existingFile.setContentId(contentId);
        
        int rows = fileMapper.updateById(existingFile);
        AssertUtils.isTrue(rows > 0, ContentResultCodeEnum.FILE_DETAIL_UPDATE_FAILED);
        return buildFileVO(existingFile);
    }

    @Override
    public void delete(Long contentId) {
        fileMapper.delete(new LambdaQueryWrapper<File>().eq(File::getContentId, contentId));
    }

    @Override
    public boolean batchDelete(List<Long> contentIds) {
        return fileMapper.delete(new LambdaQueryWrapper<File>().in(File::getContentId, contentIds)) > 0;
    }

    @Override
    public List<String> getMediaFileIds(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return List.of();
        }
        return fileMapper.selectList(
                new LambdaQueryWrapper<File>()
                        .in(File::getContentId, contentIds)
                        .isNotNull(File::getFileId)
                        .select(File::getFileId)
        ).stream()
                .map(File::getFileId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
    }

    @Override
    public ContentDetailVO getDetail(Long contentId) {
        File file = fileMapper.selectOne(new LambdaQueryWrapper<File>().eq(File::getContentId, contentId));
        AssertUtils.notNull(file, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        return buildFileVO(file);
    }
    
    /**
     * 构建 FileVO，包含从 Media 服务获取的元数据
     * 
     * @param file 文件实体
     * @return FileVO 包含完整信息的文件详情
     */
    private FileVO buildFileVO(File file) {
        FileVO vo = FileVO.builder()
                .id(file.getId())
                .contentId(file.getContentId())
                .fileId(file.getFileId())
                .downloadCount(file.getDownloadCount())
                .createTime(file.getCreateTime())
                .updateTime(file.getUpdateTime())
                .available(true)
                .build();
        
        // 从 Media 服务获取文件元数据
        enrichWithMediaInfo(vo, file.getFileId());
        
        return vo;
    }
    
    /**
     * 从 Media 服务获取文件元数据并填充到 VO
     * 如果 Media 服务不可用，标记 available 为 false
     * 
     * @param vo 文件 VO
     * @param fileId 文件 ID
     */
    private void enrichWithMediaInfo(FileVO vo, String fileId) {
        if (fileId == null || fileId.isBlank()) {
            vo.setAvailable(false);
            return;
        }
        
        try {
            // 获取文件详情
            FileInfoVO fileInfo = mediaFeignClient.getFileById(fileId);
            if (fileInfo != null) {
                vo.setFileName(fileInfo.getFilename());
                vo.setFileSize(fileInfo.getSize());
                vo.setFileType(fileInfo.getMime());
            }
            
            // 获取文件访问 URL（默认 24 小时有效）
            String url = mediaFeignClient.getFileUrl(fileId, 86400);
            vo.setUrl(url);
            
        } catch (Exception e) {
            // 降级处理：Media 服务不可用时标记为不可用
            log.warn("Failed to fetch file metadata from Media service for fileId: {}, error: {}", 
                    fileId, e.getMessage());
            vo.setAvailable(false);
        }
    }
}
