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
 * 会员信息数据传输对象
 * 
 * @author KEYKB
 */
@Data
@Schema(description = "会员信息数据传输对象")
public class MemberDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 会员ID
     */
    @Schema(description = "会员ID，更新时必填", example = "1")
    @NotNull(message = "会员ID不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "101")
    @NotNull(message = "用户ID不能为空", groups = {CreateGroup.class})
    private Long userId;
    
    /**
     * 用户名（查询时使用）
     */
    @Schema(description = "用户名（查询时使用）", example = "keykb")
    private String username;
    
    /**
     * 真实姓名（查询时使用）
     */
    @Schema(description = "真实姓名（查询时使用）", example = "张三")
    private String realName;

    /**
     * 学号
     */
    @Schema(description = "学号", example = "20230001")
    @NotNull(message = "学号不能为空", groups = {CreateGroup.class})
    private String studentId;
    
    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "1")
    private Long departmentId;
    
    /**
     * 职位ID
     */
    @Schema(description = "职位ID", example = "2")
    private Long positionId;

    /**
     * 状态：1在校，2离校，3毕业
     */
    @Schema(description = "状态：1在校，2离校，3毕业", example = "1")
    private Integer status;
    
    /**
     * 入会日期
     */
    @Schema(description = "入会日期", example = "2023-09-01")
    private LocalDate joinDate;
    
    /**
     * 入会日期范围-开始（查询时使用）
     */
    @Schema(description = "入会日期范围-开始（查询时使用）", example = "2023-01-01")
    private LocalDate joinDateStart;
    
    /**
     * 入会日期范围-结束（查询时使用）
     */
    @Schema(description = "入会日期范围-结束（查询时使用）", example = "2023-12-31")
    private LocalDate joinDateEnd;
} 