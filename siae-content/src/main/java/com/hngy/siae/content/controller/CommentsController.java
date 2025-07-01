package com.hngy.siae.content.controller;

import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.common.result.Result;
import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.content.dto.request.CommentDTO;
import com.hngy.siae.content.dto.response.CommentVO;
import com.hngy.siae.content.facade.CommentFacade;
import com.hngy.siae.content.service.CommentsService;
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
@RestController
@RequestMapping("/comments")
@Validated
@RequiredArgsConstructor
public class CommentsController {

    private final CommentsService commentsService;
    private final CommentFacade commentFacade;


    @PostMapping("/{contentId}")
    public Result<CommentVO> createComment(
            @PathVariable Long contentId,
            @Validated({Default.class, CreateGroup.class})
            @RequestBody CommentDTO commentDTO) {
        return commentFacade.createComment(contentId, commentDTO);
    }


    @PutMapping("/{commentId}")
    public Result<CommentVO> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDTO commentDTO) {
        // TODO: 更新后应该还需要重新进行一次审核或者先对内容进行审核再更新
        return commentsService.updateComment(commentId, commentDTO);
    }


    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        return commentsService.deleteComment(id);
    }


    @GetMapping("/{contentId}")
    public Result<PageVO<CommentVO>> listComments(
            @PathVariable Long contentId,
            @RequestParam Integer page,
            @RequestParam Integer size) {
        return commentsService.listComments(contentId, page, size);
    }
}
