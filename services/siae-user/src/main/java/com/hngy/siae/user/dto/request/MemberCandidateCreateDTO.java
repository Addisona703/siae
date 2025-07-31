package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 候选成员创建数据传输对象
 * <p>
 * 用于候选成员创建操作的数据传输，包含创建时必需的字段和相应的校验注解。
 * 不包含ID字段，因为创建时ID由系统自动生成。
 *
 * @author KEYKB
 */
@Data
public class MemberCandidateCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

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
    private Integer status = 0;
}
