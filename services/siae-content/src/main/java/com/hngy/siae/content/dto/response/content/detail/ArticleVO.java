package com.hngy.siae.content.dto.response.content.detail;

import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章详情响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "文章详情响应对象")
public class ArticleVO implements ContentDetailVO {

    @Schema(description = "文章详情ID", example = "1")
    private Long id;

    @Schema(description = "关联内容ID", example = "1001")
    private Long contentId;

    @Schema(description = "文章正文内容", example = "<p>Spring Boot 3.0 带来了许多令人兴奋的新特性...</p>")
    private String content;

    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-11-27T10:30:00")
    private LocalDateTime updateTime;
}
