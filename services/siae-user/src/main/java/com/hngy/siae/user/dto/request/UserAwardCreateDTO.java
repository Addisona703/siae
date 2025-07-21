package com.hngy.siae.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(description = "用户奖项创建请求DTO")
public class UserAwardCreateDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    
    @NotBlank(message = "奖项名称不能为空")
    @Schema(description = "奖项名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String awardTitle;
    
    @NotNull(message = "奖项级别ID不能为空")
    @Schema(description = "奖项级别ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long awardLevelId;
    
    @NotNull(message = "奖项类型ID不能为空")
    @Schema(description = "奖项类型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long awardTypeId;
    
    @NotBlank(message = "颁发单位不能为空")
    @Schema(description = "颁发单位", requiredMode = Schema.RequiredMode.REQUIRED)
    private String awardedBy;
    
    @NotNull(message = "获奖日期不能为空")
    @Schema(description = "获奖日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate awardedAt;

    @Schema(description = "证书图片URL")
    private String certificateUrl;
    
    @Schema(description = "获奖描述（选填）")
    private String description;
    
    @Schema(description = "团队成员信息")
    private String teamMembers;
} 