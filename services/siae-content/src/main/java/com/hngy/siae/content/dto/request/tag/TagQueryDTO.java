package com.hngy.siae.content.dto.request.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签查询请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "标签查询请求")
public class TagQueryDTO {

    @Schema(description = "标签名称，支持模糊查询", example = "Java")
    private String name;

    @Schema(description = "创建时间范围-开始时间，格式：yyyy-MM-dd HH:mm:ss", example = "2025-01-01 00:00:00")
    private String createdAtStart;

    @Schema(description = "创建时间范围-结束时间，格式：yyyy-MM-dd HH:mm:ss", example = "2025-12-31 23:59:59")
    private String createdAtEnd;
}
