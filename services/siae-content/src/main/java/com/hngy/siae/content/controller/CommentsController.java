package com.hngy.siae.content.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.CommentDTO;
import com.hngy.siae.content.dto.response.CommentVO;
import com.hngy.siae.content.facade.CommentFacade;
import com.hngy.siae.content.service.CommentsService;
import com.hngy.siae.core.validation.CreateGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
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


    /**
     * 创建评论
     *
     * @param contentId 内容ID
     * @param commentDTO 评论创建请求DTO
     * @return 创建的评论信息
     */
    @Operation(summary = "创建评论", description = "为指定内容创建评论")
    @PostMapping("/{contentId}")
    public Result<CommentVO> createComment(
            @Parameter(description = "内容ID", required = true, example = "1")
            @PathVariable Long contentId,
            @Parameter(description = "评论创建请求数据", required = true)
            @Validated({Default.class, CreateGroup.class})
            @RequestBody CommentDTO commentDTO) {
        return commentFacade.createComment(contentId, commentDTO);
    }


    /**
     * 更新评论
     *
     * @param commentId 评论ID
     * @param commentDTO 评论更新请求DTO
     * @return 更新后的评论信息
     */
    @Operation(summary = "更新评论", description = "更新指定评论的内容")
    @PutMapping("/{commentId}")
    public Result<CommentVO> updateComment(
            @Parameter(description = "评论ID", required = true, example = "1")
            @PathVariable Long commentId,
            @Parameter(description = "评论更新请求数据", required = true)
            @Valid @RequestBody CommentDTO commentDTO) {
        // TODO: 更新后应该还需要重新进行一次审核或者先对内容进行审核再更新
        return commentsService.updateComment(commentId, commentDTO);
    }


    /**
     * 删除评论
     *
     * @param id 评论ID
     * @return 删除结果
     */
    @Operation(summary = "删除评论", description = "删除指定的评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID", required = true, example = "1")
            @PathVariable Long id) {
        return commentsService.deleteComment(id);
    }


    /**
     * 列出评论（旧版本，保持向后兼容）
     *
     * @param contentId 内容ID
     * @param page      页码
     * @param size      每页大小
     * @return {@link Result }<{@link PageVO }<{@link CommentVO }>>
     */
    @GetMapping("/{contentId}")
    @Operation(summary = "查询评论列表", description = "根据内容ID分页查询评论列表")
    public Result<PageVO<CommentVO>> listComments(
            @Parameter(description = "内容ID", required = true) @PathVariable Long contentId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        return commentsService.listComments(contentId, page, size);
    }

    /**
     * 分页查询评论（标准化分页）
     *
     * @param pageDTO 分页查询参数
     * @return {@link Result }<{@link PageVO }<{@link CommentVO }>>
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询评论", description = "使用标准化分页参数查询评论列表")
    public Result<PageVO<CommentVO>> listCommentsPage(
            @Parameter(description = "分页查询参数", required = true)
            @Valid @RequestBody PageDTO<CommentDTO> pageDTO) {
        return commentsService.listComments(pageDTO);
    }
}
