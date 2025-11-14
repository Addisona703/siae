package com.hngy.siae.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 内容详情DTO（用于XML联表查询结果映射）
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentDetailDTO {
    
    // 内容基本信息
    private Long id;
    private String title;
    private Integer type;
    private String description;
    private Long uploadedBy;
    private Long categoryId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 分类信息
    private String categoryName;
    
    // 统计信息
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    
    // 标签ID列表（逗号分隔的字符串）
    private String tagIdsStr;
}
