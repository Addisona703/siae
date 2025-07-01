package com.hngy.siae.content.facade;

import com.hngy.siae.common.result.Result;
import com.hngy.siae.content.dto.request.CommentDTO;
import com.hngy.siae.content.dto.response.CommentVO;

/**
 * 评论外观
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
public interface CommentFacade {

    /**
     * 创建评论
     *
     * @param contentId  内容ID
     * @param commentDTO 评论dto
     * @return {@link Result }<{@link CommentVO }>
     */
    Result<CommentVO> createComment(Long contentId, CommentDTO commentDTO);

    /**
     * 删除评论
     *
     * @param id 评论id
     * @return {@link Result }<{@link Void }>
     */
    Result<Void> deleteComment(Long id);
}
