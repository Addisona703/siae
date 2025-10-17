package com.hngy.siae.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 每日统计数据VO
 *
 * @author KEYKB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "每日统计数据")
public class DailyStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日期（格式：yyyy-MM-dd）
     */
    @Schema(description = "日期", example = "2025-10-01")
    private String date;

    /**
     * 登录人数（日活量）
     */
    @Schema(description = "登录人数", example = "150")
    private Long loginCount;

    /**
     * 注册人数
     */
    @Schema(description = "注册人数", example = "20")
    private Long registerCount;
}