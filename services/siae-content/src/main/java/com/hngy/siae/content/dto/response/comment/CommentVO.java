package com.hngy.siae.content.dto.response.comment;

import com.hngy.siae.content.enums.status.CommentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论信息响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "评论信息响应对象")
public class CommentVO {

    @Schema(description = "评论ID", example = "1")
    private Long id;

    @Schema(description = "内容ID", example = "1001")
    private Long contentId;

    @Schema(description = "评论用户ID", example = "10001")
    private Long userId;

    @Schema(description = "评论用户昵称", example = "张三")
    private String userNickname;

    @Schema(description = "评论用户头像文件ID", example = "avatar-uuid-123")
    private String userAvatarFileId;

    @Schema(description = "评论用户头像URL", example = "https://example.com/avatar.jpg")
    private String userAvatarUrl;

    @Schema(description = "父评论ID（NULL表示顶级评论）", example = "100")
    private Long parentId;

    @Schema(description = "回复目标用户ID", example = "10002")
    private Long replyToUserId;

    @Schema(description = "回复目标用户昵称", example = "李四")
    private String replyToUserNickname;

    @Schema(description = "评论内容", example = "这篇文章写得很好，学到了很多！")
    private String content;

    @Schema(description = "点赞数", example = "10")
    private Integer likeCount;

    @Schema(description = "评论状态")
    private CommentStatusEnum status;

    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-11-27T10:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "子评论总数", example = "5")
    private Integer childCount;

    @Schema(description = "子评论列表（前几条）")
    private java.util.List<CommentVO> children;
}
