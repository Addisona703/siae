package com.hngy.siae.api.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI服务成员信息DTO
 * <p>
 * 用于AI工具函数返回的成员信息，供跨服务共享使用。
 *
 * @author KEYKB
 */
@Data
@Schema(description = "AI服务成员信息")
public class MemberInfo implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "成员姓名")
    private String name;
    
    @Schema(description = "学号")
    private String studentId;
    
    @Schema(description = "部门名称")
    private String department;
    
    @Schema(description = "职位名称")
    private String position;
    
    @Schema(description = "年级（入学年份）")
    private String grade;
    
    @Schema(description = "专业名称")
    private String major;
}
