package com.hngy.siae.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户奖项查询条件DTO
 * 用于封装查询条件参数，所有字段均可选
 *
 * @author KEYKB
 */
@Data
@Schema(description = "用户奖项查询条件DTO")
public class UserAwardQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名（用于模糊查询）")
    private String username;

    @Schema(description = "真实姓名（用于模糊查询）")
    private String realName;

    @Schema(description = "奖项级别ID")
    private Long awardLevelId;

    @Schema(description = "奖项类型ID")
    private Long awardTypeId;

    @Schema(description = "奖项名称（用于模糊查询）")
    private String awardTitle;

    @Schema(description = "颁发单位（用于模糊查询）")
    private String awardedBy;

    @Schema(description = "获奖日期范围-开始")
    private LocalDate awardDateStart;

    @Schema(description = "获奖日期范围-结束")
    private LocalDate awardDateEnd;

    @Schema(description = "是否包含已删除的奖项", defaultValue = "false")
    private Boolean isDeleted = false;

    @Schema(description = "排序字段名", defaultValue = "awardedAt")
    private String orderByField = "awardedAt";

    @Pattern(regexp = "asc|desc", flags = Pattern.Flag.CASE_INSENSITIVE, message = "排序方向只能是asc或desc")
    @Schema(description = "排序方向，允许值：'asc'（升序）或 'desc'（降序）", defaultValue = "desc", allowableValues = {"asc", "desc"})
    private String orderDirection = "desc";
}