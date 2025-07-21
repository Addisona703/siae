package com.hngy.siae.user.dto.request;

import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.common.validation.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 班级信息数据传输对象
 * 
 * @author KEYKB
 */
@Data
@Schema(description = "班级信息请求体")
public class ClassInfoDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 班级ID
     */
    @Schema(description = "班级ID，更新时必填", example = "1")
    @NotNull(message = "班级ID不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 关联学院ID
     */
    @Schema(description = "关联学院ID", example = "1", required = true)
    @NotNull(message = "学院ID不能为空", groups = {CreateGroup.class})
    private Long collegeId;
    
    /**
     * 关联专业ID
     */
    @Schema(description = "关联专业ID", example = "1", required = true)
    @NotNull(message = "专业ID不能为空", groups = {CreateGroup.class})
    private Long majorId;
    
    /**
     * 入学年份
     */
    @Schema(description = "入学年份", example = "2023", required = true)
    @NotNull(message = "入学年份不能为空", groups = {CreateGroup.class})
    @Min(value = 2000, message = "入学年份不能小于2000", groups = {CreateGroup.class})
    private Integer year;
    
    /**
     * 班号
     */
    @Schema(description = "班号", example = "1", required = true)
    @NotNull(message = "班号不能为空", groups = {CreateGroup.class})
    @Min(value = 1, message = "班号不能小于1", groups = {CreateGroup.class})
    private Integer classNo;
    
    /**
     * 是否包含已删除的班级（查询时使用）
     */
    @Schema(description = "是否包含已删除的班级（查询时使用）", example = "false", defaultValue = "false")
    private Boolean includeDeleted = false;
} 