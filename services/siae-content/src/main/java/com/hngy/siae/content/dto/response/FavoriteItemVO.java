package com.hngy.siae.content.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏内容响应VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "收藏内容响应对象")
public class FavoriteItemVO {

    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "收藏夹ID")
    private Long folderId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "内容ID")
    private Long contentId;

    @Schema(description = "内容标题")
    private String contentTitle;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "内容描述")
    private String contentDescription;

    @Schema(description = "收藏备注")
    private String note;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "收藏时间")
    private LocalDateTime createTime;
}
