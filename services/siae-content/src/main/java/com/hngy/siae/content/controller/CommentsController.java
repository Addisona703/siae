package com.hngy.siae.content.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.comment.CommentCreateDTO;
import com.hngy.siae.content.dto.request.comment.CommentUpdateDTO;
import com.hngy.siae.content.dto.request.comment.CommentQueryDTO;
import com.hngy.siae.content.dto.response.comment.CommentVO;
import com.hngy.siae.content.service.CommentsService;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import com.hngy.siae.security.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 内容评论控制器
 * <p>
 * 权限说明：
 * - ROLE_ROOT：超级管理员，切面自动放行所有接口
 * - ROLE_ADMIN：普通管理员，可删除任何评论
 * - ROLE_MEMBER/ROLE_USER：普通用户，只能更新/删除自己的评论
 *
 * @author KEYKB
 * @date 2025/05/21
 */
@Tag(name = "评论管理", description = "内容评论的创建、更新、删除和查询等操作")
@RestController
@RequestMapping("/comments")
@Validated
@RequiredArgsConstructor
public class CommentsController {

    private final CommentsService commentsService;
    private final SecurityUtil securityUtil;


    @Operation(summary = "创建评论", description = "为指定内容创建评论，任意登录用户可操作")
    @SiaeAuthorize("isAuthenticated()")
    @PostMapping("/{contentId}")
    public Result<CommentVO> createComment(
            @Parameter(description = "内容ID", required = true, example = "1")
            @PathVariable Long contentId,
            @Parameter(description = "评论创建请求数据", required = true)
            @Valid @RequestBody CommentCreateDTO commentCreateDTO) {
        commentCreateDTO.setUserId(securityUtil.getCurrentUserId());
        return Result.success(commentsService.createCommentWithAudit(contentId, commentCreateDTO));
    }


    @Operation(summary = "更新评论", description = "更新指定评论的内容，仅评论作者可操作（Service层校验）")
    @SiaeAuthorize("isAuthenticated()")
    @PutMapping("/{commentId}")
    public Result<CommentVO> updateComment(
            @Parameter(description = "评论ID", required = true, example = "1")
            @PathVariable Long commentId,
            @Parameter(description = "评论更新请求数据", required = true)
            @Valid @RequestBody CommentUpdateDTO commentUpdateDTO) {
        // Service层校验：仅评论作者可更新
        return Result.success(commentsService.updateComment(commentId, commentUpdateDTO));
    }


    @Operation(summary = "删除评论", description = "删除指定的评论，管理员或评论作者可操作（Service层校验）")
    @SiaeAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID", required = true, example = "1")
            @PathVariable Long id) {
        // Service层校验：isAdmin() 或 isOwner(comment.userId)
        commentsService.deleteCommentWithPermissionCheck(id);
        return Result.success();
    }


    @Operation(summary = "分页查询根评论", description = "根据内容ID分页查询根评论列表，包含子评论数量，支持按时间或点赞数排序")
    @SiaeAuthorize("isAuthenticated()")
    @PostMapping("/{contentId}/root/page")
    public Result<PageVO<CommentVO>> listRootComments(
            @Parameter(description = "内容ID", required = true) @PathVariable Long contentId,
            @Parameter(description = "分页查询参数，params中可传sortBy(createTime/likeCount)和sortOrder(asc/desc)")
            @Valid @RequestBody PageDTO<CommentQueryDTO> pageDTO) {
        return Result.success(commentsService.listRootComments(contentId, pageDTO));
    }


    @Operation(summary = "分页查询子评论", description = "分页查询指定根评论下的所有子评论（用于展开更多）")
    @SiaeAuthorize("isAuthenticated()")
    @PostMapping("/{contentId}/children/{rootId}/page")
    public Result<PageVO<CommentVO>> listChildComments(
            @Parameter(description = "内容ID", required = true) @PathVariable Long contentId,
            @Parameter(description = "根评论ID", required = true) @PathVariable Long rootId,
            @Parameter(description = "分页参数")
            @Valid @RequestBody PageDTO<Void> pageDTO) {
        return Result.success(commentsService.listChildComments(contentId, rootId, pageDTO));
    }
}
