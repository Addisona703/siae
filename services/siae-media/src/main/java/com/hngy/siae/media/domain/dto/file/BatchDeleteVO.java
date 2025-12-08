package com.hngy.siae.media.domain.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量删除文件响应VO
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量删除文件响应")
public class BatchDeleteVO {

    @Schema(description = "成功删除的文件ID列表")
    private List<String> successIds;

    @Schema(description = "删除失败的文件ID列表")
    private List<String> failedIds;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failedCount;
}
