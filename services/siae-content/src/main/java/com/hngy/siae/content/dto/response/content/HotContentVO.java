package com.hngy.siae.content.dto.response.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门内容响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "热门内容响应对象")
public class HotContentVO {

    @Schema(description = "内容ID", example = "1001")
    private Long contentId;

    @Schema(description = "内容标题", example = "Spring Boot 3.0 新特性详解")
    private String title;

    @Schema(description = "内容描述/摘要", example = "本文详细介绍了 Spring Boot 3.0 的主要新特性...")
    private String description;

    @Schema(description = "封面媒体ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private Long coverMediaId;

    @Schema(description = "封面访问URL", example = "https://cdn.example.com/covers/spring-boot.jpg")
    private String coverUrl;

    @Schema(description = "浏览次数", example = "12580")
    private Integer viewCount;

    @Schema(description = "点赞次数", example = "1234")
    private Integer likeCount;

    @Schema(description = "收藏次数", example = "567")
    private Integer favoriteCount;

    @Schema(description = "评论次数", example = "89")
    private Integer commentCount;
}
