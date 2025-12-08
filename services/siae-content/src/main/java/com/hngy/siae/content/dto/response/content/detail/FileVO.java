package com.hngy.siae.content.dto.response.content.detail;

import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件详情响应 VO
 * 包含从 Media 服务获取的元数据
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "文件详情响应对象")
public class FileVO implements ContentDetailVO {
    
    @Schema(description = "文件详情ID", example = "1")
    private Long id;
    
    @Schema(description = "关联内容ID", example = "1001")
    private Long contentId;
    
    @Schema(description = "文件ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String fileId;
    
    @Schema(description = "下载次数", example = "256")
    private Integer downloadCount;
    
    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间", example = "2025-11-27T10:30:00")
    private LocalDateTime updateTime;
    
    // ========== 以下字段从 Media 服务获取 ==========
    
    @Schema(description = "文件名，从 Media 服务获取", example = "project-documentation.pdf")
    private String fileName;
    
    @Schema(description = "文件大小（字节），从 Media 服务获取", example = "10485760")
    private Long fileSize;
    
    @Schema(description = "文件 MIME 类型，从 Media 服务获取", example = "application/pdf")
    private String fileType;
    
    @Schema(description = "文件访问 URL", example = "https://cdn.example.com/files/project-documentation.pdf")
    private String url;
    
    @Schema(description = "Media 服务是否可用", example = "true")
    private Boolean available;
}
