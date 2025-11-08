package com.hngy.siae.media.domain.dto.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 上传完成请求 DTO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "上传完成提交参数")
public class UploadCompleteRequest {

    @Schema(description = "各分片的序号与ETag，用于合并对象")
    private List<PartInfo> parts;

    @Schema(description = "最终文件的校验信息，如 sha256")
    private Map<String, String> checksum;

    @Data
    @Schema(description = "上传分片完成信息")
    public static class PartInfo {

        @Schema(description = "分片序号，从1开始")
        private Integer partNumber;

        @Schema(description = "对象存储返回的ETag，用于校验分片内容")
        private String etag;
    }

}
