package com.hngy.siae.media.domain.dto.upload;

import com.hngy.siae.media.domain.enums.AccessPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 上传初始化请求 DTO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "上传初始化请求参数")
public class UploadInitDTO {

    @Schema(description = "原始文件名（包含扩展名）")
    @NotBlank(message = "文件名不能为空")
    private String filename;

    @Schema(description = "文件大小，单位：字节")
    @NotNull(message = "文件大小不能为空")
    private Long size;

    @Schema(description = "文件 MIME 类型，例如 image/jpeg")
    private String mime;

    @Schema(description = "租户ID，用于路由存储空间")
    @NotBlank(message = "租户ID不能为空")
    private String tenantId;

    @Schema(description = "所有者ID，文件的拥有者用户ID")
    private String ownerId;

    @Schema(description = "访问策略：PUBLIC-公开访问，PRIVATE-私有访问，PROTECTED-受保护访问，默认为 PRIVATE")
    private AccessPolicy accessPolicy = AccessPolicy.PRIVATE;

    @Schema(description = "业务标签集合，用于分类或检索")
    private java.util.List<String> bizTags;

    @Schema(description = "分片上传配置，单文件上传可为空")
    private MultipartConfig multipart;

    @Schema(description = "客户端计算的校验和，例如 sha256 值")
    private Map<String, String> checksum;

    @Schema(description = "访问控制策略，如公开/私有、允许用户列表等")
    private Map<String, Object> acl;

    @Schema(description = "自定义扩展信息，按业务自行约定")
    private Map<String, Object> ext;

    @Data
    @Schema(description = "分片上传配置")
    public static class MultipartConfig {

        @Schema(description = "是否启用分片上传")
        private Boolean enabled;

        @Schema(description = "单个分片大小，单位：字节")
        private Integer partSize;
    }

}
