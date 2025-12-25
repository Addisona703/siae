package com.hngy.siae.content.dto.response.content;

import com.hngy.siae.content.dto.response.statistics.StatisticsVO;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容信息响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "内容信息响应对象")
public class ContentVO<T extends ContentDetailVO> {

    @Schema(description = "内容ID", example = "1001")
    private Long id;

    @Schema(description = "内容标题", example = "Spring Boot 3.0 新特性详解")
    private String title;

    @Schema(description = "内容类型")
    private ContentTypeEnum type;

    @Schema(description = "内容描述/摘要", example = "本文详细介绍了 Spring Boot 3.0 的主要新特性...")
    private String description;

    @Schema(description = "封面文件ID（UUID字符串）", example = "550e8400-e29b-41d4-a716-446655440000")
    private String coverFileId;

    @Schema(description = "封面访问URL", example = "https://cdn.example.com/covers/spring-boot.jpg")
    private String coverUrl;

    @Schema(description = "上传者ID", example = "10001")
    private Long uploadedBy;

    @Schema(description = "作者昵称", example = "技术达人")
    private String authorNickname;

    @Schema(description = "作者头像URL", example = "https://cdn.example.com/avatars/user.jpg")
    private String authorAvatarUrl;

    @Schema(description = "分类名称", example = "技术文章")
    private String categoryName;

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "内容状态")
    private ContentStatusEnum status;

    @Schema(description = "统计信息")
    private StatisticsVO statistics;

    @Schema(description = "标签名称列表", example = "[\"Java\", \"Spring\", \"后端\"]")
    private List<String> tagNames;

    @Schema(description = "标签ID列表", example = "[1, 2, 3]")
    private List<Long> tagIds;

    @Schema(description = "内容详情，根据类型不同结构不同")
    private T detail;

    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-11-27T10:30:00")
    private LocalDateTime updateTime;
}
