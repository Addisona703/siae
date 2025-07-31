package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 班级信息更新数据传输对象
 * <p>
 * 用于班级信息更新操作的数据传输，包含ID字段和可更新的字段。
 * ID字段必须提供，用于标识要更新的记录。
 *
 * @author KEYKB
 */
@Data
public class ClassInfoUpdateDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 班级ID
     */
    @NotNull(message = "班级ID不能为空")
    private Long id;
    
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
