package com.hngy.siae.content.dto.request.category;

import com.hngy.siae.content.enums.status.CategoryStatusEnum;
import com.hngy.siae.core.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 分类分页查询请求DTO
 *
 * @author KEYKB
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分类分页查询请求")
public class CategoryPageDTO extends PageDTO<Object> {

    @Schema(description = "分类状态：ENABLED-启用，DISABLED-禁用", example = "ENABLED")
    private CategoryStatusEnum status;

    @Schema(description = "父分类ID，查询指定父分类下的子分类", example = "1")
    private Long parentId;
}
