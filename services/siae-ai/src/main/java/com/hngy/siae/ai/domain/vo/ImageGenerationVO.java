package com.hngy.siae.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图片生成响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "图片生成响应")
public class ImageGenerationVO {

    @Schema(description = "生成的图片列表")
    private List<ImageData> data;

    @Schema(description = "创建时间戳")
    private Long created;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageData {
        @Schema(description = "图片URL")
        private String url;

        @Schema(description = "Base64编码的图片（如果请求了的话）")
        private String b64Json;
    }
}
