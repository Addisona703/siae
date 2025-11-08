package com.hngy.siae.media.domain.dto.file;

import com.hngy.siae.media.domain.enums.FileStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件查询请求 DTO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "文件列表查询条件")
public class FileQueryRequest {

    @Schema(description = "租户ID，建议传入以命中租户索引")
    private String tenantId;

    @Schema(description = "文件所属用户ID")
    private String ownerId;

    @Schema(description = "需匹配的业务标签列表")
    private List<String> bizTags;

    @Schema(description = "文件状态过滤条件")
    private FileStatus status;

    @Schema(description = "创建时间起始（含）")
    private LocalDateTime createdFrom;

    @Schema(description = "创建时间结束（含）")
    private LocalDateTime createdTo;

    @Schema(description = "排序字段，默认 created_at")
    private String orderBy = "created_at";

    @Schema(description = "排序方式，支持 asc/desc，默认 desc")
    private String order = "desc";

}
