package com.hngy.siae.media.domain.dto.upload;

import com.hngy.siae.media.domain.enums.FileStatus;
import com.hngy.siae.media.domain.enums.UploadStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 上传状态查询响应 DTO
 * 用于异步合并分片后，前端轮询查询处理结果
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "上传状态查询结果")
public class UploadStatusVO {

    @Schema(description = "上传会话ID")
    private String uploadId;

    @Schema(description = "文件ID")
    private String fileId;

    @Schema(description = "上传会话状态")
    private UploadStatus uploadStatus;

    @Schema(description = "文件状态")
    private FileStatus fileStatus;

    @Schema(description = "文件访问URL（仅当状态为COMPLETED时有效）")
    private String url;

    @Schema(description = "URL过期时间（仅私有文件有效）")
    private LocalDateTime urlExpiresAt;

    @Schema(description = "是否处理完成（COMPLETED或FAILED）")
    private boolean finished;
}
