package com.hngy.siae.content.dto.response.content.detail;

import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.enums.status.QuestionStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 问题详情响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "问题详情响应对象")
public class QuestionVO implements ContentDetailVO {

    @Schema(description = "问题详情ID", example = "1")
    private Long id;

    @Schema(description = "关联内容ID", example = "1001")
    private Long contentId;

    @Schema(description = "问题内容", example = "如何在 Spring Boot 中配置多数据源？")
    private String content;

    @Schema(description = "回答数量", example = "5")
    private Integer answerCount;

    @Schema(description = "问题状态：是否已解决")
    private QuestionStatusEnum solved;

    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-11-27T10:30:00")
    private LocalDateTime updateTime;
}
