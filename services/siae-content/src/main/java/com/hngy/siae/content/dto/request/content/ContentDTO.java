package com.hngy.siae.content.dto.request.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hngy.siae.content.common.enums.status.ContentStatusEnum;
import com.hngy.siae.content.dto.request.content.detail.*;
import com.hngy.siae.core.validation.CreateGroup;
import com.hngy.siae.core.validation.UpdateGroup;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容dto
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentDTO {
    @NotNull(groups = UpdateGroup.class)
    private Long id;
    @NotNull(groups = CreateGroup.class)
    private String title;
    @NotNull(groups = CreateGroup.class)
    private String type;
    private String description;
    private ContentStatusEnum status;
    @NotNull(groups = CreateGroup.class)
    private Long uploadedBy;
    private Long categoryId;
    private List<Long> tagIds;
    @NotNull(groups = CreateGroup.class)
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
