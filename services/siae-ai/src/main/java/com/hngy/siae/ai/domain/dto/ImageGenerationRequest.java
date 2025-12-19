package com.hngy.siae.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 图片生成请求
 */
@Data
@Schema(description = "图片生成请求")
public class ImageGenerationRequest {

    @NotBlank(message = "提示词不能为空")
    @Schema(description = "图片描述提示词", required = true)
    private String prompt;

    @Schema(description = "模型名称，默认cogview-4-250304")
    private String model = "cogview-4-250304";

    @Schema(description = "图片尺寸，如1024x1024")
    private String size = "1024x1024";

    @Schema(description = "生成数量，默认1")
    private Integer n = 1;
}
