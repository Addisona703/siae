package com.hngy.siae.user.dto.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 班级信息查询数据传输对象
 * <p>
 * 用于班级信息分页查询和条件查询的数据传输，包含查询条件字段。
 * 所有字段都是可选的，不需要校验注解，因为查询条件通常允许为空。
 *
 * @author KEYKB
 */
@Data
public class ClassInfoQueryDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 班级ID
     */
    private Long id;
    
    /**
     * 关联学院ID
     */
    private Long collegeId;
    
    /**
     * 关联专业ID
     */
    private Long majorId;
    
    /**
     * 入学年份
     */
    private Integer year;
    
    /**
     * 班号
     */
    private Integer classNo;
}
