package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 职位创建DTO
 *
 * @author KEYKB
 */
@Data
public class PositionCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 职位名称
     */
    @NotBlank(message = "职位名称不能为空")
    @Size(max = 64, message = "职位名称长度不能超过64个字符")
    private String name;

    /**
     * 排序ID
     */
    private Integer orderId;
}
