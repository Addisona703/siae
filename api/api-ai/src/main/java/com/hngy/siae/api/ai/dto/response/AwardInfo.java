package com.hngy.siae.api.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * AI服务获奖信息DTO
 * <p>
 * 用于AI工具函数返回的获奖信息，供跨服务共享使用。
 *
 * @author KEYKB
 */
@Data
@Schema(description = "AI服务获奖信息")
public class AwardInfo implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "获奖记录ID")
    private Long id;
    
    @Schema(description = "奖项名称")
    private String awardTitle;
    
    @Schema(description = "奖项等级名称")
    private String awardLevel;
    
    @Schema(description = "奖项类型名称")
    private String awardType;
    
    @Schema(description = "颁发单位")
    private String awardedBy;
    
    @Schema(description = "获奖日期")
    private LocalDate awardedAt;
    
    @Schema(description = "获奖描述")
    private String description;
    
    @Schema(description = "团队成员姓名列表")
    private List<String> teamMembers;
}
