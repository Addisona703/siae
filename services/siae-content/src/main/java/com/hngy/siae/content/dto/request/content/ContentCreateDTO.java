package com.hngy.siae.content.dto.request.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hngy.siae.content.dto.request.content.detail.*;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容创建请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "内容创建请求")
public class ContentCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Schema(description = "内容标题", example = "Spring Boot 3.0 新特性详解", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "内容类型不能为空")
    @Schema(description = "内容类型：article-文章，note-笔记，question-问题，video-视频，file-文件", example = "article", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @NotBlank(message = "内容描述不能为空")
    @Size(max = 500, message = "描述长度不能超过500个字符")
    @Schema(description = "内容描述/摘要", example = "本文详细介绍了 Spring Boot 3.0 的主要新特性")
    private String description;

    @Schema(description = "封面文件ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String coverFileId;

    @NotNull(message = "上传者ID不能为空")
    @Schema(description = "上传者用户ID", example = "10001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long uploadedBy;

    @NotNull(message = "分类不能为空")
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "标签ID列表", example = "[1, 2, 3]")
    private List<Long> tagIds;

    @NotNull(message = "状态不能为空")
    @Schema(description = "内容状态：DRAFT-草稿，PENDING-待审核", example = "PENDING", requiredMode = Schema.RequiredMode.REQUIRED)
    private ContentStatusEnum status;

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
    @Schema(description = "内容详情，根据类型不同结构不同", requiredMode = Schema.RequiredMode.REQUIRED)
    private ContentDetailDTO detail;
}
