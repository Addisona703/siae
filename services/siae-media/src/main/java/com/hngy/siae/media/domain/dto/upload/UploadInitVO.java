package com.hngy.siae.media.domain.dto.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 上传初始化响应 DTO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "上传初始化结果")
public class UploadInitVO {

    @Schema(description = "上传会话ID")
    private String uploadId;

    @Schema(description = "文件ID")
    private String fileId;

    @Schema(description = "目标存储桶名称")
    private String bucket;

    @Schema(description = "预签名URL分片信息列表，单文件仅一条")
    private List<PartInfo> parts;

    @Schema(description = "上传时需要附带的额外HTTP头")
    private Map<String, String> headers;

    @Schema(description = "预签名URL整体过期时间")
    private LocalDateTime expireAt;

    @Schema(description = "文件访问URL（秒传时直接返回，无需上传）")
    private String url;

    @Data
    @Schema(description = "上传分片信息")
    public static class PartInfo {

        @Schema(description = "分片序号，从1开始")
        private Integer partNumber;

        @Schema(description = "该分片对应的预签名上传URL")
        private String url;

        @Schema(description = "该分片URL的单独过期时间")
        private LocalDateTime expiresAt;
    }

}
