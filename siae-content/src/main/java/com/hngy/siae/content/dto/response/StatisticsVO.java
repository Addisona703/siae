package com.hngy.siae.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p></p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsVO {
    private Long contentId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
}
