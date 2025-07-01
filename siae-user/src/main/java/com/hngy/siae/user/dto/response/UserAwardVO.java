package com.hngy.siae.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户奖项响应VO")
public class UserAwardVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "奖项ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    @Schema(description = "奖项名称")
    private String awardTitle;
    
    @Schema(description = "奖项级别ID")
    private Long awardLevelId;
    
    @Schema(description = "奖项级别名称")
    private String awardLevelName;
    
    @Schema(description = "奖项类型ID")
    private Long awardTypeId;
    
    @Schema(description = "奖项类型名称")
    private String awardTypeName;
    
    @Schema(description = "颁发单位")
    private String awardedBy;
    
    @Schema(description = "获奖日期")
    private LocalDate awardedAt;
    
    @Schema(description = "证书编号")
    private String certificateNo;
    
    @Schema(description = "证书图片URL")
    private String certificateUrl;
    
    @Schema(description = "获奖描述")
    private String description;
    
    @Schema(description = "获奖分数")
    private BigDecimal score;
    
    @Schema(description = "获奖排名")
    private Integer rank;
    
    @Schema(description = "团队成员信息")
    private String teamMembers;
    
    @Schema(description = "记录创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "记录更新时间")
    private LocalDateTime updatedAt;
}