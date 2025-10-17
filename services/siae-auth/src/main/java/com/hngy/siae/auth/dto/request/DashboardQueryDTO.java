package com.hngy.siae.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 仪表盘统计查询条件DTO
 *
 * @author KEYKB
 */
@Data
@Schema(description = "仪表盘统计查询条件")
public class DashboardQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 统计天数：7天、30天、90天
     */
    @NotNull(message = "统计天数不能为空")
    @Schema(description = "统计天数（7、30、90）", example = "7", allowableValues = {"7", "30", "90"})
    private Integer days;
}