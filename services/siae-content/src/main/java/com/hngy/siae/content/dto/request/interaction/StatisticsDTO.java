package com.hngy.siae.content.dto.request.interaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计数据请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "统计数据请求")
public class StatisticsDTO {

    @Min(value = 0, message = "浏览次数不能为负数")
    @Schema(description = "浏览次数", example = "100")
    private Integer viewCount;

    @Min(value = 0, message = "点赞次数不能为负数")
    @Schema(description = "点赞次数", example = "50")
    private Integer likeCount;

    @Min(value = 0, message = "收藏次数不能为负数")
    @Schema(description = "收藏次数", example = "20")
    private Integer favoriteCount;

    @Min(value = 0, message = "评论次数不能为负数")
    @Schema(description = "评论次数", example = "10")
    private Integer commentCount;
}
