package com.hngy.siae.content.facade;

import com.hngy.siae.content.dto.request.CommentCreateDTO;
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
     * @param commentCreateDTO 评论创建dto
     * @return {@link CommentVO }
     */
    CommentVO createComment(Long contentId, CommentCreateDTO commentCreateDTO);

    /**
     * 删除评论
     *
     * @param id 评论id
     */
    void deleteComment(Long id);
}
