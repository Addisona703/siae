package com.hngy.siae.media.domain.dto.sign;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 签名请求
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "下载签名申请参数")
public class SignRequest {

    @Schema(description = "待下载文件ID")
    @NotBlank(message = "文件ID不能为空")
    private String fileId;

    /**
     * 签名有效期（秒），默认3600秒（1小时）
     */
    @Schema(description = "签名有效期，单位秒，默认3600，范围60~86400")
    @Min(value = 60, message = "有效期不能少于60秒")
    @Max(value = 86400, message = "有效期不能超过86400秒（24小时）")
    private Integer expirySeconds = 3600;

    /**
     * 是否绑定IP
     */
    @Schema(description = "是否绑定请求客户端IP")
    private Boolean bindIp = false;

    /**
     * 是否单次使用
     */
    @Schema(description = "是否限制单次使用，true 时校验 token")
    private Boolean singleUse = false;

    /**
     * 下载文件名（可选，用于Content-Disposition）
     */
    @Schema(description = "期望的下载文件名，用于Content-Disposition")
    private String filename;

}
