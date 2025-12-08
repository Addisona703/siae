package com.hngy.siae.api.media.dto.response;

import com.hngy.siae.api.media.enums.AccessPolicy;
import com.hngy.siae.api.media.enums.FileStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件信息响应 DTO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "文件详情信息")
public class FileInfoVO {

    @Schema(description = "文件ID")
    private String fileId;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "文件所属用户ID")
    private String ownerId;

    @Schema(description = "文件名")
    private String filename;

    @Schema(description = "存储桶名称")
    private String bucket;

    @Schema(description = "对象存储路径/键")
    private String storageKey;

    @Schema(description = "文件大小，单位：字节")
    private Long size;

    @Schema(description = "文件 MIME 类型")
    private String mime;

    @Schema(description = "文件校验值（SHA-256）")
    private String sha256;

    @Schema(description = "当前状态，如 INIT、AVAILABLE、DELETED")
    private FileStatus status;

    @Schema(description = "访问策略：PUBLIC-公开访问，PRIVATE-私有访问")
    private AccessPolicy accessPolicy;

    @Schema(description = "访问控制策略")
    private Map<String, Object> acl;

    @Schema(description = "业务标签列表")
    private List<String> bizTags;

    @Schema(description = "扩展字段，存储自定义元数据")
    private Map<String, Object> ext;

    @Schema(description = "派生文件信息，例如缩略图、转码结果")
    private List<DerivativeInfo> derivatives;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "最近更新时间")
    private LocalDateTime updatedAt;

    @Data
    @Schema(description = "派生文件详情")
    public static class DerivativeInfo {

        @Schema(description = "派生文件类型，如 thumbnail、preview")
        private String type;

        @Schema(description = "派生文件可访问的URL")
        private String url;

        @Schema(description = "派生文件大小，单位：字节")
        private Long size;

        @Schema(description = "派生文件附加元数据")
        private Map<String, Object> metadata;
    }
}
