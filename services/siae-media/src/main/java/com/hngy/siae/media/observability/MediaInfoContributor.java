package com.hngy.siae.media.observability;

import com.hngy.siae.media.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.media.domain.entity.FileEntity;
import java.util.HashMap;
import java.util.Map;

/**
 * 媒体服务信息贡献者
 * 为 /actuator/info 端点提供媒体服务的统计信息
 */
@Component
@RequiredArgsConstructor
public class MediaInfoContributor implements InfoContributor {

    private final FileRepository fileRepository;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> mediaInfo = new HashMap<>();
        
        try {
            // 获取文件总数
            long totalFiles = fileRepository.selectCount(null);
            mediaInfo.put("totalFiles", totalFiles);
            
            // 获取活跃文件数（未删除）
            LambdaQueryWrapper<FileEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNull(FileEntity::getDeletedAt);
            long activeFiles = fileRepository.selectCount(wrapper);
            mediaInfo.put("activeFiles", activeFiles);
            
            mediaInfo.put("status", "operational");
        } catch (Exception e) {
            mediaInfo.put("status", "error");
            mediaInfo.put("error", e.getMessage());
        }
        
        builder.withDetail("media", mediaInfo);
    }
}
