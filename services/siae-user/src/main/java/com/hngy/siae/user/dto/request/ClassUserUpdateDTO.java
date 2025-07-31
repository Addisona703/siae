package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 班级用户关联更新数据传输对象
 * <p>
 * 用于更新用户班级关联信息操作的数据传输，包含ID字段和可更新的字段。
 * ID字段必须提供，用于标识要更新的记录。
 *
 * @author KEYKB
 */
@Data
public class ClassUserUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联记录ID
     */
    @NotNull(message = "关联记录ID不能为空")
    private Long id;

    /**
     * 成员类型：0非协会成员，1预备成员，2正式成员
     */
    @NotNull(message = "成员类型不能为空")
    @Min(value = 0, message = "成员类型值不能小于0")
    @Max(value = 2, message = "成员类型值不能大于2")
    private Integer memberType;

    /**
     * 状态：1在读，2转班，3毕业
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态值不能小于1")
    @Max(value = 3, message = "状态值不能大于3")
    private Integer status;
}
