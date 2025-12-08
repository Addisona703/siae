package com.hngy.siae.api.media.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * 批量获取文件URL请求
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "批量获取文件URL请求")
public class BatchUrlDTO {

    @NotEmpty(message = "文件ID列表不能为空")
    @Schema(description = "文件ID列表", example = "[\"file-id-1\", \"file-id-2\"]")
    private List<String> fileIds;

    @Positive(message = "过期时间必须大于0")
    @Schema(description = "URL过期时间（秒），默认24小时", example = "86400")
    private Integer expirySeconds = 86400; // 默认24小时
}
