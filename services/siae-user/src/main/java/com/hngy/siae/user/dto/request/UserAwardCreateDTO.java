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
 * 用户获奖记录创建数据传输对象
 * <p>
 * 用于用户获奖记录创建操作的数据传输，包含创建时必需的字段和相应的校验注解。
 * 不包含ID字段，因为创建时ID由系统自动生成。
 * <p>
 * 团队获奖只需创建一条记录，所有成员ID存储在 teamMembers 数组中
 *
 * @author KEYKB
 */
@Data
public class UserAwardCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 获奖描述（选填）
     */
    private String description;

    /**
     * 证书文件ID
     */
    @Size(max = 64, message = "证书文件ID长度不能超过64个字符")
    private String certificateFileId;

    /**
     * 团队成员ID列表（包括个人或团队所有成员）
     * 个人获奖：只包含一个用户ID，如 [1]
     * 团队获奖：包含所有成员ID，如 [1,2,3,5]
     */
    @NotNull(message = "团队成员列表不能为空")
    private java.util.List<Long> teamMembers;
}