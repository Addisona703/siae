package com.hngy.siae.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 奖项等级响应 VO
 *
 * @author KEYKB
 */
@Data
@Schema(description = "奖项等级响应实体")
public class AwardLevelVO {

    @Schema(description = "奖项等级ID", example = "1")
    private Long id;

    @Schema(description = "奖项等级名称", example = "国家级")
    private String name;

    @Schema(description = "排序ID", example = "1")
    private Integer orderId;

    @Schema(description = "关联的奖项数量", example = "10")
    private Long refCount;
}
