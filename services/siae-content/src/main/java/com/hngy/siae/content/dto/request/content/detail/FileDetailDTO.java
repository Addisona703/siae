package com.hngy.siae.content.dto.request.content.detail;

import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件详情请求 DTO（精简版）
 * 仅包含文件 ID，元数据（文件名、大小、类型等）通过 Media 服务获取
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "文件详情请求")
public class FileDetailDTO implements ContentDetailDTO {
    
    @NotBlank(message = "文件ID不能为空")
    @Schema(description = "文件ID（UUID字符串），关联 Media 服务", 
            example = "550e8400-e29b-41d4-a716-446655440000", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileId;
}
