package com.hngy.siae.api.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * AI服务成员统计DTO
 * <p>
 * 用于AI工具函数返回的成员统计信息，供跨服务共享使用。
 *
 * @author KEYKB
 */
@Data
@Schema(description = "AI服务成员统计信息")
public class MemberStatistics implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "成员总数")
    private Integer totalMembers;
    
    @Schema(description = "按部门统计（部门名称 -> 数量）")
    private Map<String, Integer> byDepartment;
    
    @Schema(description = "按年级统计（年级 -> 数量）")
    private Map<String, Integer> byGrade;
    
    @Schema(description = "按职位统计（职位名称 -> 数量）")
    private Map<String, Integer> byPosition;
}
