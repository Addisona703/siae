package com.hngy.siae.content.dto.response.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容查询结果 VO
 * 用于 Mapper 查询结果映射，包含内容基本信息、分类、统计信息和标签
 * 
 * 注意：此类用于内部数据传输，从 ContentDetailDTO 重命名以符合 VO 命名规范
 * Requirements: 3.4 - response 包中不存在 DTO 后缀的类
 *
 * @author KEYKB
 */
@Data
@Schema(description = "内容查询结果响应对象")
public class ContentQueryResultVO {

    @Schema(description = "内容ID", example = "1")
    private Long id;

    @Schema(description = "内容标题", example = "Spring Boot 3.0 新特性详解")
    private String title;

    @Schema(description = "内容类型：1-文章，2-笔记，3-问题，4-视频，5-文件", example = "1")
    private Integer type;

    @Schema(description = "内容描述", example = "本文详细介绍了 Spring Boot 3.0 的主要新特性")
    private String description;

    @Schema(description = "封面文件ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String coverFileId;

    @Schema(description = "上传者ID", example = "10001")
    private Long uploadedBy;

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "内容状态：0-草稿，1-待审核，2-已发布，3-已删除，4-回收站", example = "2")
    private Integer status;

    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-11-27T10:30:00")
    private LocalDateTime updateTime;

    // 分类信息
    @Schema(description = "分类名称", example = "技术文章")
    private String categoryName;

    // 统计信息
    @Schema(description = "浏览次数", example = "1000")
    private Integer viewCount;

    @Schema(description = "点赞次数", example = "100")
    private Integer likeCount;

    @Schema(description = "收藏次数", example = "50")
    private Integer favoriteCount;

    @Schema(description = "评论次数", example = "20")
    private Integer commentCount;

    // 标签ID列表（逗号分隔的字符串，需要在Service层转换）
    @Schema(description = "标签ID列表（逗号分隔）", example = "1,2,3")
    private String tagIdsStr;

    // 标签名称列表（逗号分隔的字符串，需要在Service层转换）
    @Schema(description = "标签名称列表（逗号分隔）", example = "Java,Spring,后端")
    private String tagNamesStr;
}
