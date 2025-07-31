package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 班级信息创建数据传输对象
 * <p>
 * 用于班级信息创建操作的数据传输，包含创建时必需的字段和相应的校验注解。
 * 不包含ID字段，因为创建时ID由系统自动生成。
 *
 * @author KEYKB
 */
@Data
public class ClassInfoCreateDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 关联学院ID
     */
    @NotNull(message = "学院ID不能为空")
    private Long collegeId;
    
    /**
     * 关联专业ID
     */
    @NotNull(message = "专业ID不能为空")
    private Long majorId;
    
    /**
     * 入学年份
     */
    @NotNull(message = "入学年份不能为空")
    @Min(value = 2000, message = "入学年份不能小于2000")
    private Integer year;
    
    /**
     * 班号
     */
    @NotNull(message = "班号不能为空")
    @Min(value = 1, message = "班号不能小于1")
    private Integer classNo;
}
