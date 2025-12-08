package com.hngy.siae.content.dto.request.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hngy.siae.content.dto.request.content.detail.*;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容更新请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "内容更新请求")
public class ContentUpdateDTO {

    @NotNull(message = "内容ID不能为空")
    @Schema(description = "内容ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Schema(description = "内容标题", example = "Spring Boot 3.0 新特性详解（更新版）")
    private String title;

    @Schema(description = "内容类型：article-文章，note-笔记，question-问题，video-视频，file-文件", example = "article")
    private String type;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    @Schema(description = "内容描述/摘要", example = "更新后的内容描述")
    private String description;

    @Schema(description = "封面文件ID", example = "100")
    private String coverFileId;

    @Schema(description = "内容状态：DRAFT-草稿，PENDING-待审核", example = "PENDING")
    private ContentStatusEnum status;

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "标签ID列表", example = "[1, 2, 3]")
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
    @Schema(description = "内容详情，根据类型不同结构不同")
    private ContentDetailDTO detail;
}
