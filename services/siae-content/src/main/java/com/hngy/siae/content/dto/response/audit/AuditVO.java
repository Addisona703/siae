package com.hngy.siae.content.dto.response.audit;

import com.hngy.siae.content.enums.status.AuditStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审核信息响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "审核信息响应对象")
public class AuditVO {

    @Schema(description = "审核记录ID", example = "1")
    private Long id;

    @Schema(description = "审核目标ID", example = "1001")
    private Long targetId;

    @Schema(description = "目标类型：CONTENT-内容，COMMENT-评论", example = "CONTENT")
    private String targetType;

    @Schema(description = "审核状态")
    private AuditStatusEnum auditStatus;

    @Schema(description = "审核原因/备注", example = "内容符合社区规范，审核通过")
    private String auditReason;

    @Schema(description = "审核人ID", example = "10001")
    private Long auditBy;

    @Schema(description = "审核人昵称")
    private String auditByName;

    @Schema(description = "版本号（乐观锁）", example = "1")
    private Integer version;

    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;

    // ========== 关联信息 ==========

    @Schema(description = "内容标题（targetType=CONTENT时有值）")
    private String contentTitle;

    @Schema(description = "内容描述（targetType=CONTENT时有值）")
    private String contentDescription;

    @Schema(description = "内容作者ID（targetType=CONTENT时有值）")
    private Long contentAuthorId;

    @Schema(description = "内容作者昵称")
    private String contentAuthorName;

    @Schema(description = "内容类型：0文章,1笔记,2提问,3文件,4视频")
    private Integer contentType;

    @Schema(description = "内容封面文件ID")
    private String contentCoverFileId;

    @Schema(description = "内容封面URL")
    private String contentCoverUrl;

    @Schema(description = "评论内容（targetType=COMMENT时有值）")
    private String commentContent;

    @Schema(description = "评论用户ID（targetType=COMMENT时有值）")
    private Long commentUserId;

    @Schema(description = "评论用户昵称")
    private String commentUserName;

    @Schema(description = "评论所属内容ID")
    private Long commentContentId;

    @Schema(description = "评论所属内容标题")
    private String commentContentTitle;
}
