package com.hngy.siae.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 仪表盘统计结果VO
 *
 * @author KEYKB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仪表盘统计结果")
public class DashboardStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 统计天数
     */
    @Schema(description = "统计天数", example = "7")
    private Integer days;

    /**
     * 总登录人数
     */
    @Schema(description = "总登录人数", example = "1050")
    private Long totalLoginCount;

    /**
     * 总注册人数
     */
    @Schema(description = "总注册人数", example = "140")
    private Long totalRegisterCount;

    /**
     * 平均日活量
     */
    @Schema(description = "平均日活量", example = "150")
    private Long avgDailyLoginCount;

    /**
     * 每日统计数据列表
     */
    @Schema(description = "每日统计数据列表")
    private List<DailyStatsVO> dailyStats;
}