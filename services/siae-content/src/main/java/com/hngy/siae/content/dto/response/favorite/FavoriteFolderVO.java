package com.hngy.siae.content.dto.response.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏夹响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "收藏夹响应对象")
public class FavoriteFolderVO {

    @Schema(description = "收藏夹ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    @Schema(description = "收藏夹名称", example = "我的收藏")
    private String name;

    @Schema(description = "收藏夹描述", example = "存放技术文章的收藏夹")
    private String description;

    @Schema(description = "是否默认收藏夹：0-否，1-是", example = "1")
    private Integer isDefault;

    @Schema(description = "是否公开：0-私密，1-公开", example = "0")
    private Integer isPublic;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "收藏内容数量", example = "25")
    private Integer itemCount;

    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-11-27T10:30:00")
    private LocalDateTime updateTime;
}
