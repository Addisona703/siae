package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 正式成员更新数据传输对象
 * <p>
 * 用于正式成员更新操作的数据传输，包含ID字段和可更新的字段。
 * ID字段必须提供，用于标识要更新的记录。
 *
 * @author KEYKB
 */
@Data
public class MemberUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 正式成员ID
     */
    @NotNull(message = "正式成员ID不能为空")
    private Long id;

    /**
     * 学号
     */
    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过20个字符")
    private String studentId;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 职位ID
     */
    private Long positionId;

    /**
     * 状态：1在校，2离校，3毕业
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 入会日期
     */
    private LocalDate joinDate;
}
