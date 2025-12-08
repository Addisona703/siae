package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.content.dto.request.comment.CommentCreateDTO;
import com.hngy.siae.content.dto.request.comment.CommentUpdateDTO;
import com.hngy.siae.content.dto.request.comment.CommentQueryDTO;
import com.hngy.siae.content.dto.response.comment.CommentVO;
import com.hngy.siae.content.entity.Comment;
import com.hngy.siae.content.enums.status.CommentStatusEnum;

/**
 * 内容评论服务接口
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
public interface CommentsService extends IService<Comment> {

    /**
     * 创建评论（内部方法，不触发审核）
     *
     * @param contentId  内容ID
     * @param commentCreateDTO 评论创建dto
     * @return {@link Comment }
     */
    Comment createComment(Long contentId, CommentCreateDTO commentCreateDTO);

    /**
     * 创建评论并触发审核流程
     * 从 CommentFacade 迁移的业务逻辑
     *
     * @param contentId        内容ID
     * @param commentCreateDTO 评论创建dto
     * @return {@link CommentVO }
     */
    CommentVO createCommentWithAudit(Long contentId, CommentCreateDTO commentCreateDTO);

    /**
     * 删除评论（包含权限检查）
     * 从 CommentFacade 迁移的业务逻辑
     * 权限规则：管理员可删除任何评论，评论作者可删除自己的评论，内容创建者可删除其内容下的评论
     *
     * @param id 评论id
     */
    void deleteCommentWithPermissionCheck(Long id);


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
     * 分页查询根评论（包含子评论数量和前几条子评论）
     *
     * @param contentId 内容ID
     * @param pageDTO   分页参数（支持排序：sortBy=createTime/likeCount, sortOrder=asc/desc）
     * @return {@link PageVO }<{@link CommentVO }>
     */
    PageVO<CommentVO> listRootComments(Long contentId, PageDTO<CommentQueryDTO> pageDTO);

    /**
     * 分页查询指定根评论下的子评论（用于展开更多）
     *
     * @param contentId 内容ID
     * @param rootId    根评论ID
     * @param pageDTO   分页参数
     * @return {@link PageVO }<{@link CommentVO }>
     */
    PageVO<CommentVO> listChildComments(Long contentId, Long rootId, PageDTO<Void> pageDTO);

    /**
     * 更新评论状态（使用乐观锁）
     * 用于审核策略模式
     *
     * @param commentId 评论ID
     * @param status    目标状态
     * @return 是否更新成功（乐观锁冲突时返回 false）
     */
    boolean updateStatus(Long commentId, CommentStatusEnum status);
}