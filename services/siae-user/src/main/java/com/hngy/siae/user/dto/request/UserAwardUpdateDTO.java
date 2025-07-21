package com.hngy.siae.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(description = "用户奖项更新请求DTO")
public class UserAwardUpdateDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @NotNull(message = "奖项ID不能为空")
    @Schema(description = "奖项ID，更新时必须提供", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
    
    @Schema(description = "奖项名称")
    private String awardTitle;
    
    @Schema(description = "奖项级别ID")
    private Long awardLevelId;
    
    @Schema(description = "奖项类型ID")
    private Long awardTypeId;
    
    @Schema(description = "颁发单位")
    private String awardedBy;
    
    @Schema(description = "获奖日期")
    private LocalDate awardedAt;
    
    @Schema(description = "证书图片URL")
    private String certificateUrl;
    
    @Schema(description = "获奖描述")
    private String description;
    
    @Schema(description = "团队成员信息")
    private String teamMembers;
}