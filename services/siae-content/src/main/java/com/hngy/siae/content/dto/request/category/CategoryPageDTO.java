package com.hngy.siae.content.dto.request.category;

import com.hngy.siae.content.common.enums.status.CategoryStatusEnum;
import com.hngy.siae.core.dto.PageDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryPageDTO extends PageDTO<Object> {
    private CategoryStatusEnum status;
    private Long parentId;
}
