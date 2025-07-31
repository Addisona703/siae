package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户获奖记录创建数据传输对象
 * <p>
 * 用于用户获奖记录创建操作的数据传输，包含创建时必需的字段和相应的校验注解。
 * 不包含ID字段，因为创建时ID由系统自动生成。
 *
 * @author KEYKB
 */
@Data
public class UserAwardCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

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
     * 证书图片URL
     */
    @Size(max = 512, message = "证书图片URL长度不能超过512个字符")
    private String certificateUrl;

    /**
     * 获奖描述（选填）
     */
    private String description;

    /**
     * 团队成员信息
     */
    private String teamMembers;
}