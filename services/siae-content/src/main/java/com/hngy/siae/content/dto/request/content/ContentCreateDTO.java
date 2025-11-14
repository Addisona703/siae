package com.hngy.siae.content.dto.request.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hngy.siae.content.dto.request.content.detail.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容创建DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentCreateDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    
    @NotBlank(message = "内容类型不能为空")
    private String type;
    
    private String description;
    
    @NotNull(message = "上传者ID不能为空")
    private Long uploadedBy;
    
    private Long categoryId;
    
    private List<Long> tagIds;
    
    @NotNull(message = "内容详情不能为空")
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
