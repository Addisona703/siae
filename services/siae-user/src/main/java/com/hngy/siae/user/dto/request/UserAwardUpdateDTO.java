package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 用户获奖记录更新数据传输对象
 * <p>
 * 用于用户获奖记录更新操作的数据传输。
 * ID 字段可选，由 Controller 从路径参数设置。
 *
 * @author KEYKB
 */
@Data
public class UserAwardUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 获奖记录ID（可选，由Controller从路径参数设置）
     */
    private Long id;

    /**
     * 奖项名称
     */
    @NotBlank(message = "奖项名称不能为空")
    @Size(max = 255, message = "奖项名称长度不能超过255个字符")
    private String awardTitle;

    /**
     * 奖项级别ID
     */
    @NotNull(message = "奖项级别ID不能为空")
    private Long awardLevelId;

    /**
     * 奖项类型ID
     */
    @NotNull(message = "奖项类型ID不能为空")
    private Long awardTypeId;

    /**
     * 颁发单位
     */
    @NotBlank(message = "颁发单位不能为空")
    @Size(max = 255, message = "颁发单位名称长度不能超过255个字符")
    private String awardedBy;

    /**
     * 获奖日期
     */
    @NotNull(message = "获奖日期不能为空")
    private LocalDate awardedAt;

    /**
     * 获奖描述
     */
    private String description;

    /**
     * 证书文件ID
     */
    @Size(max = 64, message = "证书文件ID长度不能超过64个字符")
    private String certificateFileId;

    /**
     * 团队成员ID列表（包括个人或团队所有成员）
     */
    @NotNull(message = "团队成员列表不能为空")
    private java.util.List<Long> teamMembers;
}