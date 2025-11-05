package com.hngy.siae.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员信息视图对象
 *
 * @author KEYKB
 */
@Data
@Schema(description = "正式成员响应视图对象")
public class MemberVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会员ID
     */
    @Schema(description = "成员ID", example = "1")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "101")
    private Long userId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 职位名称
     */
    @Schema(description = "职位名称", example = "部长")
    private String positionName;

    /**
     * 状态：0禁用，1启用
     */
    @Schema(description = "状态：1在校，2离校，3毕业", example = "1")
    private Integer status;

    /**
     * 状态名称
     */
    @Schema(description = "状态名称", example = "在校")
    private String statusName;

    /**
     * 创建（入会）时间
     */
    @Schema(description = "入会日期", example = "2023-09-01")
    private LocalDate joinDate;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-09-01T10:00:00")
    private LocalDateTime createAt;

    /**
     * 更新（如果status = 0 表示离会）时间
     */
    @Schema(description = "更新时间", example = "2023-09-01T10:00:00")
    private LocalDateTime updateAt;
}
