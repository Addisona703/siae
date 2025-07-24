package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.CommentDTO;
import com.hngy.siae.content.dto.response.CommentVO;
import com.hngy.siae.content.entity.Comment;

/**
 * 内容评论服务接口
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
public interface CommentsService extends IService<Comment> {

    /**
     * 创建评论
     *
     * @param contentId  内容ID
     * @param commentDTO 评论dto
     * @return {@link Result }<{@link CommentVO }>
     */
    Comment createComment(Long contentId, CommentDTO commentDTO);


    /**
     * 更新评论
     *
     * @param commentId  评论id
     * @param commentDTO 评论dto
     * @return {@link Result }<{@link CommentVO }>
     */
    Result<CommentVO> updateComment(Long commentId, CommentDTO commentDTO);

    /**
     * 删除评论
     *
     * @param id 评论id
     * @return {@link Result }<{@link Void }>
     */
    Result<Void> deleteComment(Long id);


    /**
     * 列出评论
     *
     * @param contentId 内容ID
     * @param page      页
     * @param size      大小
     * @return {@link Result }<{@link PageVO }<{@link CommentVO }>>
     */
    Result<PageVO<CommentVO>> listComments(Long contentId, Integer page, Integer size);

    /**
     * 分页查询评论（标准化分页）
     *
     * @param pageDTO 分页查询参数
     * @return {@link Result }<{@link PageVO }<{@link CommentVO }>>
     */
    Result<PageVO<CommentVO>> listComments(PageDTO<CommentDTO> pageDTO);
}