package com.hngy.siae.media.domain.dto.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上传刷新响应 DTO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "刷新上传会话后的预签名信息")
public class UploadRefreshResponse {

    @Schema(description = "上传会话ID")
    private String uploadId;

    @Schema(description = "刷新后的分片URL列表")
    private List<PartInfo> parts;

    @Schema(description = "本次刷新后的统一过期时间")
    private LocalDateTime expiresAt;

    @Data
    @Schema(description = "刷新得到的分片URL信息")
    public static class PartInfo {

        @Schema(description = "分片序号，从1开始")
        private Integer partNumber;

        @Schema(description = "最新生成的预签名URL")
        private String url;

        @Schema(description = "该分片URL的过期时间")
        private LocalDateTime expiresAt;
    }

}
