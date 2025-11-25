package com.hngy.siae.content.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.CommentCreateDTO;
import com.hngy.siae.content.dto.request.CommentUpdateDTO;
import com.hngy.siae.content.dto.request.CommentQueryDTO;
import com.hngy.siae.content.dto.response.CommentVO;
import com.hngy.siae.content.facade.CommentFacade;
import com.hngy.siae.content.service.CommentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 内容评论控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/21
 */
@Tag(name = "评论管理", description = "内容评论的创建、更新、删除和查询等操作")
@RestController
@RequestMapping("/comments")
@Validated
@RequiredArgsConstructor
public class CommentsController {

    private final CommentsService commentsService;
    private final CommentFacade commentFacade;


    @Operation(summary = "创建评论", description = "为指定内容创建评论")
    @PostMapping("/{contentId}")
    public Result<CommentVO> createComment(
            @Parameter(description = "内容ID", required = true, example = "1")
            @PathVariable Long contentId,
            @Parameter(description = "评论创建请求数据", required = true)
            @Valid @RequestBody CommentCreateDTO commentCreateDTO) {
        return Result.success(commentFacade.createComment(contentId, commentCreateDTO));
    }


    @Operation(summary = "更新评论", description = "更新指定评论的内容")
    @PutMapping("/{commentId}")
    public Result<CommentVO> updateComment(
            @Parameter(description = "评论ID", required = true, example = "1")
            @PathVariable Long commentId,
            @Parameter(description = "评论更新请求数据", required = true)
            @Valid @RequestBody CommentUpdateDTO commentUpdateDTO) {
        // TODO: 更新后应该还需要重新进行一次审核或者先对内容进行审核再更新
        return Result.success(commentsService.updateComment(commentId, commentUpdateDTO));
    }


    @Operation(summary = "删除评论", description = "删除指定的评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID", required = true, example = "1")
            @PathVariable Long id) {
        commentFacade.deleteComment(id);
        return Result.success();
    }


    @PostMapping("/{contentId}/page")
    @Operation(summary = "查询评论列表", description = "根据内容ID分页查询评论列表")
    public Result<PageVO<CommentVO>> listComments(
            @Parameter(description = "内容ID", required = true) @PathVariable Long contentId,
            @Parameter(description = "分页查询参数", required = true)
            @Valid @RequestBody PageDTO<Void> pageDTO) {
        return Result.success(commentsService.listComments(contentId, pageDTO));
    }


    @PostMapping("/page")
    @Operation(summary = "分页查询评论", description = "可以查询指定用户发表的评论，也可以根据父评论查询子评论")
    public Result<PageVO<CommentVO>> listCommentsPage(
            @Parameter(description = "分页查询参数", required = true)
            @Valid @RequestBody PageDTO<CommentQueryDTO> pageDTO) {
        return Result.success(commentsService.listComments(pageDTO));
    }
}
