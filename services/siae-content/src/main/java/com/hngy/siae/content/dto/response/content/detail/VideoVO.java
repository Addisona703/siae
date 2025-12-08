package com.hngy.siae.content.dto.response.content.detail;

import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频详情响应 VO
 * 包含从 Media 服务获取的元数据
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "视频详情响应对象")
public class VideoVO implements ContentDetailVO {
    
    @Schema(description = "视频详情ID", example = "1")
    private Long id;
    
    @Schema(description = "关联内容ID", example = "1001")
    private Long contentId;
    
    @Schema(description = "视频文件ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String videoFileId;
    
    @Schema(description = "播放次数", example = "12580")
    private Integer playCount;
    
    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间", example = "2025-11-27T10:30:00")
    private LocalDateTime updateTime;
    
    // ========== 以下字段从 Media 服务获取 ==========
    
    @Schema(description = "视频时长（秒），从 Media 服务获取", example = "3600")
    private Integer duration;
    
    @Schema(description = "视频分辨率，从 Media 服务获取", example = "1920x1080")
    private String resolution;
    
    @Schema(description = "文件名，从 Media 服务获取", example = "spring-boot-tutorial.mp4")
    private String filename;
    
    @Schema(description = "文件大小（字节），从 Media 服务获取", example = "1073741824")
    private Long size;
    
    @Schema(description = "MIME 类型，从 Media 服务获取", example = "video/mp4")
    private String mime;
    
    @Schema(description = "视频访问 URL", example = "https://cdn.example.com/videos/spring-boot-tutorial.mp4")
    private String url;
    
    @Schema(description = "Media 服务是否可用", example = "true")
    private Boolean available;
}
