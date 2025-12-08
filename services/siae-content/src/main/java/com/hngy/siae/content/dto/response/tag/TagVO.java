package com.hngy.siae.content.dto.response.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标签信息响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "标签信息响应对象")
public class TagVO {

    @Schema(description = "标签ID", example = "1")
    private Long id;

    @Schema(description = "标签名称", example = "Spring Boot")
    private String name;

    @Schema(description = "标签描述", example = "Spring Boot 相关技术文章")
    private String description;

    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-11-27T10:30:00")
    private LocalDateTime updateTime;
}
