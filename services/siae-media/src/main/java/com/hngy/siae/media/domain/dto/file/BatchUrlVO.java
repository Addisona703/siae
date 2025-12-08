package com.hngy.siae.media.domain.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 批量获取文件URL响应
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量获取文件URL响应")
public class BatchUrlVO {

    @Schema(description = "文件ID到URL的映射", example = "{\"file-id-1\": \"https://...\", \"file-id-2\": \"https://...\"}")
    private Map<String, String> urls;

    @Schema(description = "URL过期时间")
    private LocalDateTime expiresAt;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failedCount;

}
