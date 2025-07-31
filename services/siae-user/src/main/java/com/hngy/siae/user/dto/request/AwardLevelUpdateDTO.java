package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 奖项等级更新数据传输对象
 * <p>
 * 用于奖项等级更新操作的数据传输，包含ID字段和可更新的字段。
 * ID字段必须提供，用于标识要更新的记录。
 *
 * @author KEYKB
 */
@Data
public class AwardLevelUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 奖项等级ID
     */
    @NotNull(message = "奖项等级ID不能为空")
    private Long id;

    /**
     * 奖项等级名称
     */
    @NotBlank(message = "奖项等级名称不能为空")
    @Size(max = 50, message = "奖项等级名称长度不能超过50个字符")
    private String name;
}
