package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.content.dto.request.CommentCreateDTO;
import com.hngy.siae.content.dto.request.CommentUpdateDTO;
import com.hngy.siae.content.dto.request.CommentQueryDTO;
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
     * @param commentCreateDTO 评论创建dto
     * @return {@link Comment }
     */
    Comment createComment(Long contentId, CommentCreateDTO commentCreateDTO);


    /**
     * 更新评论
     *
     * @param commentId  评论id
     * @param commentUpdateDTO 评论更新dto
     * @return {@link CommentVO }
     */
    CommentVO updateComment(Long commentId, CommentUpdateDTO commentUpdateDTO);

    /**
     * 删除评论
     *
     * @param id 评论id
     */
    void deleteComment(Long id);


    /**
     * 列出评论
     *
     * @param contentId 内容ID
     * @param pageDTO   分页参数
     * @return {@link PageVO }<{@link CommentVO }>
     */
    PageVO<CommentVO> listComments(Long contentId, PageDTO<Void> pageDTO);

    /**
     * 分页查询评论（标准化分页）
     *
     * @param pageDTO 分页查询参数
     * @return {@link PageVO }<{@link CommentVO }>
     */
    PageVO<CommentVO> listComments(PageDTO<CommentQueryDTO> pageDTO);
}