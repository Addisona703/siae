package com.hngy.siae.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.content.enums.status.CommentStatusEnum;
import com.hngy.siae.content.dto.request.CommentCreateDTO;
import com.hngy.siae.content.dto.request.CommentUpdateDTO;
import com.hngy.siae.content.dto.request.CommentQueryDTO;
import com.hngy.siae.content.dto.response.CommentVO;
import com.hngy.siae.content.entity.Comment;
import com.hngy.siae.content.mapper.CommentMapper;
import com.hngy.siae.content.service.CommentsService;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 内容评论服务impl
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
@Service
@RequiredArgsConstructor
public class CommentsServiceImpl
        extends ServiceImpl<CommentMapper, Comment>
        implements CommentsService {


    @Override
    public Comment createComment(Long contentId, CommentCreateDTO commentCreateDTO) {
        Comment comment = BeanConvertUtil.to(commentCreateDTO, Comment.class);
        comment.setStatus(CommentStatusEnum.APPROVED);
        comment.setContentId(contentId);
        AssertUtils.isTrue(this.save(comment), ContentResultCodeEnum.COMMENT_CREATE_FAILED);
        return comment;
    }

    @Override
    public CommentVO updateComment(Long commentId, CommentUpdateDTO commentUpdateDTO) {
        Comment comment = this.getById(commentId);
        AssertUtils.notNull(comment, ContentResultCodeEnum.COMMENT_NOT_FOUND);

        // 从Security上下文获取当前用户信息
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        // 获取用户ID（从Details中获取，由ServiceAuthenticationFilter设置）
        Long currentUserId = (Long) authentication.getDetails();
        
        // 判断是否为管理员（检查是否有ROLE_ADMIN或ROLE_ROOT角色）
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_ROOT"));
        
        // 权限检查：非管理员只能更新自己的评论
        if (!isAdmin) {
            AssertUtils.isTrue(comment.getUserId().equals(currentUserId), 
                ContentResultCodeEnum.COMMENT_UPDATE_NO_PERMISSION);
        }

        comment.setContent(commentUpdateDTO.getContent());
        AssertUtils.isTrue(this.updateById(comment), ContentResultCodeEnum.COMMENT_UPDATE_FAILED);

        return BeanConvertUtil.to(comment, CommentVO.class);
    }

    @Override
    public void deleteComment(Long id) {
        AssertUtils.notNull(this.getById(id), ContentResultCodeEnum.COMMENT_NOT_FOUND);
        AssertUtils.isTrue(this.removeById(id), ContentResultCodeEnum.COMMENT_DELETE_FAILED);
    }


    @Override
    public PageVO<CommentVO> listComments(Long contentId, PageDTO<Void> pageDTO) {
        // 构建分页查询条件
        Page<Comment> page = PageConvertUtil.toPage(pageDTO);
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getContentId, contentId)
                .eq(Comment::getStatus, CommentStatusEnum.APPROVED)
                .orderByDesc(Comment::getCreateTime);

        // 执行分页查询
        IPage<Comment> commentPage = baseMapper.selectPage(page, queryWrapper);

        // 使用 PageConvertUtil 转换分页结果
        return PageConvertUtil.convert(commentPage, CommentVO.class);
    }

    /**
     * 分页查询评论（使用标准化分页DTO）
     *
     * @param pageDTO 分页查询参数
     * @return 分页评论结果
     */
    public PageVO<CommentVO> listComments(PageDTO<CommentQueryDTO> pageDTO) {
        // 构建分页查询条件
        Page<Comment> page = PageConvertUtil.toPage(pageDTO);
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(Comment::getId);

        // 添加查询条件
        CommentQueryDTO params = pageDTO.getParams();
        if (params != null) {
            queryWrapper
                    .eq(params.getContentId() != null, Comment::getContentId, params.getContentId())
                    .eq(params.getUserId() != null, Comment::getUserId, params.getUserId())
                    .eq(params.getParentId() != null, Comment::getParentId, params.getParentId());
        }

        // 默认按创建时间倒序排列
        queryWrapper.orderByDesc(Comment::getCreateTime);

        // 执行分页查询
        IPage<Comment> commentPage = baseMapper.selectPage(page, queryWrapper);

        // 使用 PageConvertUtil 转换分页结果
        return PageConvertUtil.convert(commentPage, CommentVO.class);
    }
}
