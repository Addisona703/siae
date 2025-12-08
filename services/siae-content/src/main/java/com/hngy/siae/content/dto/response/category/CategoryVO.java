package com.hngy.siae.content.dto.response.category;

import com.hngy.siae.content.enums.status.CategoryStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分类信息响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分类信息响应对象")
public class CategoryVO {

    @Schema(description = "分类ID", example = "1")
    private Long id;

    @Schema(description = "分类名称", example = "技术文章")
    private String name;

    @Schema(description = "分类编码", example = "tech_article")
    private String code;

    @Schema(description = "父分类ID", example = "0")
    private Long parentId;

    @Schema(description = "分类状态")
    private CategoryStatusEnum status;

    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-11-27T10:30:00")
    private LocalDateTime updateTime;
}
