package com.hngy.siae.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 视频生成响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频生成响应")
public class VideoGenerationVO {

    @Schema(description = "任务ID，用于查询生成结果")
    private String taskId;

    @Schema(description = "任务状态：PROCESSING处理中，SUCCESS成功，FAIL失败")
    private String taskStatus;

    @Schema(description = "生成的视频列表（成功时返回）")
    private List<VideoData> videoResult;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoData {
        @Schema(description = "视频URL")
        private String url;

        @Schema(description = "封面图URL")
        private String coverImageUrl;
    }
}
