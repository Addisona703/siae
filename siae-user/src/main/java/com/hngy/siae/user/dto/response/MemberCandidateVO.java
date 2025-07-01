package com.hngy.siae.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 候选会员信息视图对象
 * 
 * @author KEYKB
 */
@Data
@Schema(description = "会员候选人响应视图对象")
public class MemberCandidateVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "候选人ID", example = "1")
    private Long id;
    
    @Schema(description = "用户ID", example = "101")
    private Long userId;

    @Schema(description = "学号", example = "20230001")
    private String studentId;
    
    @Schema(description = "申请部门ID", example = "1")
    private Long departmentId;
    
    @Schema(description = "职位ID")
    private Long positionId;

    @Schema(description = "申请状态：0待审核，1通过，2拒绝", example = "1")
    private Integer status;
    
    @Schema(description = "状态名称", example = "通过")
    private String statusName;

    @Schema(description = "创建时间", example = "2023-10-01T10:00:00")
    private LocalDateTime createAt;
    
    @Schema(description = "更新（如果status = 0 表示离会）时间")
    private LocalDateTime updateAt;
} 