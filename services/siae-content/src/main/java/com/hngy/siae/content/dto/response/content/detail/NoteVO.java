package com.hngy.siae.content.dto.response.content.detail;

import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记详情响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "笔记详情响应对象")
public class NoteVO implements ContentDetailVO {

    @Schema(description = "笔记详情ID", example = "1")
    private Long id;

    @Schema(description = "关联内容ID", example = "1001")
    private Long contentId;

    @Schema(description = "笔记内容", example = "今天学习了 Spring Boot 的自动配置原理...")
    private String content;

    @Schema(description = "内容格式：markdown/html/plain", example = "markdown")
    private String format;

    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-11-27T10:30:00")
    private LocalDateTime updateTime;
}
