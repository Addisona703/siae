package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 班级用户关联创建数据传输对象
 * <p>
 * 用于添加用户到班级操作的数据传输，包含创建时必需的字段和相应的校验注解。
 * 不包含ID字段，因为创建时ID由系统自动生成。
 *
 * @author KEYKB
 */
@Data
public class ClassUserCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 班级ID
     */
    @NotNull(message = "班级ID不能为空")
    private Long classId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 成员类型：0非协会成员，1预备成员，2正式成员
     */
    @Min(value = 0, message = "成员类型值不能小于0")
    @Max(value = 2, message = "成员类型值不能大于2")
    private Integer memberType = 0;

    /**
     * 状态：1在读，2转班，3毕业
     */
    @Min(value = 1, message = "状态值不能小于1")
    @Max(value = 3, message = "状态值不能大于3")
    private Integer status = 1;
}
