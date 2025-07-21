package com.hngy.siae.content.dto.request.content;


import com.hngy.siae.content.common.enums.ContentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentHotPageDTO{
    private Long categoryId;
    private ContentTypeEnum type;
    private String sortBy;
}
