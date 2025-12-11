package com.hngy.siae.api.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * AI服务获奖统计DTO
 * <p>
 * 用于AI工具函数返回的获奖统计信息，供跨服务共享使用。
 *
 * @author KEYKB
 */
@Data
@Schema(description = "AI服务获奖统计信息")
public class AwardStatistics implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "获奖总数")
    private Integer totalAwards;
    
    @Schema(description = "按奖项类型统计（类型名称 -> 数量）")
    private Map<String, Integer> byType;
    
    @Schema(description = "按奖项等级统计（等级名称 -> 数量）")
    private Map<String, Integer> byLevel;
    
    @Schema(description = "按年份统计（年份 -> 数量）")
    private Map<String, Integer> byYear;
}
