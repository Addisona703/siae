package com.hngy.siae.media.domain.dto.upload;

import com.hngy.siae.media.domain.enums.FileStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 上传完成响应 DTO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "上传完成结果")
public class UploadCompleteResponse {

    @Schema(description = "完成上传后的文件ID")
    private String fileId;

    @Schema(description = "最新文件状态，如 AVAILABLE、PROCESSING 等")
    private FileStatus status;

}
