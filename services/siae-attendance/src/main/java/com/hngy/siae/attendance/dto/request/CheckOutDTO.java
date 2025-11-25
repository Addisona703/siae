package com.hngy.siae.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签退请求DTO
 *
 * @author SIAE Team
 */
@Data
public class CheckOutDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 签退时间
     */
    @NotNull(message = "签退时间不能为空")
    private LocalDateTime timestamp;

    /**
     * 签退地点
     */
    private String location;
}
