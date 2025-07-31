package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 奖项等级创建数据传输对象
 * <p>
 * 用于奖项等级创建操作的数据传输，包含创建时必需的字段和相应的校验注解。
 * 不包含ID字段，因为创建时ID由系统自动生成。
 *
 * @author KEYKB
 */
@Data
public class AwardLevelCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 奖项等级名称
     */
    @NotBlank(message = "奖项等级名称不能为空")
    @Size(max = 50, message = "奖项等级名称长度不能超过50个字符")
    private String name;
}
