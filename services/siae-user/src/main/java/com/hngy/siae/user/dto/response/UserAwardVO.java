package com.hngy.siae.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户获奖记录VO
 *
 * @author KEYKB
 */
@Data
@Schema(description = "用户获奖记录响应VO")
public class UserAwardVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "获奖记录ID")
    private Long id;
    
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
    
    @Schema(description = "获奖描述")
    private String description;
    
    @Schema(description = "证书文件ID")
    private String certificateFileId;
    
    @Schema(description = "团队成员ID列表（JSON数组字符串）")
    private String teamMembers;
    
    @Schema(description = "团队成员基础信息列表")
    private List<UserVO> teamMemberList;
    
    @Schema(description = "记录创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "记录更新时间")
    private LocalDateTime updatedAt;
}