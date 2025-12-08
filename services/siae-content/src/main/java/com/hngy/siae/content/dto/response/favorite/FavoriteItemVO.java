package com.hngy.siae.content.dto.response.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏内容响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "收藏内容响应对象")
public class FavoriteItemVO {

    @Schema(description = "收藏ID", example = "1")
    private Long id;

    @Schema(description = "收藏夹ID", example = "1")
    private Long folderId;

    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    @Schema(description = "内容ID", example = "1001")
    private Long contentId;

    @Schema(description = "内容标题", example = "Spring Boot 3.0 新特性详解")
    private String contentTitle;

    @Schema(description = "内容类型", example = "article")
    private String contentType;

    @Schema(description = "内容描述", example = "本文详细介绍了 Spring Boot 3.0 的主要新特性...")
    private String contentDescription;

    @Schema(description = "收藏备注", example = "很有价值的文章，需要反复阅读")
    private String note;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "收藏时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;
}
