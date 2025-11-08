package com.hngy.siae.media.domain.dto.sign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 签名响应
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "下载签名结果")
public class SignResponse {

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    private String fileId;

    /**
     * 签名URL
     */
    @Schema(description = "可直接下载的预签名URL")
    private String url;

    /**
     * 签名令牌（用于单次使用验证）
     */
    @Schema(description = "签名令牌，单次使用时需回传")
    private String token;

    /**
     * 过期时间
     */
    @Schema(description = "签名过期时间")
    private LocalDateTime expiresAt;

    /**
     * 是否绑定IP
     */
    @Schema(description = "是否绑定客户端IP")
    private Boolean bindIp;

    /**
     * 是否单次使用
     */
    @Schema(description = "是否为单次使用签名")
    private Boolean singleUse;

}
