package com.hngy.siae.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 班级信息视图对象
 * 
 * @author KEYKB
 */
@Data
@Schema(description = "班级信息视图对象")
public class ClassInfoVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 班级ID
     */
    @Schema(description = "班级ID", example = "1")
    private Long id;
    
    /**
     * 学院ID
     */
    @Schema(description = "学院ID", example = "1")
    private Long collegeId;
    
    /**
     * 学院名称
     */
    @Schema(description = "学院名称", example = "信息工程学院")
    private String collegeName;
    
    /**
     * 专业ID
     */
    @Schema(description = "专业ID", example = "1")
    private Long majorId;
    
    /**
     * 专业名称
     */
    @Schema(description = "专业名称", example = "移动应用开发")
    private String majorName;
    
    /**
     * 入学年份
     */
    @Schema(description = "入学年份", example = "2023")
    private Integer year;
    
    /**
     * 班号
     */
    @Schema(description = "班号", example = "1")
    private Integer classNo;
    
    /**
     * 班级名称（格式：年份+专业+班号）
     */
    @Schema(description = "班级名称（格式：年份+专业+班号）", example = "23移动应用开发1班")
    private String className;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 