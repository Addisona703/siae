package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 专业创建DTO
 *
 * @author KEYKB
 */
@Data
public class MajorCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 专业名称
     */
    @NotBlank(message = "专业名称不能为空")
    @Size(max = 64, message = "专业名称长度不能超过64个字符")
    private String name;

    /**
     * 专业代码
     */
    @Size(max = 32, message = "专业代码长度不能超过32个字符")
    private String code;

    /**
     * 专业简称
     */
    @Size(max = 32, message = "专业简称长度不能超过32个字符")
    private String abbr;

    /**
     * 所属学院
     */
    @Size(max = 64, message = "所属学院长度不能超过64个字符")
    private String collegeName;
}
