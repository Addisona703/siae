package com.hngy.siae.content.dto.request.content.detail;

import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频详情请求 DTO（精简版）
 * 仅包含视频文件 ID，元数据（时长、分辨率等）通过 Media 服务获取
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "视频详情请求")
public class VideoDetailDTO implements ContentDetailDTO {
    
    @NotBlank(message = "视频文件ID不能为空")
    @Schema(description = "视频文件ID（UUID字符串），关联 Media 服务", 
            example = "550e8400-e29b-41d4-a716-446655440000", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String videoFileId;
}
