package com.hngy.siae.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 视频生成请求
 */
@Data
@Schema(description = "视频生成请求")
public class VideoGenerationRequest {

    @NotBlank(message = "提示词不能为空")
    @Schema(description = "视频描述提示词，最大512字符", required = true)
    private String prompt;

    @Schema(description = "模型名称，默认cogvideox-3")
    private String model = "cogvideox-3";

    @Schema(description = "参考图片URL（可选，用于图生视频）")
    private String imageUrl;

    @Schema(description = "视频尺寸，可选：1280x720, 720x1280, 1024x1024, 1920x1080, 1080x1920, 2048x1080, 3840x2160")
    private String size = "1920x1080";

    @Schema(description = "帧率，30或60，默认30")
    private Integer fps = 30;

    @Schema(description = "是否包含AI音效，默认false")
    private Boolean withAudio = false;

    @Schema(description = "输出模式：quality质量优先，speed速度优先，默认speed")
    private String quality = "speed";

    @Schema(description = "视频时长，5或10秒，默认5")
    private Integer duration = 5;
}
