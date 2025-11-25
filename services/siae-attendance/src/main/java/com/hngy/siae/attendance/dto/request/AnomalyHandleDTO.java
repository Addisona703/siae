package com.hngy.siae.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 考勤异常处理DTO
 *
 * @author SIAE Team
 */
@Data
public class AnomalyHandleDTO {

    /**
     * 处理说明
     */
    private String handlerNote;

    /**
     * 是否已解决
     */
    @NotNull(message = "处理结果不能为空")
    private Boolean resolved;
}
