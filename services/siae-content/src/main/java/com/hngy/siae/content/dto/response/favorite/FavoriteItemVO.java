package com.hngy.siae.content.dto.response.favorite;

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
 * 收藏内容响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "收藏内容响应对象")
public class FavoriteItemVO {

    // ==================== 收藏项基本信息 ====================

    @Schema(description = "收藏ID", example = "1")
    private Long id;

    @Schema(description = "收藏夹ID", example = "1")
    private Long folderId;

    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    @Schema(description = "内容ID", example = "1001")
    private Long contentId;

    @Schema(description = "收藏备注", example = "很有价值的文章，需要反复阅读")
    private String note;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "收藏时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;

    // ==================== 内容信息（与ContentVO一致） ====================

    @Schema(description = "内容标题", example = "Spring Boot 3.0 新特性详解")
    private String contentTitle;

    @Schema(description = "内容类型")
    private ContentTypeEnum contentType;

    @Schema(description = "内容描述/摘要", example = "本文详细介绍了 Spring Boot 3.0 的主要新特性...")
    private String contentDescription;

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

    @Schema(description = "内容状态")
    private ContentStatusEnum contentStatus;

    @Schema(description = "统计信息")
    private StatisticsVO statistics;

    @Schema(description = "标签名称列表", example = "[\"Java\", \"Spring\", \"后端\"]")
    private List<String> tagNames;

    @Schema(description = "内容创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime contentCreateTime;

    @Schema(description = "内容更新时间", example = "2025-11-27T10:30:00")
    private LocalDateTime contentUpdateTime;
}
