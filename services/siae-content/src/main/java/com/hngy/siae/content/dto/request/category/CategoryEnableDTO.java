package com.hngy.siae.content.dto.request.category;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 分类禁用/启用dto
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
@Data
public class CategoryEnableDTO {
    @NotNull
    private Long id;
    @NotNull
    private Boolean enable;
}
