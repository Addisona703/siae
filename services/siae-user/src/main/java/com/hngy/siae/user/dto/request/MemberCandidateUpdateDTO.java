package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 候选成员更新数据传输对象
 * <p>
 * 用于候选成员更新操作的数据传输，包含ID字段和可更新的字段。
 * ID字段必须提供，用于标识要更新的记录。
 *
 * @author KEYKB
 */
@Data
public class MemberCandidateUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 候选成员ID
     */
    @NotNull(message = "候选成员ID不能为空")
    private Long id;

    /**
     * 学号
     */
    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过20个字符")
    private String studentId;

    /**
     * 申请部门ID
     */
    private Long departmentId;

    /**
     * 申请状态：0待审核，1通过，2拒绝
     */
    @NotNull(message = "申请状态不能为空")
    private Integer status;
}
