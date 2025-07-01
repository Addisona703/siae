package com.hngy.siae.content.dto.request.content;

import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.content.common.enums.ContentTypeEnum;
import com.hngy.siae.content.common.enums.status.ContentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容页分页dto
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentPageDTO extends PageDTO {
    private Long categoryId;
    private List<Long> tagIds;
    private ContentTypeEnum type;
    private ContentStatusEnum status;

    public int getOffset() {
        return (super.getPage() != null && super.getPage() > 0 && super.getPageSize() != null)
                ? (super.getPage() - 1) * super.getPageSize()
                : 0;
    }
}
