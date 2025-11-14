package com.hngy.siae.content.dto.request.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hngy.siae.content.dto.request.content.detail.*;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容更新DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentUpdateDTO {
    @NotNull(message = "内容ID不能为空")
    private Long id;
    
    private String title;
    
    private String type;
    
    private String description;
    
    private ContentStatusEnum status;
    
    private Long categoryId;
    
    private List<Long> tagIds;
    
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ArticleDetailDTO.class, name = "article"),
            @JsonSubTypes.Type(value = NoteDetailDTO.class, name = "note"),
            @JsonSubTypes.Type(value = QuestionDetailDTO.class, name = "question"),
            @JsonSubTypes.Type(value = FileDetailDTO.class, name = "file"),
            @JsonSubTypes.Type(value = VideoDetailDTO.class, name = "video")
    })
    private ContentDetailDTO detail;
}
