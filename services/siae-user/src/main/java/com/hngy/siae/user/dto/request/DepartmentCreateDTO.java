package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门创建DTO
 *
 * @author KEYKB
 */
@Data
public class DepartmentCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 64, message = "部门名称长度不能超过64个字符")
    private String name;

    /**
     * 排序ID
     */
    private Integer orderId;
}
