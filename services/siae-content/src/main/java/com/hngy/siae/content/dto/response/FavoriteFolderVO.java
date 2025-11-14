package com.hngy.siae.content.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏夹响应VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "收藏夹响应对象")
public class FavoriteFolderVO {

    @Schema(description = "收藏夹ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "收藏夹名称")
    private String name;

    @Schema(description = "收藏夹描述")
    private String description;

    @Schema(description = "是否默认收藏夹")
    private Integer isDefault;

    @Schema(description = "是否公开")
    private Integer isPublic;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "收藏内容数量")
    private Integer itemCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
