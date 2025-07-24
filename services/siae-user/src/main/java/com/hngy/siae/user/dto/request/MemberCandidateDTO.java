package com.hngy.siae.user.dto.request;

import com.hngy.siae.core.validation.CreateGroup;
import com.hngy.siae.core.validation.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 会员候选人数据传输对象
 * 
 * @author KEYKB
 */
@Data
@Schema(description = "会员候选人请求数据传输对象")
public class MemberCandidateDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "候选人ID，更新时必填", example = "1")
    @NotNull(message = "候选人ID不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    @Schema(description = "用户ID，创建时必填", example = "101")
    @NotNull(message = "用户ID不能为空", groups = {CreateGroup.class})
    private Long userId;
    
    @Schema(description = "用户名（查询时使用）", example = "zhangsan")
    private String username;
    
    @Schema(description = "真实姓名（查询时使用）", example = "张三")
    private String realName;

    @Schema(description = "学号", example = "20230001")
    @NotNull(message = "学号不能为空", groups = {CreateGroup.class})
    private String studentId;
    
    @Schema(description = "申请部门ID", example = "1")
    private Long departmentId;
    
    @Schema(description = "申请状态：0待审核，1通过，2拒绝", example = "0")
    private Integer status;

    @Schema(description = "申请日期范围-开始（查询时使用）", example = "2023-01-01")
    private LocalDate applyDateStart;
    
    @Schema(description = "申请日期范围-结束（查询时使用）", example = "2023-12-31")
    private LocalDate applyDateEnd;
    
    @Schema(description = "是否包含已删除的候选人（查询时使用）", example = "false")
    private Boolean includeDeleted = false;
}